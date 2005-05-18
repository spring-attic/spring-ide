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
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.beans.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.util.StringUtils;

public class BeansConfigValidator {

    private static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
    
    private static final String PROPERTY_PLACEHOLDER_SUFFIX = "}";
    
    private static final String FACTORY_BEAN_REFERENCE_PREFIX = "&";
    
    private static final String FACTORY_BEAN_REFERENCE_REGEXP = "[&]";
    
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

				IBeansProject project = (IBeansProject) configSet.getElementParent();
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
					validateReferences(config, configSet, registry);
				}
				isValidated = true;
			}
		}

		// If not already validated then validate config file now
		if (!isValidated) {
			DefaultBeanDefinitionRegistry registry =
									   new DefaultBeanDefinitionRegistry(null); 
			registry.setAllowAliasOverriding(false);
			validateConfig(config, null, registry);
			validateReferences(config, null, registry);
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
    	}

	protected void validateBean(IBean bean, IBeansConfigSet configSet,
								BeanDefinitionRegistry registry) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
				   "BeansConfigValidator.validateBean", bean.getElementName()));
		// Validate bean overriding
		if (configSet != null &&
					  !configSet.isAllowBeanDefinitionOverriding() &&
					  registry.containsBeanDefinition(bean.getElementName())) {
			BeansModelUtils.createProblemMarker(bean,
						"Overrides another bean within config set '" +
						configSet.getElementName() + "'", IMarker.SEVERITY_ERROR,
						bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
						bean.getElementName(), configSet.getElementName());
		}
		
		// Validate bean name and aliases
		validateBeanDefinitionHolder(bean, registry);

		// Validate root bean
		BeanDefinitionHolder bdHolder = ((Bean) bean).getBeanDefinitionHolder();
		BeanDefinition bd = bdHolder.getBeanDefinition();
		if (bd instanceof RootBeanDefinition) {
	
			// Validate bean definition
			try {
				((RootBeanDefinition) bd).validate();
			} catch (BeanDefinitionValidationException e) {
				BeansModelUtils.createProblemMarker(bean,
						"Invalid bean definition: " + e.getMessage(),
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						IBeansProjectMarker.ERROR_CODE_INVALID_BEAN_DEFINITION,
						bean.getElementName(), null);
			}

			// If bean's Java type available then valdiate bean's constructor
			// arguments and properties
			String className = ((RootBeanDefinition) bd).getBeanClassName();
			if (className != null) {
				IType type = BeansModelUtils.getJavaType(
							 bean.getConfig().getConfigFile().getProject(),
							 className);
				if (type == null) {
					if (!bean.isAbstract()) {
						BeansModelUtils.createProblemMarker(bean,
							 "Class '" + className + "' not found",
							 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							 IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
							 bean.getElementName(), className);
					}
				} else {
					// Only validate constructor arguments of non-abstract beans
					if (!bean.isAbstract()) {
						validateConstructorArguments(bean, type,
											bd.getConstructorArgumentValues());
					}
					validateProperties(bean, type, bd.getPropertyValues());
				}
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
											 BeanDefinitionRegistry registry) {
		BeanDefinitionHolder bdHolder = ((Bean) bean).getBeanDefinitionHolder();
		AbstractBeanDefinition bd = (AbstractBeanDefinition)
												  bdHolder.getBeanDefinition();
		// Validate bean name
		try {
			registry.registerBeanDefinition(bdHolder.getBeanName(), bd);
		} catch (BeanDefinitionStoreException e) {
			BeansModelUtils.createProblemMarker(bean, e.getMessage(),
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_BEAN_OVERRIDE,
							bean.getElementName(), null);
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

		// Validate static factory method in this bean
		if (bd.getBeanClassName() != null &&
										   bd.getFactoryMethodName() != null &&
										   bd.getFactoryBeanName() == null) {
			validateFactoryMethod(bean, bd.getBeanClassName(),
								  bd.getFactoryMethodName(), true, registry);
		}
	}

	protected void validateFactoryBean(IBean bean, String beanName,
						  String methodName, BeanDefinitionRegistry registry) {
		try {
			AbstractBeanDefinition factoryBd = (AbstractBeanDefinition)
										  registry.getBeanDefinition(beanName);
			if (factoryBd.isAbstract() ||
										factoryBd.getBeanClassName() == null) {
				BeansModelUtils.createProblemMarker(bean,
						   "Invalid factory bean '" + beanName + "'",
						   IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					  	   IBeansProjectMarker.ERROR_CODE_INVALID_FACTORY_BEAN,
						   bean.getElementName(), beanName);
			} else {
				validateFactoryMethod(bean, factoryBd.getBeanClassName(),
									  methodName, false, registry);
			}
		} catch (NoSuchBeanDefinitionException e) {
			if (e.getBeanName().equals(bean.getElementName())) {
				BeansModelUtils.createProblemMarker(bean,
						 "Bean name and factory bean name are the same",
						 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						 IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_BEAN,
						 bean.getElementName(), e.getBeanName());
			} else {
				BeansModelUtils.createProblemMarker(bean,
						 "Factory bean '" + beanName + "' not found",
						 IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						 IBeansProjectMarker.ERROR_CODE_UNDEFINED_FACTORY_BEAN,
						 bean.getElementName(), beanName);
			}
		}
	}

	protected void validateFactoryMethod(IBean bean, String className,
										String methodName, boolean isStatic,
										BeanDefinitionRegistry registry) {
		IType type = BeansModelUtils.getJavaType(
					 bean.getConfig().getConfigFile().getProject(), className);
		if (type == null) {
			BeansModelUtils.createProblemMarker(bean,
							"Factory bean class '" + className + "' not found",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_CLASS_NOT_FOUND,
							bean.getElementName(), className);
		} else {
			try {
				if (Introspector.findMethod(type, methodName, -1, true,
											isStatic) == null) {
					BeansModelUtils.createProblemMarker(bean,
							(isStatic ? "Static" : "Instance") +
							" factory method '" + methodName +
							"' in factory bean class '" + className +
							"' not found", IMarker.SEVERITY_ERROR,
							bean.getElementStartLine(),
							IBeansProjectMarker.ERROR_CODE_UNDEFINED_FACTORY_BEAN_METHOD,
							bean.getElementName(), methodName);
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateConstructorArguments(IBean bean, IType type,
									ConstructorArgumentValues argumentValues) {
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
						   "BeansConfigValidator.validateConstructorArguments",
						   bean.getElementName()));
		// Skip validation if default constructor or factory bean
		int numArguments = argumentValues.getArgumentCount();
		BeanDefinitionHolder bdHolder = ((Bean)
											   bean).getBeanDefinitionHolder();
		AbstractBeanDefinition bd = (AbstractBeanDefinition)
												  bdHolder.getBeanDefinition();
		if (numArguments > 0 && bd.getFactoryBeanName() == null &&
										   bd.getFactoryMethodName() == null) {
			try {
				if (!Introspector.hasConstructor(type, numArguments)) {
					BeansModelUtils.createProblemMarker(bean,
						"No public constructor with " + numArguments +
						(numArguments == 1 ? " argument" : " arguments") +
						" defined in class '" + type.getFullyQualifiedName() +
						"'", IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
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
															 0, true, false);
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
					if (!Introspector.hasWritableProperty(type, propertyName)) {
						IBeanProperty property = bean.getProperty(propertyName);
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

	protected void validateReferences(IBeansConfig config,
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
			} catch (NoSuchBeanDefinitionException e) {
				if (e.getBeanName().equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean,
							  "Bean name and parent bean name are the same",
							  IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_BEAN,
							  bean.getElementName(), e.getBeanName());
				} else if (configSet != null && !configSet.isIncomplete()) {
					BeansModelUtils.createProblemMarker(bean,
							  "Parent bean '" + e.getBeanName() +
							  "' not found in config set '" +
							  configSet.getElementName() + "'",
							  IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							  IBeansProjectMarker.ERROR_CODE_UNDEFINED_PARENT_BEAN,
							  bean.getElementName(), e.getBeanName());
				}
			}

			// Validate bean references in this bean
			validateRefBeansInBean(bean, registry);

			// Validate non-static factory method in factory bean
			BeanDefinitionHolder bdHolder = ((Bean)
											   bean).getBeanDefinitionHolder();
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
												  bdHolder.getBeanDefinition();
			if (bd.getFactoryBeanName() != null &&
										   bd.getFactoryMethodName() != null) {
				validateFactoryBean(bean, bd.getFactoryBeanName(),
									bd.getFactoryMethodName(), registry);
			}
		}

		// Validate references of all inner beans
		beans = config.getInnerBeans().iterator();
		while (beans.hasNext()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			IBean bean = (IBean) beans.next();
			validateRefBeansInBean(bean, registry);

			// Validate non-static factory method in factory bean
			BeanDefinitionHolder bdHolder = ((Bean)
											   bean).getBeanDefinitionHolder();
			AbstractBeanDefinition bd = (AbstractBeanDefinition)
												  bdHolder.getBeanDefinition();
			if (bd.getFactoryBeanName() != null &&
										   bd.getFactoryMethodName() != null) {
				validateFactoryBean(bean, bd.getFactoryBeanName(),
									bd.getFactoryMethodName(), registry);
			}
		}
	}

	protected void validateRefBeansInBean(IBean bean,
										  BeanDefinitionRegistry registry) {
		try {
			BeanDefinition bd = registry.getBeanDefinition(
														bean.getElementName());
			// Validate referenced beans in indexed constructor argument values
			ConstructorArgumentValues cargs = bd.getConstructorArgumentValues();
			Iterator iter = cargs.getIndexedArgumentValues().entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int index = ((Integer) entry.getKey()).intValue();
	
				// Lookup corresponding model element (contructor argument) 
				IModelElement element = bean;
				Iterator cas = bean.getConstructorArguments().iterator();
				while (cas.hasNext()) {
					IBeanConstructorArgument	carg = (IBeanConstructorArgument)
																	cas.next();
					if (carg.getIndex() == index) {
						element = carg;
						break;
					}
				}
				ConstructorArgumentValues.ValueHolder valueHolder =
					  (ConstructorArgumentValues.ValueHolder) entry.getValue();
				validateRefBeansInValue(bean, element, valueHolder.getValue(), registry);
			}

			// Validate referenced beans in generic constructor argument values
			iter = cargs.getGenericArgumentValues().iterator();
			while (iter.hasNext()) {
				ConstructorArgumentValues.ValueHolder valueHolder =
						   (ConstructorArgumentValues.ValueHolder) iter.next();

				// Lookup corresponding model element (contructor argument) 
				IModelElement element = bean;
				if (valueHolder.getType() != null) {
					Iterator cas = bean.getConstructorArguments().iterator();
					while (cas.hasNext()) {
						IBeanConstructorArgument	carg =
										 (IBeanConstructorArgument) cas.next();
						if (carg.getType() == valueHolder.getType()) {
							element = carg;
							break;
						}
					}
				}
				validateRefBeansInValue(bean, element, valueHolder.getValue(),
										registry);
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
				validateRefBeansInValue(bean, element, prop.getValue(), registry);
			}
		} catch (NoSuchBeanDefinitionException e) {
			// Ignore all exceptions
		}
	}

	protected void validateRefBeansInValue(IBean bean, IModelElement element,
							   Object value, BeanDefinitionRegistry registry) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			try {
				registry.getBeanDefinition(beanName);
			} catch (NoSuchBeanDefinitionException e) {
			    
				// Display a warning if the bean ref is a property placeholder
				if (isPropertyPlaceHolder(beanName)) {
                    BeansModelUtils.createProblemMarker(element,
    					  "Referenced bean '" + beanName + "' not found",
    					  IMarker.SEVERITY_WARNING,
    					  ((ISourceModelElement) element).getElementStartLine(),
    					  IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
    					  element.getElementName(), beanName);
                // Handle factory bean references
                } else if (isFactoryBeanReference(beanName)) {
                    String tempBeanName = beanName.replaceFirst(FACTORY_BEAN_REFERENCE_REGEXP, "");
                    try {
                        BeanDefinition def = registry.getBeanDefinition(tempBeanName);
                        String beanClassName = ((AbstractBeanDefinition) def).getBeanClassName();
                        if (beanClassName != null) {
                            IType type = BeansModelUtils.getJavaType(
                                         bean.getConfig().getConfigFile().getProject(),
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
                                                    IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
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
                                        IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
                                        element.getElementName(), beanName);
                            }
                        }
                    } catch (NoSuchBeanDefinitionException be) {
                        BeansModelUtils.createProblemMarker(element,
                                "Referenced factory bean '" + tempBeanName + "' not found",
                                IMarker.SEVERITY_ERROR,
                                ((ISourceModelElement) element).getElementStartLine(),
                                IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
                                element.getElementName(), beanName);
                    }
                }
                else {
                    BeansModelUtils.createProblemMarker(element,
                          "Referenced bean '" + beanName + "' not found",
                          IMarker.SEVERITY_ERROR,
                          ((ISourceModelElement) element).getElementStartLine(),
                          IBeansProjectMarker.ERROR_CODE_UNDEFINED_REFERENCE,
                          element.getElementName(), beanName);
                }
			}
		} else if (value instanceof List) {
			List list = (List) value;
			for (int i = 0; i < list.size(); i++) {
				validateRefBeansInValue(bean, element, list.get(i), registry);
			}
		} else if (value instanceof Set) {
			Set set = (Set) value;
			for (Iterator iter = set.iterator(); iter.hasNext(); ) {
				validateRefBeansInValue(bean, element, iter.next(), registry);
			}
		} else if (value instanceof Map) {
			Map map = (Map) value;
			for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
				validateRefBeansInValue(bean, element, map.get(iter.next()),
										registry);
			}
		}
	}

    /**
     * Checks if the specified property value is a placeholder,
     * e.g. <code>${beansRef}</code>
     * @param property the property value
     * @return true if property value is placeholder
     */
    private boolean isPropertyPlaceHolder(String property) {
        return (property.startsWith(PROPERTY_PLACEHOLDER_PREFIX) 
                && property.endsWith(PROPERTY_PLACEHOLDER_SUFFIX));
    }
    
    /**
     * Checks if the specified property value is a reference to a factory bean,
     * e.g. <code>&factoryBean</code>
     * @param property the property value
     * @return true if property value is placeholder
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
