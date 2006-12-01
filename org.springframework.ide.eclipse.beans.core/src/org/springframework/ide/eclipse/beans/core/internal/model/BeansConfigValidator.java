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

import java.util.LinkedHashSet;
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
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker.ErrorCode;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
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
		for (IBeansConfigSet configSet : ((IBeansProject) config
				.getElementParent()).getConfigSets()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (configSet.hasConfig(config.getElementName())) {
				DefaultBeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry(
						null);
				registry.setAllowAliasOverriding(false);
				registry.setAllowBeanDefinitionOverriding(configSet
						.isAllowBeanDefinitionOverriding());
				for (IBeansConfig cfg : configSet.getConfigs()) {
					if (cfg != null) {
						if (cfg.getElementName()
								.equals(config.getElementName())) {
							validateConfig(config, configSet, registry);
						} else {
							BeansModelUtils.registerBeanConfig(cfg, registry);
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
					"BeansConfigValidator.validateConfig", ModelUtils
							.getResourcePath(config)));
			if (DEBUG) {
				System.out.println("Validating config '"
						+ ModelUtils.getResourcePath(config) + "'");
			}
		} else {
			monitor.subTask(BeansCorePlugin.getFormattedMessage(
					"BeansConfigValidator.validateConfigSet", new String[] {
							ModelUtils.getResourcePath(config),
							configSet.getElementName() }));
			if (DEBUG) {
				System.out.println("Validating config '"
						+ ModelUtils.getResourcePath(config) + "' in set '"
						+ configSet.getElementName() + "'");
			}
		}

		// Validate all beans
		for (IBean bean : config.getBeans()) {
			validateBean(bean, configSet, registry);
		}

		// Finally validate all aliases
		for (IBeanAlias alias : config.getAliases()) {
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
			if (configSet == null
					|| BeansModelUtils.getConfig(alias).hasBean(
							alias.getElementName())) {
				BeansModelUtils.createProblemMarker(alias,
						"Overrides another bean in the same config file",
						IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						ErrorCode.BEAN_OVERRIDE, alias.getElementName(), null);
			} else if (!configSet.isAllowBeanDefinitionOverriding()) {
				BeansModelUtils.createProblemMarker(alias,
						"Overrides another bean in config set '"
								+ configSet.getElementName() + "'",
						IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						ErrorCode.BEAN_OVERRIDE, alias.getElementName(),
						configSet.getElementName());
			}
		}

		// Validate alias overriding within config file
		for (IBeanAlias al : BeansModelUtils.getConfig(alias).getAliases()) {
			if (al == alias) {
				break;
			} else if (al.getElementName().equals(alias.getElementName())) {
				BeansModelUtils.createProblemMarker(alias,
						"Overrides another alias in the same config file",
						IMarker.SEVERITY_ERROR, alias.getElementStartLine(),
						ErrorCode.ALIAS_OVERRIDE, alias.getElementName(),
						alias.getBeanName());
				break;
			}
		}

		// Validate alias within config set
		if (configSet != null) {

			// Validate alias overriding
			for (IBeansConfig config : configSet.getConfigs()) {
				if (config == BeansModelUtils.getConfig(alias)) {
					break;
				}
				if (config.getAlias(alias.getElementName()) != null) {
					BeansModelUtils.createProblemMarker(alias,
							"Overrides another alias in config set '"
									+ configSet.getElementName() + "'",
							IMarker.SEVERITY_ERROR,
							alias.getElementStartLine(),
							ErrorCode.ALIAS_OVERRIDE, alias.getElementName(),
							configSet.getElementName());
					break;
				}
			}
			
			// Check if corresponding bean exists
			if (!configSet.isIncomplete()
					&& !registry.containsBeanDefinition(alias.getBeanName())) {
				BeansModelUtils.createProblemMarker(alias, "Referenced bean '"
						+ alias.getBeanName() + "' not found in config set '"
						+ configSet.getElementName() + "'",
						IMarker.SEVERITY_WARNING, alias.getElementStartLine(),
						ErrorCode.UNDEFINED_REFERENCED_BEAN,
						alias.getElementName(), alias.getBeanName());
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
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		AbstractBeanDefinition mergedBd;
		if (configSet == null) {
			mergedBd = (AbstractBeanDefinition) BeansModelUtils
					.getMergedBeanDefinition(bean, BeansModelUtils
							.getConfig(bean));
		} else {
			mergedBd = (AbstractBeanDefinition) BeansModelUtils
					.getMergedBeanDefinition(bean, configSet);
		}

		// Validate bean definition
		try {
			bd.validate();
		} catch (BeanDefinitionValidationException e) {
			BeansModelUtils.createProblemMarker(bean,
					"Invalid bean definition: " + e.getMessage(),
					IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					ErrorCode.INVALID_BEAN_DEFINITION, bean.getElementName(),
					null);
		}

		// Get bean's merged bean definition and class name
		String className = bd.getBeanClassName();
		String mergedClassName = mergedBd.getBeanClassName();

		// Validate bean class and constructor arguments - skip child beans and
		// class names with placeholders
		if (className != null && !hasPlaceHolder(className)) {
			IType type = BeansModelUtils.getJavaType(BeansModelUtils
					.getProject(bean).getProject(), className);
			if (type == null) {
				BeansModelUtils.createProblemMarker(bean, "Class '" + className
						+ "' not found", IMarker.SEVERITY_ERROR,
						bean.getElementStartLine(), ErrorCode.CLASS_NOT_FOUND,
						bean.getElementName(), className);
			} else {

				// Validate merged constructor args of non-abstract beans only
				if (!bean.isAbstract()) {
					validateConstructorArguments(bean, type, mergedBd
							.getConstructorArgumentValues());
				}
			}
		}

		// Validate bean's constructor arguments, init-method, destroy-method
		// and properties with bean class from merged bean definition - skip
		// class names with placeholders
		if (mergedClassName != null && !hasPlaceHolder(mergedClassName)) {
			IType type = BeansModelUtils.getJavaType(BeansModelUtils
					.getProject(bean).getProject(), mergedClassName);
			if (type != null) {

				// Validate constructor args of non-abstract beans
				if (bd.hasConstructorArgumentValues() && !bean.isAbstract()) {
					validateConstructorArguments(bean, type, mergedBd
							.getConstructorArgumentValues());
				}

				// For non-factory beans validate bean's init-method and
				// destroy-method
				if (!Introspector.doesImplement(type, FactoryBean.class
						.getName())) {
					validateMethod(bean, type, METHOD_TYPE_INIT, bd
							.getInitMethodName(), 0, false);
					validateMethod(bean, type, METHOD_TYPE_DESTROY, bd
							.getDestroyMethodName(), 0, false);
				}

				// Validate bean's properties
				validateProperties(bean, type, bd.getPropertyValues());
			}
		}

		// Validate bean's static factory method with bean class from merged
		// bean definition - skip factory methods with placeholders or
		// factory beans
		String methodName = bd.getFactoryMethodName();
		if (methodName != null && !hasPlaceHolder(methodName)
				&& bd.getFactoryBeanName() == null) {
			if (mergedClassName == null) {
				if (!(bd instanceof ChildBeanDefinition)) {
					BeansModelUtils.createProblemMarker(bean,
							"Factory method needs class from root or parent bean",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							ErrorCode.BEAN_WITHOUT_CLASS_OR_PARENT,
							bean.getElementName(), null);
				}
			} else {

				// Use constructor argument values of root bean as arguments
				// for static factory method
				int argCount = (bd instanceof RootBeanDefinition
						&& !bd.isAbstract() ? bd.getConstructorArgumentValues()
						.getArgumentCount() : -1);
				validateFactoryMethod(bean, mergedClassName, methodName,
						argCount, true);
			}
		}
		
		// Validate this bean's inner beans recursively
		for (IBean innerBean :bean.getInnerBeans()) {
			validateBean(innerBean, configSet, registry);
		}
	}

	protected void validateBeanDefinitionHolder(IBean bean,
			IBeansConfigSet configSet, BeanDefinitionRegistry registry) {
		// Validate bean name
		try {
			registry.registerBeanDefinition(bean.getElementName(),
					((Bean) bean).getBeanDefinition());
		} catch (BeanDefinitionStoreException e) {
			if (configSet == null) {
				BeansModelUtils.createProblemMarker(bean,
						"Overrides another bean in the same config file",
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						ErrorCode.BEAN_OVERRIDE, bean.getElementName(), null);
			} else if (!configSet.isAllowBeanDefinitionOverriding()) {
				BeansModelUtils.createProblemMarker(bean,
						"Overrides another bean in config set '"
								+ configSet.getElementName() + "'",
						IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
						ErrorCode.BEAN_OVERRIDE, bean.getElementName(),
						configSet.getElementName());
			}
		}

		// Validate bean aliases
		if (bean.getAliases() != null) {
			for (String alias : bean.getAliases()) {
				try {
					registry.registerAlias(bean.getElementName(), alias);
				} catch (BeanDefinitionStoreException e) {
					BeansModelUtils.createProblemMarker(bean, e.getMessage(),
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							ErrorCode.INVALID_BEAN_ALIAS,
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
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		if (bd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO
				&& bd.getFactoryBeanName() == null
				&& bd.getFactoryMethodName() == null) {
			// Check for default constructor if no constructor arguments are
			// available
			int numArguments = (argumentValues == null ? 0 : argumentValues
					.getArgumentCount());
			try {
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element = BeansModelUtils
							.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}
					BeansModelUtils.createProblemMarker(bean,
							"No constructor with "
									+ numArguments
									+ (numArguments == 1 ? " argument"
											: " arguments")
									+ " defined in class '"
									+ type.getFullyQualifiedName() + "'",
							IMarker.SEVERITY_ERROR,
							element.getElementStartLine(),
							ErrorCode.NO_CONSTRUCTOR, bean.getElementName(),
							type.getFullyQualifiedName());
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
		for (PropertyValue propValue : propertyValues.getPropertyValues()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			String propertyName = propValue.getName();
			IBeanProperty property = bean.getProperty(propertyName);

			// Skip properties with placeholders
			if (hasPlaceHolder(propertyName)) {
				continue;
			}

			// Check for property accessor in given type
			try {

				// First check for nested property path
				int nestedIndex = getNestedPropertySeparatorIndex(propertyName,
						false);
				if (nestedIndex >= 0) {
					String nestedPropertyName = propertyName.substring(0,
							nestedIndex);
					PropertyTokenHolder tokens = getPropertyNameTokens(
							nestedPropertyName);
					String getterName = "get"
							+ StringUtils.capitalize(tokens.actualName);
					IMethod getter = Introspector.findMethod(type, getterName,
							0, true, Statics.NO);
					if (getter == null) {
						BeansModelUtils.createProblemMarker(bean,
								"No getter found for nested property '"
										+ nestedPropertyName + "' in class '"
										+ type.getFullyQualifiedName() + "'",
								IMarker.SEVERITY_ERROR,
								(property != null ? property
										.getElementStartLine() : bean
										.getElementStartLine()),
								ErrorCode.NO_GETTER, bean.getElementName(),
								propertyName);
					} else {

						// Check getter's return type
						if (tokens.keys != null) {
							// TODO Check getter's return type for index or map
							// type
						}
					}
				} else {

					// Now check for mapped property
					int mappedIndex = propertyName
							.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
					if (mappedIndex != -1) {
						propertyName = propertyName.substring(0, mappedIndex);
					}

					// Finally check property
					if (!Introspector.isValidPropertyName(propertyName)) {
						BeansModelUtils.createProblemMarker(bean,
								"Invalid property name '" + propertyName
										+ "' - not JavaBean compliant",
								IMarker.SEVERITY_ERROR,
								(property != null ? property
										.getElementStartLine() : bean
										.getElementStartLine()),
								ErrorCode.INVALID_PROPERTY_NAME, bean
										.getElementName(), propertyName);
					} else if (!Introspector.hasWritableProperty(type,
							propertyName)) {
						BeansModelUtils.createProblemMarker(bean,
								"No setter found for property '" + propertyName
										+ "' in class '"
										+ type.getFullyQualifiedName() + "'",
								IMarker.SEVERITY_ERROR,
								(property != null ? property
										.getElementStartLine() : bean
										.getElementStartLine()),
								ErrorCode.NO_SETTER, bean.getElementName(),
								propertyName);
					}

					// TODO If mapped property then check type of setter's
					// argument
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	protected void validateConfigReferences(IBeansConfig config,
			IBeansConfigSet configSet, BeanDefinitionRegistry registry) {
		if (DEBUG) {
			System.out.println("Validating references of bean config '"
					+ ModelUtils.getResourcePath(config) + "'");
		}
		monitor.subTask(BeansCorePlugin.getFormattedMessage(
				"BeansConfigValidator.validateReferences",
				ModelUtils.getResourcePath(config)));

		// Validate references of all beans
		for (IBean bean : config.getBeans()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			validateBeanReferences(bean, registry);
			
			// Validate references of all inner beans
			for (IBean innerBean : bean.getInnerBeans()) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				validateBeanReferences(innerBean, registry);
			}
		}
	}

	protected void validateBeanReferences(IBean bean,
			BeanDefinitionRegistry registry) {
		try {
			AbstractBeanDefinition bd = (AbstractBeanDefinition) registry
					.getBeanDefinition(bean.getElementName());
			ConstructorArgumentValues cargs = bd.getConstructorArgumentValues();

			// Validate referenced beans in indexed constructor argument values
			for (Object entry : cargs.getIndexedArgumentValues().entrySet()) {
				Map.Entry cargValue = (Map.Entry) entry;
				int index = ((Integer) cargValue.getKey()).intValue();

				// Lookup corresponding model element (constructor argument)
				for (IBeanConstructorArgument carg : bean
						.getConstructorArguments()) {
					if (carg.getIndex() == index) {
						ConstructorArgumentValues.ValueHolder valueHolder =
								(ConstructorArgumentValues.ValueHolder)
								cargValue.getValue();
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
			for (Object entry : cargs.getGenericArgumentValues()) {
				ConstructorArgumentValues.ValueHolder valueHolder =
						(ConstructorArgumentValues.ValueHolder) entry;

				// Lookup corresponding model element (constructor argument)
				for (IBeanConstructorArgument carg : bean
						.getConstructorArguments()) {
					if (carg.getType() == valueHolder.getType()
							&& carg.getValue() == valueHolder.getValue()) {
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
			for (PropertyValue propValue : bd.getPropertyValues()
					.getPropertyValues()) {

				// Lookup corresponding model element (property)
				ISourceModelElement element = bean.getProperty(propValue
						.getName());
				if (element == null) {
					element = bean;
				}
				validateBeanReferencesInValue(bean, element, propValue
						.getValue(), registry);
			}

			// Validate factory bean and it's non-static factory method
			if (bd.getFactoryBeanName() != null) {
				if (bd.getFactoryMethodName() == null) {
					BeansModelUtils.createProblemMarker(bean,
							"A factory bean requires a factory method",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							ErrorCode.NO_FACTORY_METHOD, bean.getElementName(),
							bd.getFactoryBeanName());
				} else {
					validateFactoryBean(bean, bd.getFactoryBeanName(),
							bd.getFactoryMethodName(), registry);
				}
			}

			// Validate depends-on beans
			if (bd.getDependsOn() != null) {
				for (String beanName : bd.getDependsOn()) {
					validateDependsOnBean(bean, beanName, registry);
				}
			}
		} catch (BeansException e) {
			// Ignore all exceptions
		}
	}

	protected void validateBeanReferencesInValue(IBean bean,
			ISourceModelElement element, Object value,
			BeanDefinitionRegistry registry) {
		if (value instanceof RuntimeBeanReference) {
			String beanName = ((RuntimeBeanReference) value).getBeanName();
			try {
				AbstractBeanDefinition refBd = (AbstractBeanDefinition) registry
						.getBeanDefinition(beanName);
				if (refBd.isAbstract()
						|| (refBd.getBeanClassName() == null && refBd
								.getFactoryBeanName() == null)) {
					BeansModelUtils.createProblemMarker(bean,
							"Referenced bean '" + beanName + "' is invalid "
							+ "(abstract or no bean class and no factory bean)",
							IMarker.SEVERITY_ERROR,
							element.getElementStartLine(),
							ErrorCode.INVALID_REFERENCED_BEAN,
							bean.getElementName(), beanName);
				}
			} catch (NoSuchBeanDefinitionException e) {
			    
				// Display a warning if the bean ref contains a placeholder
				if (hasPlaceHolder(beanName)) {
                    BeansModelUtils.createProblemMarker(element,
                    		"Referenced bean '" + beanName + "' not found",
                    		IMarker.SEVERITY_WARNING,
                    		((ISourceModelElement) element)
                    				.getElementStartLine(),
                    		ErrorCode.UNDEFINED_REFERENCED_BEAN,
                    		element.getElementName(), beanName);
                // Handle factory bean references
                } else if (isFactoryBeanReference(beanName)) {
					String tempBeanName = beanName.replaceFirst(
							FACTORY_BEAN_REFERENCE_REGEXP, "");
					try {
						BeanDefinition def = registry
								.getBeanDefinition(tempBeanName);
						String beanClassName = ((AbstractBeanDefinition) def)
								.getBeanClassName();
						if (beanClassName != null) {
							IType type = BeansModelUtils.getJavaType(
									BeansModelUtils.getProject(bean)
											.getProject(), beanClassName);
							if (type != null) {
								if (!Introspector.doesImplement(type,
										FactoryBean.class.getName())) {
									BeansModelUtils.createProblemMarker(
											element,
											"Referenced factory bean '"
											+ tempBeanName
											+ "' does not implement the FactoryBean interface",
											IMarker.SEVERITY_ERROR,
											((ISourceModelElement) element)
													.getElementStartLine(),
											ErrorCode.INVALID_FACTORY_BEAN,
											element.getElementName(),
											beanName);
								}
							} else {
								BeansModelUtils.createProblemMarker(
										element, "Referenced factory bean '"
										+ tempBeanName
										+ "' implementation class not found",
										IMarker.SEVERITY_WARNING,
										((ISourceModelElement) element)
												.getElementStartLine(),
										ErrorCode.INVALID_REFERENCED_BEAN,
										element.getElementName(), beanName);
							}
						}
					} catch (NoSuchBeanDefinitionException be) {
						BeansModelUtils.createProblemMarker(element,
								"Referenced factory bean '" + tempBeanName
										+ "' not found",
								IMarker.SEVERITY_WARNING,
								((ISourceModelElement) element)
										.getElementStartLine(),
								ErrorCode.UNDEFINED_FACTORY_BEAN,
								element.getElementName(), beanName);
					}
				} else {
					BeansModelUtils.createProblemMarker(element,
							"Referenced bean '" + beanName + "' not found",
							IMarker.SEVERITY_WARNING,
							((ISourceModelElement) element)
									.getElementStartLine(),
							ErrorCode.UNDEFINED_REFERENCED_BEAN, element
									.getElementName(), beanName);
				}
			}
		} else if (value instanceof List) {
			for (Object entry : (List) value) {
				validateBeanReferencesInValue(bean, element, entry, registry);
			}
		} else if (value instanceof Set) {
			for (Object entry : (Set) value) {
				validateBeanReferencesInValue(bean, element, entry, registry);
			}
		} else if (value instanceof Map) {
			for (Object entry : ((Map) value).values()) {
				validateBeanReferencesInValue(bean, element, entry, registry);
			}
		}
	}

	protected void validateMethod(IBean bean, IType type, int methodType,
			String methodName, int argCount, boolean isStatic) {
		if (methodName != null && !hasPlaceHolder(methodName)) {
			try {
				if (Introspector.findMethod(type, methodName, argCount, true,
						(isStatic ? Statics.YES : Statics.NO)) == null) {
					switch (methodType) {
					case METHOD_TYPE_FACTORY:
						BeansModelUtils.createProblemMarker(bean,
								(isStatic ? "Static" : "Non-static")
								+ " factory method '" + methodName + "' "
								+ (argCount != -1 ? "with " + argCount
										+ " arguments " : "")
								+ "not found in factory bean class",
								IMarker.SEVERITY_ERROR,
								bean.getElementStartLine(),
								ErrorCode.UNDEFINED_FACTORY_BEAN_METHOD,
								bean.getElementName(), methodName);
						break;

					case METHOD_TYPE_INIT:
						BeansModelUtils.createProblemMarker(bean,
								"Init-method '" + methodName
										+ "' not found in bean class",
								IMarker.SEVERITY_ERROR,
								bean.getElementStartLine(),
								ErrorCode.UNDEFINED_INIT_METHOD,
								bean.getElementName(), methodName);
						break;

					case METHOD_TYPE_DESTROY:
						BeansModelUtils.createProblemMarker(bean,
								"Destroy-method '" + methodName
										+ "' not found in bean class",
								IMarker.SEVERITY_ERROR,
								bean.getElementStartLine(),
								ErrorCode.UNDEFINED_DESTROY_METHOD,
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
				// Skip validating factory beans which are created by another
				// factory bean
				if (factoryBd.getFactoryBeanName() == null) {
					if (factoryBd.isAbstract()
							|| factoryBd.getBeanClassName() == null) {
						BeansModelUtils.createProblemMarker(bean,
								"Referenced factory bean '" + beanName
								+ "' is invalid (abstract or no bean class)",
								IMarker.SEVERITY_ERROR,
								bean.getElementStartLine(),
								ErrorCode.INVALID_FACTORY_BEAN,
								bean.getElementName(), beanName);
					} else {

						// Validate non-static factory method in factory bean
						// Factory beans with factory methods can only be
						// validated during runtime - so skip them
						if (factoryBd instanceof RootBeanDefinition
								&& factoryBd.getFactoryMethodName() == null) {
							validateFactoryMethod(bean, factoryBd
									.getBeanClassName(), methodName, -1,
									false);
						}
					}
				}
			} catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean, "Factory bean '"
							+ beanName + "' not found", IMarker.SEVERITY_ERROR,
							bean.getElementStartLine(),
							ErrorCode.UNDEFINED_FACTORY_BEAN,
							bean.getElementName(), beanName);
				}
			}
		}
	}

	protected void validateFactoryMethod(IBean bean, String className,
			String methodName, int argCount, boolean isStatic) {
		if (className != null && !hasPlaceHolder(className)) {
			IType type = BeansModelUtils.getJavaType(BeansModelUtils
					.getProject(bean).getProject(), className);
			// Skip factory-method validation for factory beans which are
			// Spring factory beans as well
			if (type != null
					&& !Introspector.doesImplement(type, FactoryBean.class
							.getName())) {
				validateMethod(bean, type, METHOD_TYPE_FACTORY, methodName,
						argCount, isStatic);
			}
		}
	}

	protected void validateDependsOnBean(IBean bean, String beanName,
			BeanDefinitionRegistry registry) {
		if (beanName != null && !hasPlaceHolder(beanName)) {
			try {
				AbstractBeanDefinition dependsBd = (AbstractBeanDefinition) registry
						.getBeanDefinition(beanName);
				if (dependsBd.isAbstract()
						|| (dependsBd.getBeanClassName() == null && dependsBd
								.getFactoryBeanName() == null)) {
					BeansModelUtils.createProblemMarker(bean,
							"Referenced depends-on bean '" + beanName
							+ "' is invalid (abstract or no bean class and no "
							+ "factory bean)", IMarker.SEVERITY_ERROR,
							bean.getElementStartLine(),
							ErrorCode.INVALID_DEPENDS_ON_BEAN,
							bean.getElementName(), beanName);
				}
			} catch (NoSuchBeanDefinitionException e) {

				// Skip error "parent name is equal to bean name"
				if (!e.getBeanName().equals(bean.getElementName())) {
					BeansModelUtils.createProblemMarker(bean,
							"Depends-on bean '" + beanName + "' not found",
							IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
							ErrorCode.UNDEFINED_DEPENDS_ON_BEAN,
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

	/**
	 * Parse the given property name into the corresponding property name
	 * tokens.
	 * 
	 * @param propertyName
	 *            the property name to parse
	 * @return representation of the parsed property tokens
	 */
	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		Set<String> keys = new LinkedHashSet<String>(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(
					PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName
						.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart
								+ PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart
							+ PropertyAccessor.PROPERTY_KEY_PREFIX.length(),
							keyEnd);
					if (key.startsWith("'") && key.endsWith("'")) {
						key = key.substring(1, key.length() - 1);
					} else if (key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd
							+ PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName += PropertyAccessor.PROPERTY_KEY_PREFIX
					+ StringUtils.collectionToDelimitedString(keys,
							PropertyAccessor.PROPERTY_KEY_SUFFIX
									+ PropertyAccessor.PROPERTY_KEY_PREFIX)
					+ PropertyAccessor.PROPERTY_KEY_SUFFIX;
			tokens.keys = keys.toArray(new String[keys.size()]);
		}
		return tokens;
	}

	private static class PropertyTokenHolder {

		private String canonicalName;

		private String actualName;

		private String[] keys;
	}
}
