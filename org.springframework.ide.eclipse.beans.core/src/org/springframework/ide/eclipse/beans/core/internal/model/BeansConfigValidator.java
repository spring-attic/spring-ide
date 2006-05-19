/*
 * Copyright 2002-2006 the original author or authors.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.StringUtils;

/**
 * Validates given beans config file. 
 * @author Torsten Juergeleit
 */
public class BeansConfigValidator {

	private static final String PLACEHOLDER_PREFIX = "${";
	private static final String PLACEHOLDER_SUFFIX = "}";

	private static final String FACTORY_BEAN_REFERENCE_PREFIX = "&";
	private static final String FACTORY_BEAN_REFERENCE_REGEXP = "[&]";

	private static final int METHOD_TYPE_FACTORY = 1;
	private static final int METHOD_TYPE_INIT = 2;
	private static final int METHOD_TYPE_DESTROY = 3;

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
													  "/model/validator/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private IProgressMonitor monitor;

	public void validate(IBeansConfig config, IProgressMonitor monitor) {
		this.monitor = monitor;

		// Validate the config file within all defined config sets
		boolean isValidated = false;
		Iterator configSets = ((IBeansProject)
						  config.getElementParent()).getConfigSets().iterator();
		while (configSets.hasNext()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IBeansConfigSet configSet = (IBeansConfigSet) configSets.next();
			if (configSet.hasConfig(config.getElementName())) {
				DefaultBeanDefinitionRegistry registry =
									   new DefaultBeanDefinitionRegistry(null); 
				registry.setAllowAliasOverriding(false);
				registry.setAllowBeanDefinitionOverriding(
								  configSet.isAllowBeanDefinitionOverriding());
				IBeansProject project = (IBeansProject)
												  configSet.getElementParent();
				Iterator cfgNames = configSet.getConfigs().iterator();
				while (cfgNames.hasNext()) {
					String cfgName = (String) cfgNames.next();
					if (cfgName.equals(config.getElementName())) {
						validateConfig(config, configSet, registry);
					} else {
						// Resolve config name (including support for external
						// project references!!!)
						IBeansConfig cfg = BeansModelUtils.getConfig(cfgName,
																	 project);
						if (cfg != null) {
							BeansModelUtils.registerBeanDefinitions(cfg,
																	registry);
						}
					}
				}

				// If the config set is complete the check all bean references
				// of given config
				if (!configSet.isIncomplete()) {
					validateConfigReferences(config, configSet, registry);
				}
				isValidated = true;
			}
		}

		// If not already validated then validate config file now
		if (!isValidated) {
			DefaultBeanDefinitionRegistry registry =
									   new DefaultBeanDefinitionRegistry(null); 
			registry.setAllowAliasOverriding(false);
			registry.setAllowBeanDefinitionOverriding(false);
			validateConfig(config, null, registry);
			validateConfigReferences(config, null, registry);
		}
		monitor.worked(1);
	}

