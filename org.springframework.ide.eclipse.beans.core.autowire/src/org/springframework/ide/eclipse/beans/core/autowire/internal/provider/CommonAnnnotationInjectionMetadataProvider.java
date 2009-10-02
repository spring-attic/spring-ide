/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.Service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ide.eclipse.beans.core.autowire.AutowireBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.IAutowireDependencyResolver;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link IInjectionMetadataProvider} implementation that supports common Java annotations out of the box, in particular
 * the JSR-250 annotations in the <code>javax.annotation</code> package. These common Java annotations are supported in
 * many Java EE 5 technologies (e.g. JSF 1.2), as well as in Java 6's JAX-WS.
 * 
 * <p>
 * The central element is the {@link javax.annotation.Resource} annotation for annotation-driven injection of named
 * beans, by default from the containing Spring BeanFactory, with only <code>mappedName</code> references resolved in
 * JNDI. The {@link #setAlwaysUseJndiLookup "alwaysUseJndiLookup" flag} enforces JNDI lookups equivalent to standard
 * Java EE 5 resource injection for <code>name</code> references and default names as well. The target beans can be
 * simple POJOs, with no special requirements other than the type having to match.
 * 
 * <p>
 * The JAX-WS {@link javax.xml.ws.WebServiceRef} annotation is supported too, analogous to
 * {@link javax.annotation.Resource} but with the capability of creating specific JAX-WS service endpoints. This may
 * either point to an explicitly defined resource by name or operate on a locally specified JAX-WS service class.
 * Finally, this post-processor also supports the EJB 3 {@link javax.ejb.EJB} annotation, analogous to
 * {@link javax.annotation.Resource} as well, with the capability to specify both a local bean name and a global JNDI
 * name for fallback retrieval. The target beans can be plain POJOs as well as EJB 3 Session Beans in this case.
 * 
 * <p>
 * The common annotations supported by this post-processor are available in Java 6 (JDK 1.6) as well as in Java EE 5
 * (which provides a standalone jar for its common annotations as well, allowing for use in any Java 5 based
 * application). Hence, this post-processor works out of the box on JDK 1.6, and requires the JSR-250 API jar (and
 * optionally the JAX-WS API jar and/or the EJB 3 API jar) to be added to the classpath on JDK 1.5 (when running outside
 * of Java EE 5).
 * 
 * <p>
 * For default usage, resolving resource names as Spring bean names, simply define the following in your application
 * context:
 * 
 * <pre class="code">
 * &lt;bean class=&quot;org.springframework.context.annotation.CommonAnnotationBeanPostProcessor&quot;/&gt;
 * </pre>
 * 
 * For direct JNDI access, resolving resource names as JNDI resource references within the Java EE application's
 * "java:comp/env/" namespace, use the following:
 * 
 * <pre class="code">
 * &lt;bean class=&quot;org.springframework.context.annotation.CommonAnnotationBeanPostProcessor&quot;&gt;
 *   &lt;property name=&quot;alwaysUseJndiLookup&quot; value=&quot;true&quot;/&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * <code>mappedName</code> references will always be resolved in JNDI, allowing for global JNDI names (including "java:"
 * prefix) as well. The "alwaysUseJndiLookup" flag just affects <code>name</code> references and default names (inferred
 * from the field name / property name).
 * 
 * <p>
 * <b>NOTE:</b> A default CommonAnnotationBeanPostProcessor will be registered by the "context:annotation-config" and
 * "context:component-scan" XML tags. Remove or turn off the default annotation configuration there if you intend to
 * specify a custom CommonAnnotationBeanPostProcessor bean definition!
 * 
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.2.7
 */
@SuppressWarnings("unchecked")
public class CommonAnnnotationInjectionMetadataProvider implements IInjectionMetadataProvider {

	private static String webServiceRefClassName = "javax.xml.ws.WebServiceRef";

	private static String ejbRefClassName = "javax.ejb.EJB";

	private static String resourceClassName = "javax.annotation.Resource";

	private final Set<String> ignoredResourceTypes = new HashSet<String>(1);

	private boolean fallbackToDefaultTypeMatch = true;

	private IInjectionMetadataProviderProblemReporter problemReporter = new PassThroughProblemReporter();

	private transient final Map<Class<?>, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<Class<?>, InjectionMetadata>();

	public CommonAnnnotationInjectionMetadataProvider() {
		ignoreResourceType("javax.xml.ws.WebServiceContext");
	}

	/**
	 * Set the internally {@link IInjectionMetadataProviderProblemReporter}. 
	 */
	public void setProblemReporter(IInjectionMetadataProviderProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	/**
	 * Ignore the given resource type when resolving <code>@Resource</code> annotations.
	 * <p>
	 * By default, the <code>javax.xml.ws.WebServiceContext</code> interface will be ignored, since it will be resolved
	 * by the JAX-WS runtime.
	 * @param resourceType the resource type to ignore
	 */
	public void ignoreResourceType(String resourceType) {
		Assert.notNull(resourceType, "Ignored resource type must not be null");
		this.ignoredResourceTypes.add(resourceType);
	}

	/**
	 * {@inheritDoc}
	 */
	public InjectionMetadata findAutowiringMetadata(final Class<?> clazz) {
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(clazz);
		if (metadata == null) {
			synchronized (this.injectionMetadataCache) {
				Class<? extends Annotation> annotationClass = null;

				// Load the annotation classes in the same class loader context as the user clazz
				try {
					annotationClass = (Class<? extends Annotation>) clazz.getClassLoader().loadClass(resourceClassName);
				}
				catch (ClassNotFoundException e) {
					annotationClass = null;
				}
				final Class<? extends Annotation> resourceClass = annotationClass;

				try {
					annotationClass = (Class<? extends Annotation>) clazz.getClassLoader().loadClass(
							webServiceRefClassName);
				}
				catch (ClassNotFoundException e) {
					annotationClass = null;
				}
				final Class<? extends Annotation> webServiceRefClass = annotationClass;

				try {
					annotationClass = (Class<? extends Annotation>) clazz.getClassLoader().loadClass(ejbRefClassName);
				}
				catch (ClassNotFoundException e) {
					annotationClass = null;
				}
				final Class<? extends Annotation> ejbRefClass = annotationClass;

				metadata = this.injectionMetadataCache.get(clazz);
				if (metadata == null) {
					final InjectionMetadata newMetadata = new InjectionMetadata();
					ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) {
							if (webServiceRefClass != null && field.isAnnotationPresent(webServiceRefClass)) {
								if (Modifier.isStatic(field.getModifiers())) {
									problemReporter.error(
											"@WebServiceRef annotation is not supported on static fields", field);
									return;
								}
								newMetadata.addInjectedField(new WebServiceRefElement(field, null, webServiceRefClass));
							}
							else if (ejbRefClass != null && field.isAnnotationPresent(ejbRefClass)) {
								if (Modifier.isStatic(field.getModifiers())) {
									problemReporter.error("@EJB annotation is not supported on static fields", field);
									return;
								}
								newMetadata.addInjectedField(new EjbRefElement(field, null, ejbRefClass));
							}
							else if (field.isAnnotationPresent(resourceClass)) {
								if (Modifier.isStatic(field.getModifiers())) {
									problemReporter.error("@Resource annotation is not supported on static fields",
											field);
									return;
								}
								if (!ignoredResourceTypes.contains(field.getType().getName())) {
									newMetadata.addInjectedField(new ResourceElement(field, null, resourceClass));
								}
							}
						}
					});
					ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
						public void doWith(Method method) {
							if (webServiceRefClass != null && method.isAnnotationPresent(webServiceRefClass)
									&& method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
								if (Modifier.isStatic(method.getModifiers())) {
									problemReporter.error(
											"@WebServiceRef annotation is not supported on static methods", method);
									return;
								}
								if (method.getParameterTypes().length != 1) {
									problemReporter.error("@WebServiceRef annotation requires a single-arg method",
											method);
									return;
								}
								PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
								newMetadata.addInjectedMethod(new WebServiceRefElement(method, pd, webServiceRefClass));
							}
							else if (ejbRefClass != null && method.isAnnotationPresent(ejbRefClass)
									&& method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
								if (Modifier.isStatic(method.getModifiers())) {
									problemReporter.error("@EJB annotation is not supported on static methods", method);
									return;
								}
								if (method.getParameterTypes().length != 1) {
									problemReporter.error("@EJB annotation requires a single-arg method", method);
									return;
								}
								PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
								newMetadata.addInjectedMethod(new EjbRefElement(method, pd, ejbRefClass));
							}
							else if (method.isAnnotationPresent(resourceClass)
									&& method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
								if (Modifier.isStatic(method.getModifiers())) {
									problemReporter.error("@Resource annotation is not supported on static methods",
											method);
									return;
								}
								Class<?>[] paramTypes = method.getParameterTypes();
								if (paramTypes.length != 1) {
									problemReporter.error("@Resource annotation requires a single-arg method", method);
									return;
								}
								if (!ignoredResourceTypes.contains(paramTypes[0].getName())) {
									PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
									newMetadata.addInjectedMethod(new ResourceElement(method, pd, resourceClass));
								}
							}
						}
					});
					metadata = newMetadata;
					this.injectionMetadataCache.put(clazz, metadata);
				}
			}
		}
		return metadata;
	}

	/**
	 * Class<?> representing generic injection information about an annotated field or setter method, supporting @Resource
	 * and related annotations.
	 */
	protected abstract class LookupElement extends InjectionMetadata.InjectedElement {

		protected String name;

		protected boolean isDefaultName = false;

		protected Class<?> lookupType;

		protected String mappedName;

		public LookupElement(Member member, PropertyDescriptor pd, Class<? extends Annotation> annotationClass) {
			super(member, pd);
			initAnnotation((AnnotatedElement) member, annotationClass);
		}

		protected abstract void initAnnotation(AnnotatedElement ae, Class<? extends Annotation> annotationClass);

		/**
		 * Return the resource name for the lookup.
		 */
		public final String getName() {
			return this.name;
		}

		/**
		 * Return the desired type for the lookup.
		 */
		public final Class<?> getLookupType() {
			return this.lookupType;
		}

		public Set<IBeanReference> getBeanReferences(IBean bean, IBeansModelElement context,
				AutowireDependencyProvider provider) {
			BeanDefinition bd = BeansModelUtils.getMergedBeanDefinition(bean, context);

			if (!shouldSkip(bd)) {
				if (fallbackToDefaultTypeMatch && isDefaultName && !provider.containsBean(name)) {
					return super.getBeanReferences(bean, context, provider);
				}
				else {
					String[] matchingBeans = provider.getBeansForType(lookupType);
					for (String matchingBen : matchingBeans) {
						if (name.equals(matchingBen) || Arrays.asList(provider.getAliases(matchingBen)).contains(name)) {
							IBeanReference ref = new AutowireBeanReference(bean, new RuntimeBeanReference(matchingBen));
							if (getMember() instanceof Field) {
								((AutowireBeanReference) ref).setSource((Field) getMember());
							}
							else {
								((AutowireBeanReference) ref).setSource(getMember(), 0);
							}
							return Collections.singleton(ref);
						}
					}
				}
			}
			return Collections.emptySet();
		}

		protected DependencyDescriptor[] getDependencyDescriptor(IAutowireDependencyResolver resolver) {
			if (getMember() instanceof Field) {
				return new DependencyDescriptor[] { new DependencyDescriptor((Field) getMember(), true) };
			}
			else {
				Method method = (Method) this.member;
				Class<?>[] paramTypes = method.getParameterTypes();
				DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];

				for (int i = 0; i < paramTypes.length; i++) {
					MethodParameter methodParam = new MethodParameter(method, i);
					descriptors[i] = new DependencyDescriptor(methodParam, true);
				}
				return descriptors;
			}
		}

		@Override
		public boolean shouldSkip(BeanDefinition bd) {
			return checkPropertySkipping(bd.getPropertyValues());
		}

	}

	/**
	 * Class representing injection information about an annotated field or setter method, supporting the @Resource
	 * annotation.
	 */
	private class ResourceElement extends LookupElement {

		public ResourceElement(Member member, PropertyDescriptor pd, Class<? extends Annotation> annotationClass) {
			super(member, pd, annotationClass);
		}

		@Override
		protected void initAnnotation(AnnotatedElement ae, Class<? extends Annotation> annotationClass) {
			Annotation resource = ae.getAnnotation(annotationClass);
			String resourceName = (String) AnnotationUtils.getValue(resource, "name");
			Class<?> resourceType = (Class<?>) AnnotationUtils.getValue(resource, "type");
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				resourceName = this.member.getName();
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			if (resourceType != null && !Object.class.equals(resourceType)) {
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method.
				resourceType = getResourceType();
			}
			this.name = resourceName;
			this.lookupType = resourceType;
			this.mappedName = (String) AnnotationUtils.getValue(resource, "mappedName");
		}

		@Override
		public Set<IBeanReference> getBeanReferences(IBean bean, IBeansModelElement context,
				IAutowireDependencyResolver resolver) {
			BeanDefinition bd = BeansModelUtils.getMergedBeanDefinition(bean, context);

			if (!shouldSkip(bd)) {
				if (StringUtils.hasLength(mappedName)) {
					String[] matchingBeans = resolver.getBeansForType(lookupType);
					for (String matchingBen : matchingBeans) {
						if (mappedName.equals(matchingBen)) {
							IBeanReference ref = new AutowireBeanReference(bean, new RuntimeBeanReference(matchingBen));
							if (getMember() instanceof Field) {
								((AutowireBeanReference) ref).setSource((Field) getMember());
							}
							else {
								((AutowireBeanReference) ref).setSource(getMember(), 0);
							}
							return Collections.singleton(ref);
						}
					}
				}
				else {
					return super.getBeanReferences(bean, context, resolver);
				}
			}
			return Collections.emptySet();
		}

	}

	/**
	 * Class representing injection information about an annotated field or setter method, supporting the @WebServiceRef
	 * annotation.
	 */
	private class WebServiceRefElement extends LookupElement {

		public WebServiceRefElement(Member member, PropertyDescriptor pd, Class<? extends Annotation> annotationClass) {
			super(member, pd, annotationClass);
		}

		@Override
		protected void initAnnotation(AnnotatedElement ae, Class<? extends Annotation> annotationClass) {
			Annotation resource = ae.getAnnotation(annotationClass);
			String resourceName = (String) AnnotationUtils.getValue(resource, "name");
			Class<?> resourceType = (Class<?>) AnnotationUtils.getValue(resource, "type");
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				resourceName = this.member.getName();
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			if (resourceType != null && !Object.class.equals(resourceType)) {
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method.
				resourceType = getResourceType();
			}
			this.name = resourceName;
			if (Service.class.isAssignableFrom(resourceType)) {
				this.lookupType = resourceType;
			}
			else {
				this.lookupType = (!Object.class.equals(AnnotationUtils.getValue(resource, "value")) ? (Class<?>) AnnotationUtils
						.getValue(resource, "value")
						: Service.class);
			}
			this.mappedName = (String) AnnotationUtils.getValue(resource, "mappedName");
		}
	}

	/**
	 * Class representing injection information about an annotated field or setter method, supporting the @EJB
	 * annotation.
	 */
	private class EjbRefElement extends LookupElement {

		private String beanName;

		public EjbRefElement(Member member, PropertyDescriptor pd, Class<? extends Annotation> annotationClass) {
			super(member, pd, annotationClass);
		}

		@Override
		protected void initAnnotation(AnnotatedElement ae, Class<? extends Annotation> annotationClass) {
			Annotation resource = ae.getAnnotation(annotationClass);
			String resourceBeanName = (String) AnnotationUtils.getValue(resource, "beanName");
			String resourceName = (String) AnnotationUtils.getValue(resource, "name");
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				resourceName = this.member.getName();
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = Introspector.decapitalize(resourceName.substring(3));
				}
			}
			Class<?> resourceType = (Class<?>) AnnotationUtils.getValue(resource, "beanInterface");
			if (resourceType != null && !Object.class.equals(resourceType)) {
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method.
				resourceType = getResourceType();
			}
			this.beanName = resourceBeanName;
			this.name = resourceName;
			this.lookupType = resourceType;
			this.mappedName = (String) AnnotationUtils.getValue(resource, "mappedName");
		}

		@Override
		public Set<IBeanReference> getBeanReferences(IBean bean, IBeansModelElement context,
				AutowireDependencyProvider provider) {
			BeanDefinition bd = BeansModelUtils.getMergedBeanDefinition(bean, context);

			if (!shouldSkip(bd)) {

				if (StringUtils.hasLength(this.beanName)) {
					if (provider.containsBean(this.beanName)) {
						String[] matchingBeans = provider.getBeansForType(this.lookupType);
						for (String matchingBen : matchingBeans) {
							if (this.beanName.equals(matchingBen)) {
								IBeanReference ref = new AutowireBeanReference(bean, new RuntimeBeanReference(
										matchingBen));
								if (getMember() instanceof Field) {
									((AutowireBeanReference) ref).setSource((Field) getMember());
								}
								else {
									((AutowireBeanReference) ref).setSource(getMember(), 0);
								}
								return Collections.singleton(ref);
							}
						}
					}
					else if (this.isDefaultName && !StringUtils.hasLength(this.mappedName)) {
						// throw new NoSuchBeanDefinitionException(this.beanName,
						// "Cannot resolve 'beanName' in local BeanFactory. Consider specifying a general 'name' value instead.");
					}
				}
			}
			return super.getBeanReferences(bean, context, provider);
		}

	}

}
