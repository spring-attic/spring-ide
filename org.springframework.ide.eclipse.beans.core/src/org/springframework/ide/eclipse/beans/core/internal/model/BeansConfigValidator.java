/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class BeansConfigValidator {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
													   "/model/validator/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private IProgressMonitor monitor;

	public BeansConfigValidator(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void validate(IFile file) {
		if (file != null && file.isAccessible()) {
			if (monitor != null) {
				if (monitor.isCanceled()) {
					return;
				}
				monitor.beginTask(BeansCorePlugin.getFormattedMessage(
											"BeansConfigValidator.validateFile",
											file.getFullPath().toString()), 2);
			}

			// Delete all problem markers created by Spring IDE
			BeansCoreUtils.deleteProblemMarkers(file);

			// Reset the corresponding config within the bean model to force
			// re-reading the config file and updating the model
			IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);
			config.reset();

			// At first check if model was able to parse the config file 
			BeanDefinitionException e = config.getException();
			if (e != null) {
				createProblemMarker(config, e.getMessage(),
								 IMarker.SEVERITY_ERROR, e.getLineNumber(),
								 IBeansProjectMarker.ERROR_CODE_PARSING_FAILED);
			} else {
	
				// Now validate the config
				validateConfig(config, null);
				if (monitor != null) {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						return;
					}
				}
	
				// Finally validate the config file within all defined config sets
				Collection configSets = project.getConfigSets();
				if (configSets.size() > 0) {
					Iterator iter = configSets.iterator();
					while (iter.hasNext()) {
						if (monitor != null && monitor.isCanceled()) {
							return;
						}
						IBeansConfigSet configSet = (IBeansConfigSet)
																	iter.next();
						if (configSet.hasConfig(file)) {
							validateConfig(config, configSet);
							validateConfigSet(configSet, config);
						}
					}
				}
				if (monitor != null) {
					monitor.worked(1);
				}
			}
		}
		if (monitor != null) {
			monitor.done();
		}
	}

	protected void validateConfig(IBeansConfig config,
								  IBeansConfigSet configSet) {
		if (DEBUG && configSet == null) {
			System.out.println("Validating config '" +
							   config.getConfigPath() + "'");
		}
		if (monitor != null && configSet == null) {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
										  "BeansConfigValidator.validateConfig",
										  config.getConfigPath()));
		}

		// Validate all beans
		Iterator iter = config.getBeans().iterator();
		while (iter.hasNext()) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			IBean bean = (IBean) iter.next();
			validateBean(bean, configSet);
		}

		// Validate all inner beans
		iter = config.getInnerBeans().iterator();
		while (iter.hasNext()) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			IBean bean = (IBean) iter.next();
			validateBean(bean, configSet);
		}
    	}

	protected void validateConfigSet(IBeansConfigSet configSet,
									 IBeansConfig config) {
		String configSetName = configSet.getElementName();
		if (DEBUG) {
			System.out.println("Validating config '" + config.getConfigPath() +
							   "' in set '" + configSetName + "'");
		}
		if (monitor != null) {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
					  "BeansConfigValidator.validateConfigSet", configSetName));
		}

		// Check all beans of given config if they override a bean defined
		// earlier in the config set
		if (!configSet.isAllowBeanDefinitionOverriding()) {
			IBeansProject project = (IBeansProject) config.getElementParent();
			Iterator beans = config.getBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				Iterator configs = configSet.getConfigs().iterator();
				while (configs.hasNext()) {
					if (monitor != null && monitor.isCanceled()) {
						return;
					}
					String configName = (String) configs.next();
					if (configName.equals(config.getElementName())) {
						break;
					}
					IBeansConfig cfg = getConfig(project, configName); 
					if (cfg != null && cfg.hasBean(bean.getElementName())) {
						createProblemMarker(config, "Overrides bean in " +
							 "config '" + cfg.getElementName() +
							 "' within config set '" + configSetName + "'",
							 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							 IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
							 bean.getElementName(), configSetName);
					}
				}
			}
		}

		// If the config set is complete the check all bean references of given
		// config
		if (!configSet.isIncomplete()) {

			// Validate all beans
			Iterator beans = config.getBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				validateRefBeansInBean(bean, configSet);
			}

			// Validate all inner beans
			beans = config.getInnerBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				validateRefBeansInBean(bean, configSet);
			}
		}
	}

	protected void validateBean(IBean bean, IBeansConfigSet configSet) {
		if (monitor != null) {
			if (monitor.isCanceled()) {
				return;
			}
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
				   "BeansConfigValidator.validateBean", bean.getElementName()));
		}

		// Get Java type for given bean and validate bean's constructor
		// arguments and properties
		IType type = getBeanType(bean, configSet);
		if (type != null) {

			// Don't validate constructor arguments of abstract beans or for an
			// incomplete config set
			if (!bean.isAbstract() && configSet != null &&
													!configSet.isIncomplete()) {
				validateConstructorArguments(bean, type, configSet);
			}
			validateProperties(bean, type, configSet);
		}
	}

	protected void validateConstructorArguments(IBean bean, IType type,
													IBeansConfigSet configSet) {
		if (monitor != null) {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
									"BeansConfigValidator.validateConstructors",
									bean.getElementName()));
		}
		int numArguments = getConstructorArguments(bean, configSet).size();
		if (numArguments > 0) {
			try {
				boolean found = false;
				List cons = Introspector.getConstructors(type);
				for (Iterator iter = cons.iterator(); iter.hasNext();) {
					if (monitor != null && monitor.isCanceled()) {
						return;
					}
					IMethod method = (IMethod) iter.next();
					if (method.getNumberOfParameters() == numArguments) {
						found = true;
						break;
					}
				}
				if (!found) {
					createProblemMarker(bean, "No constructor with " +
						   numArguments + (numArguments == 1 ? " argument" :
						   " arguments") + " defined in class '" +
						   type.getFullyQualifiedName() + "'",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_NO_CONSTRUCTOR,
						   bean.getElementName(), type.getFullyQualifiedName());
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateProperties(IBean bean, IType type,
									  IBeansConfigSet configSet) {
		if (monitor != null) {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
									  "BeansConfigValidator.validateProperties",
									  bean.getElementName()));
		}

		// Validate all properties defined in given bean
		Iterator iter = bean.getProperties().iterator();
		while (iter.hasNext()) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			IBeanProperty property = (IBeanProperty) iter.next();
			String propertyName = property.getElementName();

			// Check for setter in given type
			try {
				if (!Introspector.hasWritableProperty(type, propertyName)) {
					createProblemMarker(bean, "No setter found for property '" +
						 propertyName + "' in class '" +
						 type.getFullyQualifiedName() + "'",
						 IMarker.SEVERITY_ERROR, property.getElementStartLine(),
						 IBeansProjectMarker.ERROR_CODE_NO_SETTER,
						 bean.getElementName(), property.getElementName());
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}

		// If given bean is a child bean the validate all parent properties too
		if (!bean.isRootBean()) {
			iter = getParentProperties(bean, configSet).iterator();
			while (iter.hasNext()) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}
				IBeanProperty property = (IBeanProperty) iter.next();
				String propertyName = property.getElementName();
	
				// Check for setter in given type
				try {
					if (!Introspector.hasWritableProperty(type, propertyName)) {
						createProblemMarker(bean, "No setter found for " +
							"parent property '" + propertyName +
							"' in class '" + type.getFullyQualifiedName() + "'",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_NO_SETTER,
							bean.getElementName(), property.getElementName());
					}
				} catch (JavaModelException e) {
					BeansCorePlugin.log(e);
				}
			}
		}
	}

	protected void validateRefBeansInBean(IBean bean,
										   IBeansConfigSet configSet) {
		// Validate referenced beans in constructor arguments
		Iterator cargs = bean.getConstructorArguments().iterator();
		while (cargs.hasNext()) {
			IBeanConstructorArgument carg = (IBeanConstructorArgument)
														   cargs.next();
			validateRefBeansInValue(carg, carg.getValue(), configSet);
		}

		// Validate referenced beans in properties
		Iterator props = bean.getProperties().iterator();
		while (props.hasNext()) {
			IBeanProperty prop = (IBeanProperty) props.next();
			validateRefBeansInValue(prop, prop.getValue(), configSet);
		}
	}

	protected void validateRefBeansInValue(IBeansModelElement element,
									  Object value, IBeansConfigSet configSet) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			IBean bean = configSet.getBean(beanName);
			if (bean == null) {
				createProblemMarker(element, "Referenced bean '" + beanName +
							 "' not found", IMarker.SEVERITY_ERROR,
							 element.getElementStartLine(),
							 IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
							 null, beanName);
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				validateRefBeansInValue(element, list.get(i), configSet);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				validateRefBeansInValue(element, iter.next(), configSet);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				validateRefBeansInValue(element, map.get(iter.next()),
										configSet);
			}
		}
	}

	protected void createProblemMarker(IBeansModelElement element,
						String message, int severity, int line, int errorCode) {
		createProblemMarker(element, message, severity, line, errorCode, null,
							null);
	}

	protected void createProblemMarker(IBeansModelElement element,
						  String message, int severity, int line, int errorCode,
						  String beanID, String errorData) {
		IFile file;
		if (element instanceof IBeansConfig) {
			file = ((IBeansConfig) element).getConfigFile();
		} else if (element instanceof IBean) {
			file = ((IBean) element).getConfig().getConfigFile();
		} else if (element instanceof IBeanProperty) {
			IBean bean = (IBean) ((IBeanProperty) element).getElementParent();
			file = bean.getConfig().getConfigFile();
		} else if (element instanceof IBeanConstructorArgument) {
			IBean bean = (IBean)
						((IBeanConstructorArgument) element).getElementParent();
			file = bean.getConfig().getConfigFile();
		} else {
			file = null;
		}
		if (file != null) {
			BeansCoreUtils.createProblemMarker(file, message, severity, line,
											   errorCode, beanID, errorData);
		}
	}

	private IBeansConfig getConfig(IBeansProject project, String configName) {

		// For external project get the corresponding project from beans model 
		if (configName.charAt(0) == '/') {
			String projectName = configName.substring(0,
													configName.indexOf('/', 1));
			project = BeansCorePlugin.getModel().getProject(projectName);
		}
		return (project != null ? project.getConfig(configName) : null);
	}

	/**
	 * Returns the Java type of the given bean's class or (for child beans) the
	 * parent's class.
	 */
	private IType getBeanType(IBean bean, IBeansConfigSet configSet) {
		IType type = null;
		IFile configFile = bean.getConfig().getConfigFile();
		String className = bean.getClassName();
		if (className != null) {
			type = BeansModelUtil.getJavaType(configFile.getProject(),
											  className);
			if (type == null && configSet == null) {
				BeansCoreUtils.createProblemMarker(configFile,
							 "Class '" + className + "' not found",
							 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							 IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
							 bean.getElementName(), className);
			}
		} else if (!bean.isRootBean()) {

			// For child beans use parent's bean type
			IBean parent = getParentBean(bean, configSet);
			if (parent != null) {
				type = getBeanType(parent, configSet);
			}
		}
		return type;
	}

	private IBean getParentBean(IBean bean, IBeansConfigSet configSet) {
		String parentName = bean.getParentName();

		// If a config set was given then get the parent bean from this config
		// set else get it from the bean's config
		IBean parent;
		if (configSet != null) {
			parent = configSet.getBean(parentName);
			if (parent == null && !configSet.isIncomplete()) {
				createProblemMarker(bean, "Parent bean '" + parentName +
					  "' not found in config set '" +
					  configSet.getElementName() + "'",
					  IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_ROOT_BEAN,
					  bean.getElementName(), parentName);
			}
		} else {
			parent = bean.getConfig().getBean(parentName);
		}

		// If parent bean is a child bean then go for it's parent bean
		if (parent != null && !parent.isRootBean()) {
			parent = getParentBean(parent, configSet);
		}
		return parent;
	}

	private List getConstructorArguments(IBean bean,
										 IBeansConfigSet configSet) {
		List cargs = new ArrayList(bean.getConstructorArguments());
		IBean parent = bean;
		while (parent != null && !parent.isRootBean()) {
			String parentName = parent.getParentName();

			// If a config set was given then get the parent bean from this config
			// set else get it from the bean's config
			if (configSet != null) {
				parent = configSet.getBean(parentName);
			} else {
				parent = bean.getConfig().getBean(parentName);
			}
			if (parent != null) {
				cargs.addAll(parent.getConstructorArguments());
			}
		}
		return cargs;
	}

	private List getParentProperties(IBean bean, IBeansConfigSet configSet) {
		List props = new ArrayList();
		IBean parent = bean;
		while (parent != null && !parent.isRootBean()) {
			String parentName = parent.getParentName();

			// If a config set was given then get the parent bean from this config
			// set else get it from the bean's config
			if (configSet != null) {
				parent = configSet.getBean(parentName);
			} else {
				parent = bean.getConfig().getBean(parentName);
			}
			if (parent != null) {
				props.addAll(parent.getProperties());
			}
		}
		return props;
	}
}