	protected void validateConfig(IBeansConfig config,
				  IBeansConfigSet configSet, BeanDefinitionRegistry registry) {
		if (configSet == null) {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
										 "BeansConfigValidator.validateConfig",
										 config.getConfigPath()));
			if (DEBUG) {
				System.out.println("Validating config '" +
								   config.getConfigPath() + "'");
			}
		} else {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
								 "BeansConfigValidator.validateConfigSet",
								 new String[] { config.getConfigPath(),
								 				configSet.getElementName() }));
			if (DEBUG) {
				System.out.println("Validating config '" +
								   config.getConfigPath() + "' in set '" +
								   configSet.getElementName() + "'");
			}
		}

		// Validate all beans
		Iterator beans = config.getBeans().iterator();
		while (beans.hasNext()) {
			IBean bean = (IBean) beans.next();
			validateBean(bean, configSet, registry);
		}
		
		// Finally validate all aliases
		Iterator aliases = config.getAliases().iterator();
		while (aliases.hasNext()) {
			IBeanAlias alias = (IBeanAlias) aliases.next();
			validateAlias(alias, configSet, registry);
		}
    }

	protected void validateAlias(IBeanAlias alias, IBeansConfigSet configSet,
								 BeanDefinitionRegistry registry) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
				"BeansConfigValidator.validateAlias", alias.getElementName()));
		
		// Validate bean overriding
		if (registry.containsBeanDefinition(alias.getElementName())) {
			if (configSet == null ||
									  BeansModelUtils.getConfig(alias).hasBean(
											  		 alias.getElementName())) {
				BeansModelUtils.createProblemMarker(alias,
						   "Overrides another bean in the same config file",
						   IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
						   alias.getElementName(), null);
			} else if (!configSet.isAllowBeanDefinitionOverriding()) {
				BeansModelUtils.createProblemMarker(alias,
						   "Overrides another bean in config set '" +
						   configSet.getElementName() + "'",
						   IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
						   alias.getElementName(), configSet.getElementName());
			}
		}

		// Validate alias overriding within config file 
		Iterator aliases = BeansModelUtils.getConfig(
												alias).getAliases().iterator();
		while (aliases.hasNext()) {
			IBeanAlias al = (IBeanAlias) aliases.next();
			if (al == alias) {
				break;
			} else if (al.getElementName().equals(alias.getElementName())) {
				BeansModelUtils.createProblemMarker(alias,
						   "Overrides another alias in the same config file",
						   IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_ALIAS_OVERRIDE,
						   alias.getElementName(), alias.getName());
				break;
			}
		}

		// Validate alias within config set
		if (configSet != null) {

			// Validate alias overriding
			Iterator configs = configSet.getConfigs().iterator();
			while (configs.hasNext()) {
				String configName = (String) configs.next();
				IBeansConfig config = BeansModelUtils.getConfig(configName,
																configSet);
				if (config == BeansModelUtils.getConfig(alias)) {
					break;
				} else if (config.getAlias(alias.getElementName()) != null) {
					BeansModelUtils.createProblemMarker(alias,
						   "Overrides another alias in config set '" +
						   configSet.getElementName() + "'",
						   IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_ALIAS_OVERRIDE,
						   alias.getElementName(), configSet.getElementName());
					break;
				}
			}
			
			// Check if corresponding bean exists
			if (!configSet.isIncomplete() &&
					!registry.containsBeanDefinition(alias.getName())) {
				BeansModelUtils.createProblemMarker(alias,
					  "Referenced bean '" + alias.getName() +
					  "' not found in config set '" +
					  configSet.getElementName() + "'",
					  IMarker.SEVERITY_WARNING, alias.getElementStartLine(),
					  IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCED_BEAN,
					  alias.getElementName(), alias.getName());
			}
		}
	}

	protected void validateBean(IBean bean, IBeansConfigSet configSet,
								BeanDefinitionRegistry registry) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
				   "BeansConfigValidator.validateBean", bean.getElementName()));
		
		// Validate bean's name and aliases
		validateBeanDefinitionHolder(bean, configSet, registry);

		// Get bean's definition and the one merged with it's parent bean(s)
		AbstractBeanDefinition bd = (AbstractBeanDefinition)
									   BeansModelUtils.getBeanDefinition(bean);
		AbstractBeanDefinition mergedBd;
		if (configSet == null) {
			mergedBd = (AbstractBeanDefinition)
					  BeansModelUtils.getMergedBeanDefinition(bean,
				  						  BeansModelUtils.getConfig(bean));
		} else {
			mergedBd = (AbstractBeanDefinition)
				  BeansModelUtils.getMergedBeanDefinition(bean, configSet);
		}

		// Validate bean definition
		try {
			bd.validate();
		} catch (BeanDefinitionValidationException e) {
			BeansModelUtils.createProblemMarker(bean,
						"Invalid bean definition: " + e.getMessage(),
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_INVALID_BEAN_DEFINITION,
						bean.getElementName(), null);
		}

		// Get bean's merged bean definition and class name
		String className = bd.getBeanClassName();
		String mergedClassName = mergedBd.getBeanClassName();

		// Validate bean class and constructor arguments - skip child beans and
		// class names with placeholders
		if (className != null && !hasPlaceHolder(className)) {
			IType type = BeansModelUtils.getJavaType(
					BeansModelUtils.getProject(bean).getProject(), className);
			if (type == null) {
				BeansModelUtils.createProblemMarker(bean,
								"Class '" + className + "' not found",
								IMarker.SEVERITY_ERROR,
								bean.getElementStartLine(),
								IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
								bean.getElementName(), className);
			} else {

				// Validate merged constructor args of non-abstract beans only
				if (!bean.isAbstract()) {
					validateConstructorArguments(bean, type,
									  mergedBd.getConstructorArgumentValues());
				}
			}
		}
		
		// Validate bean's constructor arguments, init-method, destroy-method
		// and properties with bean class from merged bean definition - skip
		// class names with placeholders
		if (mergedClassName != null && !hasPlaceHolder(mergedClassName)) {
			IType type = BeansModelUtils.getJavaType(
								 BeansModelUtils.getProject(bean).getProject(),
								 mergedClassName);
			if (type != null) {

				// Validate constructor args of non-abstract beans
				if (bd.hasConstructorArgumentValues() && !bean.isAbstract()) {
					validateConstructorArguments(bean, type,
									  mergedBd.getConstructorArgumentValues());
				}

				// Validate bean's init-method and destroy-method
				validateMethod(bean, type, METHOD_TYPE_INIT,
							   bd.getInitMethodName(), 0, false);
				validateMethod(bean, type, METHOD_TYPE_DESTROY,
							   bd.getDestroyMethodName(), 0, false);

				// Validate bean's properties
				validateProperties(bean, type, bd.getPropertyValues());
			}
		}

		// Validate bean's static factory method with bean class from merged
		// bean definition - skip factory methods with placeholders or
		// abstract beans
		String methodName = bd.getFactoryMethodName();
		if (methodName != null && !hasPlaceHolder(methodName)) {
			if (mergedClassName == null) {
				if (bd.getFactoryBeanName() == null &&
										!(bd instanceof ChildBeanDefinition)) {
					BeansModelUtils.createProblemMarker(bean,
						"Factory method needs class from root or parent bean",
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_BEAN_WITHOUT_CLASS_OR_PARENT,
						bean.getElementName(), null);
				}
			} else {

				// Use constructor argument values of root bean as arguments
				// for static factory method
				int argCount = (bd instanceof RootBeanDefinition &&
					!bd.isAbstract() ?
					bd.getConstructorArgumentValues().getArgumentCount() : -1);
				validateFactoryMethod(bean, mergedClassName, methodName,
									  argCount, true);
			}
		}
		
		// Validate this bean's inner beans recursively
		Iterator innerBeans = bean.getInnerBeans().iterator();
		while (innerBeans.hasNext()) {
			IBean innerBean = (IBean) innerBeans.next();
			validateBean(innerBean, configSet, registry);
		}
	}

	protected void validateBeanDefinitionHolder(IBean bean,
				  IBeansConfigSet configSet, BeanDefinitionRegistry registry) {
		BeanDefinitionHolder bdHolder = ((Bean) bean).getBeanDefinitionHolder();

		// Validate bean name
		try {
			registry.registerBeanDefinition(bdHolder.getBeanName(),
											bdHolder.getBeanDefinition());
		} catch (BeanDefinitionStoreException e) {
			if (configSet == null) {
				BeansModelUtils.createProblemMarker(bean,
						   "Overrides another bean in the same config file",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
						   bean.getElementName(), null);
			} else if (!configSet.isAllowBeanDefinitionOverriding()) {
				BeansModelUtils.createProblemMarker(bean,
						   "Overrides another bean in config set '" +
						   configSet.getElementName() + "'",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						   IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
						   bean.getElementName(), configSet.getElementName());
			}
		}

		// Validate bean aliases
		if (bdHolder.getAliases() != null) {
			String[] aliases = bdHolder.getAliases();
			for (int i = 0; i < aliases.length; i++) {
				String alias = aliases[i];
				try {
					registry.registerAlias(bdHolder.getBeanName(), alias);
				} catch (BeanDefinitionStoreException e) {
					BeansModelUtils.createProblemMarker(bean,
							 e.getMessage(), IMarker.SEVERITY_ERROR,
							 bean.getElementStartLine(),
							 IBeansProjectMarker.ERROR_CODE_INVALID_BEAN_ALIAS,
							 bean.getElementName(), alias);
				}
			}
		}
	}

	protected void validateConstructorArguments(IBean bean, IType type,
									ConstructorArgumentValues argumentValues) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
						   "BeansConfigValidator.validateConstructorArguments",
						   bean.getElementName()));
		// Skip validation if auto-wiring or a factory are involved
		AbstractBeanDefinition bd = (AbstractBeanDefinition)
									   BeansModelUtils.getBeanDefinition(bean);
		if (bd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO &&
										   bd.getFactoryBeanName() == null &&
										   bd.getFactoryMethodName() == null) {
			// Check for default constructor if no constructor arguments are
			// available
			int numArguments = (argumentValues == null ? 0 :
											argumentValues.getArgumentCount());
			try {
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element =
							 BeansModelUtils.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}
					BeansModelUtils.createProblemMarker(bean,
						"No constructor with " + numArguments +
						(numArguments == 1 ? " argument" : " arguments") +
						" defined in class '" + type.getFullyQualifiedName() +
						"'", IMarker.SEVERITY_ERROR,
						element.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_NO_CONSTRUCTOR,
						bean.getElementName(), type.getFullyQualifiedName());
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateProperties(IBean bean, IType type,
									   MutablePropertyValues propertyValues) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
									  "BeansConfigValidator.validateProperties",
									  bean.getElementName()));
		// Validate all properties defined in given property values instance
		PropertyValue[] propValues = propertyValues.getPropertyValues();
		for (int i = 0; i < propValues.length; i++) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			PropertyValue propValue = propValues[i];
			String propertyName = propValue.getName();

			// Skip properties with placeholders
			if (hasPlaceHolder(propertyName)) {
				continue;
			}

			// Check for property accessor in given type
			try {

				// First check for nested property path
				int pos = getNestedPropertySeparatorIndex(propertyName, false);
				if (pos >= 0) {
					String nestedPropertyName = propertyName.substring(0, pos);
					PropertyTokenHolder tokens = getPropertyNameTokens(
														   nestedPropertyName);
					String getterName = "get" + StringUtils.capitalize(
															tokens.actualName);
					IMethod getter = Introspector.findMethod(type, getterName,
											  0, true, Introspector.STATIC_NO);
					if (getter == null) {
						IBeanProperty property = bean.getProperty(propertyName);
						BeansModelUtils.createProblemMarker(bean,
									"No getter found for nested property '" +
							 		nestedPropertyName + "' in class '" +
							 		type.getFullyQualifiedName() + "'",
							 		IMarker.SEVERITY_ERROR, (property != null ?
						 					   property.getElementStartLine() :
						 					   bean.getElementStartLine()),
						 IBeansProjectMarker.ERROR_CODE_NO_GETTER,
						 bean.getElementName(), propertyName);
					} else {

						// Check getter's return type
						if (tokens.keys != null) {
							// TODO Check getter's return type for index or map
							// type
						}
					}
				} else {
					IBeanProperty property = bean.getProperty(propertyName);
					if (!Introspector.isValidPropertyName(propertyName)) {
						BeansModelUtils.createProblemMarker(bean,
								   "Invalid property name '" +
								   propertyName + "' - not JavaBean compliant",
								   IMarker.SEVERITY_ERROR, (property != null ?
						 					   property.getElementStartLine() :
						 					   bean.getElementStartLine()),
						 IBeansProjectMarker.ERROR_CODE_INVALID_PROPERTY_NAME,
						 bean.getElementName(), propertyName);
					} else if (!Introspector.hasWritableProperty(type,
															   propertyName)) {
						BeansModelUtils.createProblemMarker(bean,
									"No setter found for property '" +
							 		propertyName + "' in class '" +
							 		type.getFullyQualifiedName() + "'",
							 		IMarker.SEVERITY_ERROR, (property != null ?
						 					   property.getElementStartLine() :
						 					   bean.getElementStartLine()),
						 IBeansProjectMarker.ERROR_CODE_NO_SETTER,
						 bean.getElementName(), propertyName);
					}
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateConfigReferences(IBeansConfig config,
				  IBeansConfigSet configSet, BeanDefinitionRegistry registry) {
		if (DEBUG) {
			System.out.println("Validating references of bean config '" +
							   config.getConfigPath() + "'");
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
					  			 "BeansConfigValidator.validateReferences",
								 config.getConfigPath()));
		// Validate references of all beans
		Iterator beans = config.getBeans().iterator();
		while (beans.hasNext()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IBean bean = (IBean) beans.next();

			// Validate parent bean reference (if any)
			try {
				registry.getBeanDefinition(bean.getElementName());
			} catch (BeansException e) {
				String beanName = "<NA>";
				if (e instanceof NoSuchBeanDefinitionException) {
					beanName = ((NoSuchBeanDefinitionException)
															  e).getBeanName();
				} else if (e instanceof BeanDefinitionStoreException) {
					if (e.getCause() instanceof NoSuchBeanDefinitionException) {
						beanName = ((NoSuchBeanDefinitionException)
												   e.getCause()).getBeanName();
					}
				}
				if (beanName.equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean,
						  "Bean name and parent bean name are the same",
						  IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_BEAN,
						  bean.getElementName(), beanName);
				} else if (configSet != null && !configSet.isIncomplete()) {
					BeansModelUtils.createProblemMarker(bean,
						  "Parent bean '" + beanName +
						  "' not found in config set '" +
						  configSet.getElementName() + "'",
						  IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_BEAN,
						  bean.getElementName(), beanName);
				}
			}

			validateBeanReferences(bean, registry);
		}

		// Validate references of all inner beans
		beans = config.getInnerBeans().iterator();
		while (beans.hasNext()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IBean bean = (IBean) beans.next();
			validateBeanReferences(bean, registry);
		}
	}

	protected void validateBeanReferences(IBean bean,
										  BeanDefinitionRegistry registry) {
		try {
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
							 registry.getBeanDefinition(bean.getElementName());

			// Validate referenced beans in indexed constructor argument values
			ConstructorArgumentValues cargs = bd.getConstructorArgumentValues();
			Iterator iter = cargs.getIndexedArgumentValues().entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int index = ((Integer) entry.getKey()).intValue();

				// Lookup corresponding model element (constructor argument) 
				Iterator cas = bean.getConstructorArguments().iterator();
				while (cas.hasNext()) {
					IBeanConstructorArgument carg = (IBeanConstructorArgument)
																	cas.next();
					if (carg.getIndex() == index) {
						ConstructorArgumentValues.ValueHolder valueHolder =
							  			(ConstructorArgumentValues.ValueHolder)
							  			entry.getValue();
						// Skip constructor arguments with null value
						if (valueHolder.getValue() != null) {
							validateBeanReferencesInValue(bean, carg,
											 valueHolder.getValue(), registry);
						}
						break;
					}
				}
			}

			// Validate referenced beans in generic constructor argument values
			iter = cargs.getGenericArgumentValues().iterator();
			while (iter.hasNext()) {
				ConstructorArgumentValues.ValueHolder valueHolder =
						   (ConstructorArgumentValues.ValueHolder) iter.next();

				// Lookup corresponding model element (constructor argument) 
				Iterator cas = bean.getConstructorArguments().iterator();
				while (cas.hasNext()) {
					IBeanConstructorArgument	carg =
									 (IBeanConstructorArgument) cas.next();
					if (carg.getType() == valueHolder.getType() &&
								   carg.getValue() == valueHolder.getValue()) {
						// Skip constructor arguments with null value
						if (valueHolder.getValue() != null) {
							validateBeanReferencesInValue(bean, carg,
											 valueHolder.getValue(), registry);
						}
						break;
					}
				}
			}

			// Validate referenced beans in bean properties
			PropertyValue[] props = bd.getPropertyValues().getPropertyValues();
			for (int i = 0; i < props.length; i++) {
				PropertyValue prop = props[i];
	
				// Lookup corresponding model element (property) 
				IModelElement element = bean.getProperty(prop.getName());
				if (element == null) {
					element = bean;
				}
				validateBeanReferencesInValue(bean, element, prop.getValue(),
											  registry);
			}

			// Validate factory bean and it's non-static factory method
			if (bd.getFactoryBeanName() != null) {
				if (bd.getFactoryMethodName() == null) {
					BeansModelUtils.createProblemMarker(bean,
							"A factory bean requires a factory method",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_NO_FACTORY_METHOD,
							bean.getElementName(), bd.getFactoryBeanName());
				} else {
					validateFactoryBean(bean, bd.getFactoryBeanName(),
										bd.getFactoryMethodName(), registry);
				}
			}

			// Validate depends-on beans
			if (bd.getDependsOn() != null) {
				String[] beanNames = bd.getDependsOn();
				for (int i = 0; i < beanNames.length; i++) {
					String beanName = beanNames[i];
					validateDependsOnBean(bean, beanName, registry);
				}
			}
		} catch (BeansException e) {
			// Ignore all exceptions
		}
	}

	protected void validateBeanReferencesInValue(IBean bean,
										   IModelElement element, Object value,
										   BeanDefinitionRegistry registry) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			try {
				AbstractBeanDefinition refBd = (AbstractBeanDefinition)
										  registry.getBeanDefinition(beanName);
				if (!refBd.hasBeanClass()) {
					BeansModelUtils.createProblemMarker(bean,
						"Invalid referenced bean '" + beanName + "'",
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_INVALID_REFERENCED_BEAN,
						bean.getElementName(), beanName);
				}
			} catch (NoSuchBeanDefinitionException e) {
			    
				// Display a warning if the bean ref contains a placeholder
				if (hasPlaceHolder(beanName)) {
                    BeansModelUtils.createProblemMarker(element,
    				  "Referenced bean '" + beanName + "' not found",
    				  IMarker.SEVERITY_WARNING,
    				  ((ISourceModelElement) element).getElementStartLine(),
    				  IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCED_BEAN,
    				  element.getElementName(), beanName);
                // Handle factory bean references
                } else if (isFactoryBeanReference(beanName)) {
                    String tempBeanName = beanName.replaceFirst(FACTORY_BEAN_REFERENCE_REGEXP, "");
                    try {
                        BeanDefinition def = registry.getBeanDefinition(tempBeanName);
                        String beanClassName = ((AbstractBeanDefinition) def).getBeanClassName();
                        if (beanClassName != null) {
                            IType type = BeansModelUtils.getJavaType(
                                         BeansModelUtils.getProject(bean).getProject(),
                                         beanClassName);
                            if (type != null) {
                                try {
                                    String[] interfaces = type.getSuperInterfaceNames();
                                    if (interfaces != null && interfaces.length > 0) {
                                        if (!Arrays.asList(interfaces).contains(FactoryBean.class.getName())) {
                                            BeansModelUtils.createProblemMarker(element,
                                                    "Referenced factory bean '" + tempBeanName + 
                                                        "' does not implement the FactoryBean interface",
                                                    IMarker.SEVERITY_ERROR,
                                                    ((ISourceModelElement) element).getElementStartLine(),
                                                    IBeansProjectMarker.ERROR_CODE_INVALID_FACTORY_BEAN,
                                                    element.getElementName(), beanName);
                                        }
                                    }
                                } catch (JavaModelException me) {
                                    BeansCorePlugin.log(e);
                                }
                            }
                            else {
                                BeansModelUtils.createProblemMarker(element,
                                        "Referenced factory bean '" + tempBeanName + 
                                            "' implementation class not found",
                                        IMarker.SEVERITY_WARNING,
                                        ((ISourceModelElement) element).getElementStartLine(),
                                        IBeansProjectMarker.ERROR_CODE_INVALID_REFERENCED_BEAN,
                                        element.getElementName(), beanName);
                            }
                        }
                    } catch (NoSuchBeanDefinitionException be) {
                        BeansModelUtils.createProblemMarker(element,
                                "Referenced factory bean '" + tempBeanName + "' not found",
                                IMarker.SEVERITY_WARNING,
                                ((ISourceModelElement) element).getElementStartLine(),
                                IBeansProjectMarker.ERROR_CODE_UNDEFINED_FACTORY_BEAN,
                                element.getElementName(), beanName);
                    }
                }
                else {
                    BeansModelUtils.createProblemMarker(element,
                          "Referenced bean '" + beanName + "' not found",
                          IMarker.SEVERITY_WARNING,
                          ((ISourceModelElement) element).getElementStartLine(),
                          IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCED_BEAN,
                          element.getElementName(), beanName);
                }
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				validateBeanReferencesInValue(bean, element, list.get(i),
											  registry);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				validateBeanReferencesInValue(bean, element, iter.next(),
											  registry);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				validateBeanReferencesInValue(bean, element,
											  map.get(iter.next()), registry);
			}
		}
	}

	protected void validateMethod(IBean bean, IType type, int methodType,
						   String methodName, int argCount, boolean isStatic) {
		if (methodName != null &&  !hasPlaceHolder(methodName)) {
			try {
				if (Introspector.findMethod(type, methodName, argCount, true,
									(isStatic ? Introspector.STATIC_YES :
											Introspector.STATIC_NO)) == null) {
					switch (methodType) {
						case METHOD_TYPE_FACTORY :
							BeansModelUtils.createProblemMarker(bean,
									(isStatic ? "Static" : "Non-static") +
									" factory method '" + methodName + "' " +
									(argCount != -1 ? "with " + argCount +
											" arguments " : "") +
									"not found in factory bean class",
									IMarker.SEVERITY_ERROR,
									bean.getElementStartLine(),
									IBeansProjectMarker.ERROR_CODE_UNDEFINED_FACTORY_BEAN_METHOD,
									bean.getElementName(), methodName);
							break;

						case METHOD_TYPE_INIT :
							BeansModelUtils.createProblemMarker(bean,
									"Init-method '" + methodName +
									"' not found in bean class",
									IMarker.SEVERITY_ERROR,
									bean.getElementStartLine(),
									IBeansProjectMarker.ERROR_CODE_UNDEFINED_INIT_METHOD,
									bean.getElementName(), methodName);
							break;

						case METHOD_TYPE_DESTROY :
							BeansModelUtils.createProblemMarker(bean,
									"Destroy-method '" + methodName +
									"' not found in bean class",
									IMarker.SEVERITY_ERROR,
									bean.getElementStartLine(),
									IBeansProjectMarker.ERROR_CODE_UNDEFINED_DESTROY_METHOD,
									bean.getElementName(), methodName);
							break;
					}
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateFactoryBean(IBean bean, String beanName,
						  String methodName, BeanDefinitionRegistry registry) {
		if (beanName != null && !hasPlaceHolder(beanName)) {
			try {
				AbstractBeanDefinition factoryBd = (AbstractBeanDefinition)
										  registry.getBeanDefinition(beanName);
				if (!factoryBd.hasBeanClass()) {
					BeansModelUtils.createProblemMarker(bean,
						   "Invalid factory bean '" + beanName + "'",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					  	   IBeansProjectMarker.ERROR_CODE_INVALID_FACTORY_BEAN,
						   bean.getElementName(), beanName);
				} else {

					// Validate non-static factory method in factory bean
					// Factory beans with factory methods can only be validated
					// during runtime - so skip them
					if (factoryBd instanceof RootBeanDefinition &&
									factoryBd.getFactoryMethodName() == null) {
						validateFactoryMethod(bean,
											  factoryBd.getBeanClassName(),
											  methodName, -1, false);
					}
				}
			} catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean,
						 "Factory bean '" + beanName + "' not found",
						 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						 IBeansProjectMarker.ERROR_CODE_UNDEFINED_FACTORY_BEAN,
						 bean.getElementName(), beanName);
				}
			}
		}
	}

	protected void validateFactoryMethod(IBean bean, String className,
						   String methodName, int argCount, boolean isStatic) {
		if (className != null && !hasPlaceHolder(className)) {
			IType type = BeansModelUtils.getJavaType(
					 BeansModelUtils.getProject(bean).getProject(), className);
			if (type == null) {
				BeansModelUtils.createProblemMarker(bean,
							"Factory bean class '" + className + "' not found",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
							bean.getElementName(), className);
			} else {
				validateMethod(bean, type, METHOD_TYPE_FACTORY, methodName,
							   argCount, isStatic);
			}
		}
	}

	protected void validateDependsOnBean(IBean bean, String beanName,
										 BeanDefinitionRegistry registry) {
		if (beanName != null && !hasPlaceHolder(beanName)) {
			try {
				AbstractBeanDefinition dependsBd = (AbstractBeanDefinition)
										  registry.getBeanDefinition(beanName);
				if (!dependsBd.hasBeanClass()) {
					BeansModelUtils.createProblemMarker(bean,
						"Invalid depends-on bean '" + beanName + "'",
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_INVALID_DEPENDS_ON_BEAN,
						bean.getElementName(), beanName);
				}
			} catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean,
						"Depends-on bean '" +
						beanName + "' not found", IMarker.SEVERITY_ERROR,
						bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_UNDEFINED_DEPENDS_ON_BEAN,
						bean.getElementName(), beanName);
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if given text contains a placeholder, e.g.
	 * <code>${beansRef}</code>.
	 */
	private boolean hasPlaceHolder(String text) {
		int pos = text.indexOf(PLACEHOLDER_PREFIX);
		return (pos != -1 && text.indexOf(PLACEHOLDER_SUFFIX, pos) != -1);
	}

	/**
	 * Returns <code>true</code> if the specified text is a reference to a
	 * factory bean, e.g. <code>&factoryBean</code>.
	 */
	private boolean isFactoryBeanReference(String property) {
		return property.startsWith(FACTORY_BEAN_REFERENCE_PREFIX);
	}
    
	/**
	 * Determine the first (or last) nested property separator in the given
	 * property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @param last whether to return the last separator rather than the first
	 * @return the index of the nested property separator, or -1 if none
	 */
	private int getNestedPropertySeparatorIndex(String propertyPath,
												boolean last) {
		boolean inKey = false;
		int i = (last ? propertyPath.length() - 1 : 0);
		while ((last && i >= 0) || i < propertyPath.length()) {
			switch (propertyPath.charAt(i)) {
				case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
				case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
					inKey = !inKey;
					break;
				case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
					if (!inKey) {
						return i;
					}
			}
			if (last) {
				i--;
			} else {
				i++;
			}
		}
		return -1;
	}

	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		List keys = new ArrayList(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(
							PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName.indexOf(
							   PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart +
							   PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart +
						PropertyAccessor.PROPERTY_KEY_PREFIX.length(), keyEnd);
					if (key.startsWith("'") && key.endsWith("'")) {
						key = key.substring(1, key.length() - 1);
					} else if (key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd +
								 PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName += PropertyAccessor.PROPERTY_KEY_PREFIX +
								  StringUtils.collectionToDelimitedString(keys,
								  PropertyAccessor.PROPERTY_KEY_SUFFIX +
								  PropertyAccessor.PROPERTY_KEY_PREFIX) +
								  PropertyAccessor.PROPERTY_KEY_SUFFIX;
			tokens.keys = (String[]) keys.toArray(new String[keys.size()]);
		}
		return tokens;
	}

	private static class PropertyTokenHolder {

		private String canonicalName;

		private String actualName;

		private String[] keys;
	}
}
