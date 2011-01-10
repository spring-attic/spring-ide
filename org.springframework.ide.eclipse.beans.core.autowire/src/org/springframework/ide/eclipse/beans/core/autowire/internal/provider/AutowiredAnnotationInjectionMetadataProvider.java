/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.ide.eclipse.beans.core.autowire.IAutowireDependencyResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link IInjectionMetadataProvider} implementation that autowires annotated fields, setter methods and arbitrary
 * config methods. Such members to be injected are detected through a Java 5 annotation: by default, Spring's
 * {@link Autowired} annotation.
 * <p>
 * Only one constructor (at max) of any given bean class may carry this annotation with the 'required' parameter set to
 * <code>true</code>, indicating <i>the</i> constructor to autowire when used as a Spring bean. If multiple
 * <i>non-required</i> constructors carry the annotation, they will be considered as candidates for autowiring. The
 * constructor with the greatest number of dependencies that can be satisfied by matching beans in the Spring container
 * will be chosen. If none of the candidates can be satisfied, then a default constructor (if present) will be used. An
 * annotated constructor does not have to be public.
 * <p>
 * Fields are injected right after construction of a bean, before any config methods are invoked. Such a config field
 * does not have to be public.
 * <p>
 * Config methods may have an arbitrary name and any number of arguments; each of those arguments will be autowired with
 * a matching bean in the Spring container. Bean property setter methods are effectively just a special case of such a
 * general config method. Such config methods do not have to be public.
 * <p>
 * Also supports JSR-330's {@link javax.inject.Inject} annotation, if available.
 * <p>
 * Note: A default AutowiredAnnotationBeanPostProcessor will be registered by the "context:annotation-config" and
 * "context:component-scan" XML tags. Remove or turn off the default annotation configuration there if you intend to
 * specify a custom AutowiredAnnotationBeanPostProcessor bean definition.
 * @author Christian Dupuis
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see #setAutowiredAnnotationType
 * @see Autowired
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
public class AutowiredAnnotationInjectionMetadataProvider implements IInjectionMetadataProvider {

	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private String requiredParameterName = "required";

	private boolean requiredParameterValue = true;

	private final Map<Class<?>, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<Class<?>, InjectionMetadata>();

	private IInjectionMetadataProviderProblemReporter problemReporter = new PassThroughProblemReporter();
	
	/**
	 * Create a new AutowiredAnnotationBeanPostProcessor for Spring's standard {@link Autowired} annotation.
	 * <p>
	 * Also supports JSR-330's {@link javax.inject.Inject} annotation, if available.
	 */
	@SuppressWarnings("unchecked")
	public AutowiredAnnotationInjectionMetadataProvider(ClassLoader cl) {
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);
		try {
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Inject"));
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}

	/**
	 * Set the internally used {@link IInjectionMetadataProviderProblemReporter}. 
	 */
	public void setProblemReporter(IInjectionMetadataProviderProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	/**
	 * Set the 'autowired' annotation type, to be used on constructors, fields, setter methods and arbitrary config
	 * methods.
	 * <p>
	 * The default autowired annotation type is the Spring-provided {@link Autowired} annotation.
	 * <p>
	 * This setter property exists so that developers can provide their own (non-Spring-specific) annotation type to
	 * indicate that a member is supposed to be autowired.
	 */
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	/**
	 * Set the 'autowired' annotation types, to be used on constructors, fields, setter methods and arbitrary config
	 * methods.
	 * <p>
	 * The default autowired annotation type is the Spring-provided {@link Autowired} annotation, as well as
	 * {@link Value} and raw use of the {@link Qualifier} annotation.
	 * <p>
	 * This setter property exists so that developers can provide their own (non-Spring-specific) annotation types to
	 * indicate that a member is supposed to be autowired.
	 */
	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}

