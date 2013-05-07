/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Provider;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.IAutowireDependencyResolver;
import org.springframework.ide.eclipse.beans.core.autowire.IFactoryBeanTypeResolver;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.InjectionMetadata.InjectedElement;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.ValidationRuleUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link IAutowireDependencyResolver} capable of processing Spring's {@link Autowired}, {@link EJB}, {@link Resource}annotations.
 * <p>
 * This class is the entry point into autowiring resolution within Spring IDE.
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.2.7
 */
public class AutowireDependencyProvider implements IAutowireDependencyResolver {
	
	public static final String TOO_MANY_MATCHING_BEANS = "TOO_MANY_MATCHING_BEANS";
	
	public static final String REQUIRED_NO_MATCH = "REQUIRED_NO_MATCH";
	
	public static final String AUTOWIRE_PROBLEM_TYPE = "AUTOWIRE_PROBLEM";
	
	public static final String MATCHING_BEAN_NAME = "MATCHING_BEAN_NAME";
	
	public static final String BEAN_TYPE = "BEAN_TYPE";

	private Set<IBean> beans;

	private IBeansModelElement context;

	private IBeansModelElement element;

	private IBeansProject project;

	private IProjectClassLoaderSupport classLoaderSupport;

	private Map<IBean, List<InjectionMetadata>> injectionMetadata = new ConcurrentHashMap<IBean, List<InjectionMetadata>>();

	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	private IInjectionMetadataProviderProblemReporter problemReporter = new AutowireProblemReporter();

	private List<ValidationProblem> problems = new ArrayList<ValidationProblem>();

	private Map<Class<?>, String> resolvableDependencies = new HashMap<Class<?>, String>();

	public AutowireDependencyProvider(IBeansModelElement element, IBeansModelElement context) {
		this.context = (context == null ? element : context);
		this.element = element;
		this.beans = BeansModelUtils.getBeans(context);
		this.project = BeansModelUtils.getParentOfClass(context, IBeansProject.class);
	}

	public List<ValidationProblem> getValidationProblems() {
		return this.problems;
	}

