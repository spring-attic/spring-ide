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

package org.springframework.ide.eclipse.beans.core.internal.project;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeanDefinitionException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;

public class BeansProjectValidator extends BeansProjectBuilder {

	public static final String BUILDER_ID = BeansCorePlugin.PLUGIN_ID +
    														  ".beansvalidator";
	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
													 "/project/validator/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	protected void buildFile(IFile file, IProgressMonitor monitor) {
		monitor.beginTask(BeansCorePlugin.getFormattedMessage(
										   "BeansProjectValidator.validateFile",
										   file.getFullPath().toString()), 3);
		BeansCoreUtils.deleteProblemMarkers(file);
		monitor.worked(1);
		validate(file, monitor);	
		monitor.done();
	}

	protected void validate(IFile file, IProgressMonitor monitor) {
		IBeansProject project = BeansCorePlugin.getModel().getProject(
																  getProject());
		IBeansConfig config = project.getConfig(file);

		// At first check if model was able to parse the config file 
		BeanDefinitionException e = config.getException();
		if (e != null) {
			BeansCoreUtils.createProblemMarker(config.getConfigFile(),
					 e.getMessage(), IMarker.SEVERITY_ERROR, e.getLineNumber(),
					 IBeansProjectMarker.ERROR_CODE_PARSING_FAILED);
		} else {

			// Now validate the config
			validateConfig(config, monitor);
			monitor.worked(1);

			// Finally validate the config file within all defined config sets
			Collection configSets = project.getConfigSets();
			if (configSets.size() > 0) {
				Iterator iter = configSets.iterator();
				while (iter.hasNext()) {
					IBeansConfigSet configSet = (IBeansConfigSet) iter.next();
					if (configSet.hasConfig(file)) {
						validateConfigSet(configSet, config, monitor);
					}
				}
			}
			monitor.worked(1);
		}
	}

	protected void validateConfig(IBeansConfig config,
													 IProgressMonitor monitor) {
		if (DEBUG) {
			System.out.println("Validating config '" + config.getElementName()
					+ "'");
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
										 "BeansProjectValidator.validateConfig",
										 config.getElementName()));
		// Validate all beans
		Iterator iter = config.getBeans().iterator();
		while (iter.hasNext()) {
			IBean bean = (IBean) iter.next();
			validateBean(bean, config, monitor);
		}

		// Validate all inner beans
		iter = config.getInnerBeans().iterator();
		while (iter.hasNext()) {
			IBean bean = (IBean) iter.next();
			validateBean(bean, config, monitor);
		}
    	}

	protected void validateConfigSet(IBeansConfigSet configSet,
								IBeansConfig config, IProgressMonitor monitor) {
		if (DEBUG) {
			System.out.println("Validating config '" + config.getElementName() +
							   "' in set '" + configSet.getElementName() + "'");
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
									  "BeansProjectValidator.validateConfigSet",
									  configSet.getElementName()));

		// Check all beans of given config if they override a bean defined
		// earlier in the config set
		if (!configSet.isAllowBeanDefinitionOverriding()) {
			IBeansProject project = (IBeansProject) config.getElementParent();
			Iterator beans = config.getBeans().iterator();
			while (beans.hasNext()) {
				IBean bean = (IBean) beans.next();
				Iterator configs = configSet.getConfigs().iterator();
				while (configs.hasNext()) {
					String configName = (String) configs.next();
					if (configName.equals(config.getElementName())) {
						break;
					}
					IBeansConfig cfg = getConfig(configName); 
					if (cfg != null && cfg.hasBean(bean.getElementName())) {
						BeansCoreUtils.createProblemMarker(
							config.getConfigFile(), "Overrides bean in " +
							"config '" + cfg.getElementName() + "' within " +
							"config set '" + configSet.getElementName() + "'",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
							bean.getElementName(), configSet.getElementName());
					}
				}
			}
		}
	}

	protected void validateBean(IBean bean, IBeansConfig config,
													 IProgressMonitor monitor) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
				  "BeansProjectValidator.validateBean", bean.getElementName()));
		IBeansProject project = (IBeansProject) config.getElementParent();
		IType type = null;
		if (bean.isRootBean()) {

			// Validate root bean's class
			String className = bean.getClassName();
			if (className == null) {
				BeansCoreUtils.createProblemMarker(config.getConfigFile(),
					"Bean definition has neither 'class' nor 'parent'",
					IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					IBeansProjectMarker.ERROR_CODE_BEAN_WITHOUT_CLASS_OR_PARENT,
					bean.getElementName(), null);
			} else {
				type = project.getJavaType(className);
				if (type == null) {
					BeansCoreUtils.createProblemMarker(config.getConfigFile(),
							"Class '" + className + "' not found",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
							bean.getElementName(), className);
				}
			}
		} else {

			// Validate child bean's parent root bean
			IBean parent = getParentBean(bean);
			if (parent != null) {
				type = project.getJavaType(parent.getClassName());
			} else {
				BeansCoreUtils.createProblemMarker(config.getConfigFile(),
					  "Undefined parent root bean", IMarker.SEVERITY_ERROR,
					  bean.getElementStartLine(),
					  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_ROOT_BEAN,
					  bean.getElementName(), null);
			}
		}
		if (type != null) {
			validateConstructorArguments(bean, type, config, monitor);
			validateProperties(bean, type, config, monitor);
		}
	}

	protected void validateConstructorArguments(IBean bean, IType type,
								IBeansConfig config, IProgressMonitor monitor) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
								   "BeansProjectValidator.validateConstructors",
								   bean.getElementName()));
		Collection cargs = bean.getConstructorArguments();
		if (cargs.size() > 0) {
			try {
				boolean found = false;
				List cons = Introspector.getConstructors(type);
				for (Iterator iter = cons.iterator(); iter.hasNext();) {
					IMethod method = (IMethod) iter.next();
					if (method.getNumberOfParameters() == cargs.size()) {
						found = true;
						break;
					}
				}
				if (!found) {
					BeansCoreUtils.createProblemMarker(config.getConfigFile(),
						   "No constructor with " + cargs.size() +
						   " defined in class '" +
						   type.getFullyQualifiedName() + "'",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_NO_CONSTRUCTOR,
						   bean.getElementName(), type.getFullyQualifiedName());
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}

	protected void validateProperties(IBean bean, IType type,
								IBeansConfig config, IProgressMonitor monitor) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
									 "BeansProjectValidator.validateProperties",
									 bean.getElementName()));
		Iterator iter = bean.getProperties().iterator();
		while (iter.hasNext()) {
			IBeanProperty property = (IBeanProperty) iter.next();
			String propertyName = property.getElementName();
			boolean isWritableProperty = false;
			try {
				isWritableProperty = Introspector.hasWritableProperty(type,
																  propertyName);
			} catch (JavaModelException e) {
				// ignore
			}
			if (!isWritableProperty) {
				BeansCoreUtils.createProblemMarker(config.getConfigFile(),
					  "No setter for property '" + propertyName +
					  "' found in class '" + type.getFullyQualifiedName() + "'",
					  IMarker.SEVERITY_ERROR, property.getElementStartLine(),
					  IBeansProjectMarker.ERROR_CODE_NO_SETTER,
					  bean.getElementName(), property.getElementName());
			}
		}
	}

	private IBeansConfig getConfig(String configName) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject project;
		if (configName.charAt(0) == '/') {
			String projectName = configName.substring(0,
													configName.indexOf('/', 1));
			project = model.getProject(projectName);
		} else {
			project = model.getProject(getProject());
		}
		return (project != null ? project.getConfig(configName) : null);
	}

	private IBean getParentBean(IBean bean) {
		IBeansConfig config = bean.getConfig();
		IBean parent = config.getBean(bean.getParentName());
		if (parent != null && !parent.isRootBean()) {
			parent = getParentBean(parent);
		}
		return parent;
	}
}