	/**
	 * Set the name of a parameter of the annotation that specifies whether it is required.
	 * @see #setRequiredParameterValue(boolean)
	 */
	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	/**
	 * Set the boolean value that marks a dependency as required
	 * <p>
	 * For example if using 'required=true' (the default), this value should be <code>true</code>; but if using
	 * 'optional=false', this value should be <code>false</code>.
	 * @see #setRequiredParameterName(String)
	 */
	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public InjectionMetadata findAutowiringMetadata(final Class<?> clazz) {
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
		if (metadata == null) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(clazz);
				if (metadata == null) {
					final InjectionMetadata newMetadata = new InjectionMetadata();
					ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) {
							Annotation annotation = findAutowiredAnnotation(field);
							if (annotation != null) {
								if (Modifier.isStatic(field.getModifiers())) {
									problemReporter.error("@Autowired annotation is not supported on static fields",
											field);
									return;
								}
								boolean required = determineRequiredStatus(annotation);
								newMetadata.addInjectedField(new AutowiredFieldElement(field, required));
							}
						}
					});
					ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
						public void doWith(Method method) {
							Annotation annotation = findAutowiredAnnotation(method);
							if (annotation != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
								boolean error = false;
								if (Modifier.isStatic(method.getModifiers())) {
									problemReporter.error("@Autowired annotation is not supported on static methods",
											method);
									error = true;
								}
								if (method.getParameterTypes().length == 0) {
									problemReporter.error("@Autowired annotation requires at least one argument",
											method);
									error = true;
								}
								if (!error) {
									boolean required = determineRequiredStatus(annotation);
									PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
									newMetadata.addInjectedMethod(new AutowiredMethodElement(clazz, method, required,
											pd));
								}
							}
						}
					});
					// add constructor
					Constructor<?>[] rawCandidates = clazz.getDeclaredConstructors();
					List<Constructor<?>> candidates = new ArrayList<Constructor<?>>(rawCandidates.length);
					Constructor<?> requiredConstructor = null;
					for (Constructor<?> candidate : rawCandidates) {
						Annotation annotation = findAutowiredAnnotation(candidate);
						if (annotation != null) {
							if (requiredConstructor != null) {
								problemReporter.error("Invalid @Autowire-marked constructor", candidate);
								problemReporter.error(
										"Found another constructor with 'required' @Autowired annotation",
										requiredConstructor);
								break;
							}
							if (candidate.getParameterTypes().length == 0) {
								problemReporter
										.error("@Autowired annotation requires at least one argument", candidate);
								break;
							}
							boolean required = determineRequiredStatus(annotation);
							if (required) {
								if (!candidates.isEmpty()) {
									for (Constructor<?> ctor : candidates) {
										problemReporter.error("Invalid @Autowire-marked constructor", ctor);
									}
									problemReporter.error(
											"Found another constructor with 'required' @Autowired annotation",
											requiredConstructor);
									break;
								}
								requiredConstructor = candidate;
							}
							newMetadata.addInjectedConstructor(new AutowiredConstructorElement(clazz, candidate,
									required));
							candidates.add(candidate);
						}
					}

					metadata = newMetadata;
					this.injectionMetadataCache.put(clazz, metadata);
				}
			}
		}
		return metadata;
	}

	private Annotation findAutowiredAnnotation(AccessibleObject ao) {
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			Annotation annotation = ao.getAnnotation(type);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Determine if the annotated field or method requires its dependency.
	 * <p>
	 * A 'required' dependency means that autowiring should fail when no beans are found. Otherwise, the autowiring
	 * process will simply bypass the field or method when no beans are found.
	 * @param annotation the Autowired annotation
	 * @return whether the annotation indicates that a dependency is required
	 */
	protected boolean determineRequiredStatus(Annotation annotation) {
		try {
			Method method = ReflectionUtils.findMethod(annotation.annotationType(), this.requiredParameterName);
			return (this.requiredParameterValue == (Boolean) ReflectionUtils.invokeMethod(method, annotation));
		}
		catch (Exception ex) {
			// required by default
			return true;
		}
	}

	/**
	 * Class representing injection information about an annotated field.
	 */
	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		protected DependencyDescriptor[] getDependencyDescriptor(IAutowireDependencyResolver resolver) {
			return new DependencyDescriptor[] { new DependencyDescriptor((Field) getMember(), this.required) };
		}
	}

	/**
	 * Class representing injection information about an annotated method.
	 */
	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private final Class<?> beanClass;

		public AutowiredMethodElement(Class<?> beanClass, Method method, boolean required, PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
			this.beanClass = beanClass;
		}

		@Override
		public DependencyDescriptor[] getDependencyDescriptor(IAutowireDependencyResolver resolver) {
			Method method = (Method) this.member;
			Class<?>[] paramTypes = method.getParameterTypes();
			DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				MethodParameter methodParam = new MethodParameter(method, i);
				GenericTypeResolver.resolveParameterType(methodParam, beanClass);
				descriptors[i] = new DependencyDescriptor(methodParam, this.required);
			}

			return descriptors;
		}

		@Override
		public boolean shouldSkip(BeanDefinition bd) {
			return checkPropertySkipping(bd.getPropertyValues());
		}
	}

	private class AutowiredConstructorElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private final Class<?> beanClass;

		public AutowiredConstructorElement(Class<?> beanClass, Constructor<?> ctor, boolean required) {
			super(ctor, null);
			this.required = required;
			this.beanClass = beanClass;
		}

		@Override
		public DependencyDescriptor[] getDependencyDescriptor(IAutowireDependencyResolver resolver) {
			Constructor<?> method = (Constructor<?>) this.member;
			Class<?>[] paramTypes = method.getParameterTypes();
			DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				MethodParameter methodParam = new MethodParameter(method, i);
				GenericTypeResolver.resolveParameterType(methodParam, beanClass);
				descriptors[i] = new DependencyDescriptor(methodParam, this.required);
			}

			return descriptors;
		}
	}

}