	public Map<IBean, Set<IBeanReference>> resolveAutowiredDependencies() {
		final Map<IBean, Set<IBeanReference>> autowiredBeanReferences = new HashMap<IBean, Set<IBeanReference>>();

		createProjectClassLoaderSupport();

		try {
			this.classLoaderSupport.executeCallback(new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

				public void doWithActiveProjectClassLoader() throws Throwable {

					// fill in the resolvableDependencies
					fillResolvableDependencies();

					for (IInjectionMetadataProvider provider : createInjectionMetadataProviders()) {
						for (final IBean bean : BeansModelUtils.getBeans(element)) {

							List<InjectionMetadata> beanInjectionMetadata = null;
							if (injectionMetadata.containsKey(bean)) {
								beanInjectionMetadata = injectionMetadata.get(bean);
							}
							else {
								beanInjectionMetadata = new ArrayList<InjectionMetadata>();
								injectionMetadata.put(bean, beanInjectionMetadata);
							}

							String className = BeansModelUtils.getBeanClass(bean, context);
							try {
								if (className != null && !bean.isFactory()) {
									Class<?> targetClass = ClassUtils.loadClass(className);
									beanInjectionMetadata.add(provider.findAutowiringMetadata(targetClass));
								}
							}
							catch (Throwable e) {
							}
						}
					}

					for (Map.Entry<IBean, List<InjectionMetadata>> entry : injectionMetadata.entrySet()) {
						Set<IBeanReference> autowiredReferences = new HashSet<IBeanReference>();

						for (InjectionMetadata metadata : entry.getValue()) {
							resolveDependencies(entry.getKey(), autowiredReferences, metadata.getInjectedFields());
							resolveDependencies(entry.getKey(), autowiredReferences, metadata.getInjectedMethods());
							resolveConstructorDependencies(entry.getKey(), autowiredReferences, metadata
									.getInjectedConstructors());
						}
						if (autowiredReferences.size() > 0) {
							autowiredBeanReferences.put(entry.getKey(), autowiredReferences);
						}
					}
				}

				private void fillResolvableDependencies() {
					addResolvableClass(BeanFactory.class.getName());
					addResolvableClass("org.springframework.core.io.ResourceLoader");
					addResolvableClass("org.springframework.context.ApplicationEventPublisher");
					addResolvableClass("org.springframework.context.ApplicationContext");

					addResolvableClass("javax.servlet.ServletConfig");
					addResolvableClass("javax.servlet.ServletRequest", "requestObjectFactory");
					addResolvableClass("javax.servlet.http.HttpSession", "sessionObjectFactory");

					addResolvableClass("javax.portlet.PortletRequest", "requestObjectFactory");
					addResolvableClass("javax.portlet.PortletSession", "sessionObjectFactory");

					addResolvableClass("javax.portlet.PortletSession", "sessionObjectFactory");
				}

				private void addResolvableClass(String className, String beanName) {
					try {
						Class<?> clazz = ClassUtils.loadClass(className);
						resolvableDependencies.put(clazz, beanName);
					}
					catch (ClassNotFoundException e) {
					}
				}

				private void addResolvableClass(String className) {
					addResolvableClass(className, StringUtils.uncapitalize(org.springframework.util.ClassUtils
							.getShortName(className)));
				}

				private void resolveConstructorDependencies(IBean bean, Set<IBeanReference> autowiredReferences,
						Set<InjectedElement> injectedConstructors) {
					InjectedElement[] constructors = sortConstructors(injectedConstructors);

					// Special handling for explicit defined values
					if (constructors.length > 0) {
						for (InjectionMetadata.InjectedElement injectionElement : constructors) {
							try {
								autowiredReferences.addAll(injectionElement.getBeanReferences(bean, context,
										AutowireDependencyProvider.this));
							}
							catch (Throwable e) {
								// TODO CD log somewhere
							}
						}
					}
				}

				private InjectedElement[] sortConstructors(Set<InjectedElement> injectedConstructors) {
					InjectedElement[] constructors = (InjectedElement[]) injectedConstructors
							.toArray(new InjectedElement[injectedConstructors.size()]);
					Arrays.sort(constructors, new Comparator<InjectedElement>() {

						public int compare(InjectedElement o1, InjectedElement o2) {
							Constructor<?> c1 = (Constructor<?>) o1.getMember();
							Constructor<?> c2 = (Constructor<?>) o2.getMember();
							boolean p1 = Modifier.isPublic(c1.getModifiers());
							boolean p2 = Modifier.isPublic(c2.getModifiers());
							if (p1 != p2) {
								return (p1 ? -1 : 1);
							}
							int c1pl = c1.getParameterTypes().length;
							int c2pl = c2.getParameterTypes().length;
							return (new Integer(c1pl)).compareTo(c2pl) * -1;
						}
					});
					return constructors;
				}

				private void resolveDependencies(IBean bean, Set<IBeanReference> autowiredReferences,
						Set<InjectionMetadata.InjectedElement> injectionElements) {
					if (injectionElements.size() > 0) {
						for (InjectionMetadata.InjectedElement injectionElement : injectionElements) {
							try {
								autowiredReferences.addAll(injectionElement.getBeanReferences(bean, context,
										AutowireDependencyProvider.this));
							}
							catch (Throwable e) {
								// TODO CD log somewhere
							}
						}
					}
				}
			});
		}
		catch (ClassNotFoundException e) {
			// Ignore here as this can easily happen if project class path is not complete
		}
		catch (NoClassDefFoundError e) {
			// Ignore here as this can easily happen if project class path is not complete
		}
		catch (Throwable e) {
			BeansCorePlugin.log(e);
		}

		return autowiredBeanReferences;
	}

	public boolean containsBean(String beanName) {
		return getBean(beanName) != null;
	}

	public String[] getAliases(String beanName) {
		Set<String> aliases = new HashSet<String>();
		if (context instanceof IBeansConfig) {
			for (IBeanAlias alias : ((IBeansConfig) context).getAliases()) {
				if (beanName.equals(alias.getBeanName())) {
					aliases.add(alias.getElementName());
				}
			}
		}
		else if (context instanceof IBeansConfigSet) {
			for (IBeanAlias alias : ((IBeansConfigSet) context).getAliases()) {
				if (beanName.equals(alias.getBeanName())) {
					aliases.add(alias.getElementName());
				}
			}
		}
		return (String[]) aliases.toArray(new String[aliases.size()]);
	}

	public IBean getBean(String candidateName) {
		for (IBean bean : beans) {
			if (bean.getElementName().equals(candidateName)) {
				return bean;
			}
			else {
				for (String alias : getAliases(bean.getElementName())) {
					if (alias.equals(candidateName)) {
						return bean;
					}
				}
			}
		}
		return null;
	}

	public String[] getBeansForType(Class<?> requiredType) {
		Set<String> matchingBeans = new HashSet<String>();
		for (IBean bean : beans) {
			String beanClassName = ValidationRuleUtils.getBeanClassName(bean, context);
			if (beanClassName != null) {
				try {
					Class<?> beanClass = ClassUtils.loadClass(beanClassName);
					if (requiredType.isAssignableFrom(beanClass)) {
						matchingBeans.add(bean.getElementName());
					}
					else if (FactoryBean.class.isAssignableFrom(beanClass) && isFactoryForType(beanClass, requiredType)) {
						matchingBeans.add(bean.getElementName());
					}
					else if (FactoryBean.class.isAssignableFrom(beanClass) && isExtensibleFactoryForType(bean, beanClass, requiredType)) {
						matchingBeans.add(bean.getElementName());
					}
				}
				catch (ClassNotFoundException e) {
				}
			}
		}
		return (String[]) matchingBeans.toArray(new String[matchingBeans.size()]);
	}

	private boolean isFactoryForType(Class<?> beanClass, Class<?> requiredType) {
		try {
			Method factoryMethod = beanClass.getMethod("getObject", new Class[] {});
			if (factoryMethod != null && requiredType.isAssignableFrom(factoryMethod.getReturnType())) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isExtensibleFactoryForType(IBean bean, Class<?> beanClass, Class<?> requiredType) {
		IFactoryBeanTypeResolver[] resolvers = FactoryBeanTypeResolverExtensions.getFactoryBeanTypeResolvers();
		
		for (IFactoryBeanTypeResolver factoryTypeResolver : resolvers) {
			Class<?> beanType = factoryTypeResolver.resolveBeanTypeFromFactory(bean, beanClass);
			if (beanType != null && requiredType.isAssignableFrom(beanType)) {
				return true;
			}
		}

		return false;
	}

	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {

		// Consider FactoryBeans as autowiring candidates.
		boolean isFactoryBean = (descriptor != null && descriptor.getDependencyType() != null && FactoryBean.class
				.isAssignableFrom(descriptor.getDependencyType()));
		if (isFactoryBean) {
			beanName = BeanFactoryUtils.transformedBeanName(beanName);
		}

		if (containsBean(beanName)) {
			return isAutowireCandidate(beanName, BeansModelUtils.getMergedBeanDefinition(getBean(beanName), context),
					descriptor);
		}
		else {
			return true;
		}
	}

	private Set<IInjectionMetadataProvider> createInjectionMetadataProviders() {
		Set<IInjectionMetadataProvider> providers = new HashSet<IInjectionMetadataProvider>();
		String[] autowiredAnnotationBeanPostProcessorNames = getBeansForType(AutowiredAnnotationBeanPostProcessor.class);
		for (String autowiredAnnotationBeanPostProcessorName : autowiredAnnotationBeanPostProcessorNames) {
			AutowiredAnnotationInjectionMetadataProvider provider = new AutowiredAnnotationInjectionMetadataProvider(
					this.classLoaderSupport.getProjectClassLoader());

			IBean bean = getBean(autowiredAnnotationBeanPostProcessorName);
			BeanDefinition beanDef = BeansModelUtils.getMergedBeanDefinition(bean, context);
			if (beanDef.getPropertyValues().size() > 0) {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(true);
				wrapper.setConversionService(new DefaultConversionService());
				wrapper.setWrappedInstance(provider);
				for (PropertyValue pv : beanDef.getPropertyValues().getPropertyValueList()) {
					if (wrapper.isWritableProperty(pv.getName())) {
						// TODO other values types required as well?
						if (pv.getValue() instanceof TypedStringValue) {
							wrapper.setPropertyValue(pv.getName(), (((TypedStringValue) pv.getValue())).getValue());
						}
					}
				}
			}
			provider.setProblemReporter(problemReporter);
			providers.add(provider);
		}

		String[] commonAnnotationBeanPostProcessorNames = getBeansForType(CommonAnnotationBeanPostProcessor.class);
		for (String commonAnnotationBeanPostProcessorName : commonAnnotationBeanPostProcessorNames) {
			CommonAnnnotationInjectionMetadataProvider provider = new CommonAnnnotationInjectionMetadataProvider();

			IBean bean = getBean(commonAnnotationBeanPostProcessorName);
			BeanDefinition beanDef = BeansModelUtils.getMergedBeanDefinition(bean, context);
			if (beanDef.getPropertyValues().size() > 0) {
				BeanWrapperImpl wrapper = new BeanWrapperImpl(true);
				wrapper.setConversionService(new DefaultConversionService());
				wrapper.setWrappedInstance(provider);
				for (PropertyValue pv : beanDef.getPropertyValues().getPropertyValueList()) {
					if (wrapper.isWritableProperty(pv.getName())) {
						// TODO other values types required as well?
						if (pv.getValue() instanceof TypedStringValue) {
							wrapper.setPropertyValue(pv.getName(), (((TypedStringValue) pv.getValue())).getValue());
						}
					}
				}
			}
			provider.setProblemReporter(problemReporter);
			providers.add(provider);
		}

		return providers;
	}

	private AutowireCandidateResolver getAutowireCandidateResolver() {
		QualifierAnnotationAutowireCandidateResolver resolver = new QualifierAnnotationAutowireCandidateResolver();
		resolver.setProblemReporter(problemReporter);
		return resolver;
	}

	protected void createProjectClassLoaderSupport() {
		if (this.classLoaderSupport == null) {
			this.classLoaderSupport = JdtUtils.getProjectClassLoaderSupport(project.getProject(), BeansCorePlugin
					.getClassLoader());
		}
	}

	public void setProjectClassLoaderSupport(IProjectClassLoaderSupport classLoaderSupport) {
		this.classLoaderSupport = classLoaderSupport;
	}

	protected String determinePrimaryCandidate(Map<String, IBean> candidateBeans, DependencyDescriptor descriptor) {
		String primaryBeanName = null;
		String fallbackBeanName = null;
		for (Map.Entry<String, IBean> entry : candidateBeans.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBean(candidateBeanName);
					boolean primaryLocal = containsBean(primaryBeanName);
					if (candidateLocal == primaryLocal) {
						problemReporter.error("More than one 'primary' bean found among candiates ["
								+ candidateBeans.keySet() + "]", descriptor);
						throw new AutowireResolutionException();
					}
					else if (candidateLocal && !primaryLocal) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					primaryBeanName = candidateBeanName;
				}
			}
			if (primaryBeanName == null && (matchesBeanName(candidateBeanName, descriptor.getDependencyName()))) {
				fallbackBeanName = candidateBeanName;
			}
		}
		return (primaryBeanName != null ? primaryBeanName : fallbackBeanName);
	}

	protected void doResolveDependency(DependencyDescriptor descriptor, Class<?> type, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {

		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			Map<String, IBean> matchingBeans = findAutowireCandidates(beanName, componentType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					problemReporter.error("No matching beans found for 'required' dependency array", descriptor);
					throw new AutowireResolutionException();
				}
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
		}
		else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> elementType = descriptor.getCollectionType();
			if (elementType == null) {
				if (descriptor.isRequired()) {
					problemReporter.error(
							"No element type declared for 'required' collection [" + type.getName() + "]", descriptor);
					throw new AutowireResolutionException();
				}
			}
			Map<String, IBean> matchingBeans = findAutowireCandidates(beanName, elementType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					problemReporter.error("No matching beans found for 'required' dependency collection", descriptor);
					throw new AutowireResolutionException();
				}
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
		}
		else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> keyType = descriptor.getMapKeyType();
			if (keyType == null || !String.class.isAssignableFrom(keyType)) {
				if (descriptor.isRequired()) {
					problemReporter.error("Key type [" + keyType + "] of map [" + type.getName()
							+ "] must be assignable to [java.lang.String]", descriptor);
					throw new AutowireResolutionException();
				}
			}
			Class<?> valueType = descriptor.getMapValueType();
			if (valueType == null) {
				if (descriptor.isRequired()) {
					problemReporter.error("No value type declared for 'required' map [" + type.getName() + "]",
							descriptor);
					throw new AutowireResolutionException();
				}
			}
			Map<String, IBean> matchingBeans = findAutowireCandidates(beanName, valueType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					problemReporter.error("No matching beans found for 'required' dependency map for value type ["
							+ valueType.getName() + "]", descriptor);
					throw new AutowireResolutionException();
				}
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
		}
		else {
			Map<String, IBean> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					problemReporter.error("Unsatisfied 'required' dependency of type [" + type
							+ "]. Expected at least 1 matching bean", descriptor, new ValidationProblemAttribute(AUTOWIRE_PROBLEM_TYPE, REQUIRED_NO_MATCH));
					throw new AutowireResolutionException();
				}
				return;
			}
			if (matchingBeans.size() > 1) {
				String primaryBeanName = determinePrimaryCandidate(matchingBeans, descriptor);
				if (primaryBeanName == null) {
					Set<String> matchingBeanNames = matchingBeans.keySet();
					ValidationProblemAttribute[] attributes = new ValidationProblemAttribute[matchingBeanNames.size() + 2];
					attributes[0] = new ValidationProblemAttribute(AUTOWIRE_PROBLEM_TYPE, TOO_MANY_MATCHING_BEANS);
					attributes[1] = new ValidationProblemAttribute(BEAN_TYPE, type.getName());
					int counter = 2;
					for(String matchingBeanName: matchingBeanNames) {
						attributes[counter] = new ValidationProblemAttribute(MATCHING_BEAN_NAME + counter, matchingBeanName);
						counter++;
					}
					problemReporter.error("Expected single matching bean but found " + matchingBeans.size() + ": "
							+ matchingBeanNames, descriptor, attributes);
					throw new AutowireResolutionException();
				}
				if (autowiredBeanNames != null) {
					autowiredBeanNames.add(primaryBeanName);
					return;
				}
			}
			// We have exactly one match.
			Map.Entry<String, IBean> entry = matchingBeans.entrySet().iterator().next();
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(entry.getKey());
			}
		}
	}

	protected Map<String, IBean> findAutowireCandidates(String beanName, Class<?> requiredType,
			DependencyDescriptor descriptor) {

		String[] candidateNames = getBeansForType(requiredType);
		Map<String, IBean> result = new LinkedHashMap<String, IBean>(candidateNames.length);

		for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
			if (autowiringType.isAssignableFrom(requiredType)) {
				String autowiringValue = this.resolvableDependencies.get(autowiringType);
				result.put(autowiringValue, null);
				break;
			}
		}

		for (String candidateName : candidateNames) {
			if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, descriptor)) {
				result.put(candidateName, getBean(candidateName));
			}
		}
		return result;
	}

	protected boolean isAutowireCandidate(String beanName, BeanDefinition mbd, DependencyDescriptor descriptor) {
		return getAutowireCandidateResolver().isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName, getAliases(beanName)), descriptor);
	}

	protected boolean isPrimary(String beanName, Object beanInstance) {
		if (containsBean(beanName)) {
			return BeansModelUtils.getMergedBeanDefinition(getBean(beanName), context).isPrimary();
		}
		return false;
	}

	protected boolean matchesBeanName(String beanName, String candidateName) {
		return (candidateName != null && (candidateName.equals(beanName) || ObjectUtils.containsElement(
				getAliases(beanName), candidateName)));
	}

	public void resolveDependency(DependencyDescriptor descriptor, Class<?> type, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) {
		descriptor.initParameterNameDiscovery(this.parameterNameDiscoverer);
		if (descriptor.getDependencyType().equals(ObjectFactory.class) || descriptor.getDependencyType().equals(Provider.class)) {
			descriptor.increaseNestingLevel();
			type = descriptor.getDependencyType();
		}
		
		try {
			doResolveDependency(descriptor, type, beanName, autowiredBeanNames, typeConverter);
		}
		catch (AutowireResolutionException e) {
			// we can ignore this as problems have been reported using the problem reporter
		}
	}

	@SuppressWarnings("serial")
	private static class AutowireResolutionException extends RuntimeException {

	}

	private class AutowireProblemReporter implements IInjectionMetadataProviderProblemReporter {

		public void error(String message, Member member, ValidationProblemAttribute... attributes) {
			try {
				IJavaElement source = AutowireUtils.getJavaElement(project.getProject(), member, -1);
				if (source != null && source.getUnderlyingResource() != null) {
					ValidationProblemAttribute[] newAttributes = new ValidationProblemAttribute[attributes.length + 1];
					for(int i=0; i<attributes.length; i++) {
						newAttributes[i] = attributes[i];
					}
					newAttributes[attributes.length] = new ValidationProblemAttribute("JAVA_HANDLE", source.getHandleIdentifier());
					
					// By convention autowire problems will only get reported as warnings (for now?)
					problems.add(new ValidationProblem(IMarker.SEVERITY_WARNING, message, source
							.getUnderlyingResource(), JdtUtils.getLineNumber(source), 
							newAttributes));
				}
			}
			catch (JavaModelException e) {
			}
		}

		public void error(String message, DependencyDescriptor descriptor, ValidationProblemAttribute... attributes) {
			if (descriptor.getField() != null) {
				error(message, descriptor.getField(), attributes);
			}
			else if (descriptor.getMethodParameter() != null && descriptor.getMethodParameter().getMethod() != null) {
				error(message, descriptor.getMethodParameter().getMethod(), attributes);
			}
			else if (descriptor.getMethodParameter() != null
					&& descriptor.getMethodParameter().getConstructor() != null) {
				error(message, descriptor.getMethodParameter().getConstructor(), attributes);
			}
		}


	}
}
