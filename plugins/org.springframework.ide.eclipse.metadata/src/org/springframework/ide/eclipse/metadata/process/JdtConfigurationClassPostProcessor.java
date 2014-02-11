/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.process;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.DelegatingSourceExtractor;
import org.springframework.ide.eclipse.beans.core.internal.model.ProfileAwareCompositeComponentDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.ToolingAwareEnvironment;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigRegistrationSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.classreading.CachingJdtMetadataReaderFactory;
import org.springframework.ide.eclipse.core.java.classreading.JdtConnectedMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelMethodSourceLocation;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * {@link IBeansConfigPostProcessor} that mirrors the behavior of Spring 3.0's {@link ConfigurationClassPostProcessor}.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.1.0
 */
@SuppressWarnings("restriction")
public class JdtConfigurationClassPostProcessor implements IBeansConfigPostProcessor {

	/**
	 * {@inheritDoc}
	 */
	public void postProcess(final IBeansConfigPostProcessingContext postProcessingContext) {
		IJavaProject project = JdtUtils.getJavaProject(postProcessingContext.getBeansConfig().getElementResource());
		if (project == null) {
			return;
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
		DelegatingSourceExtractor sourceExtractor = new DelegatingSourceExtractor(project.getProject());

		processor.setEnvironment(new ToolingAwareEnvironment());
		processor.setSourceExtractor(sourceExtractor);
		processor.setMetadataReaderFactory(new CachingJdtMetadataReaderFactory(project, classLoader));
		processor.setProblemReporter(new JdtAnnotationMetadataProblemReporter(postProcessingContext));
		processor.setResourceLoader(new DefaultResourceLoader(classLoader));

		ReaderEventListenerForwardingBeanDefinitionRegistry registry = new ReaderEventListenerForwardingBeanDefinitionRegistry(
				postProcessingContext.getBeanDefinitionRegistry(), postProcessingContext
						.getBeansConfigRegistrySupport(), sourceExtractor);
		registry.setBeanClassLoader(classLoader);
		
		processor.processConfigBeanDefinitions(registry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof JdtConfigurationClassPostProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	/**
	 * {@link BeanDefinitionRegistry} implementation that funnels
	 * {@link #registerBeanDefinition(String, BeanDefinition)} calls to
	 * {@link IBeansConfigRegistrationSupport#registerComponent()}.
	 */
	class ReaderEventListenerForwardingBeanDefinitionRegistry extends DefaultListableBeanFactory implements BeanDefinitionRegistry {

		private final BeanDefinitionRegistry registry;

		private final IBeansConfigRegistrationSupport beansConfigRegistrationSupport;
		
		private final Set<String> profileDefinedBeans;

		private final SourceExtractor sourceExtractor;

		public ReaderEventListenerForwardingBeanDefinitionRegistry(BeanDefinitionRegistry registry,
				IBeansConfigRegistrationSupport beansConfigRegistrationSupport, SourceExtractor sourceExtractor) {
			this.registry = registry;
			this.beansConfigRegistrationSupport = beansConfigRegistrationSupport;
			this.sourceExtractor = sourceExtractor;
			this.profileDefinedBeans = new HashSet<String>();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean containsBeanDefinition(String beanName) {
			return (!profileDefinedBeans.contains(beanName) && registry.containsBeanDefinition(beanName));
		}

		/**
		 * {@inheritDoc}
		 */
		public String[] getAliases(String name) {
			return registry.getAliases(name);
		}

		/**
		 * {@inheritDoc}
		 */
		public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
			return registry.getBeanDefinition(beanName);
		}

		/**
		 * {@inheritDoc}
		 */
		public int getBeanDefinitionCount() {
			return registry.getBeanDefinitionCount();
		}

		/**
		 * {@inheritDoc}
		 */
		public String[] getBeanDefinitionNames() {
			return registry.getBeanDefinitionNames();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isAlias(String beanName) {
			return registry.isAlias(beanName);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isBeanNameInUse(String beanName) {
			return registry.isBeanNameInUse(beanName);
		}

		/**
		 * {@inheritDoc}
		 */
		public void registerAlias(String name, String alias) {
			registry.registerAlias(name, alias);
		}

		/**
		 * {@inheritDoc}
		 */
		public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
				throws BeanDefinitionStoreException {
			
			Object source = beanDefinition.getSource();
			if (!(source instanceof JavaModelSourceLocation)) {
				source = sourceExtractor.extractSource(beanDefinition.getSource(), null);
			}

			// Convert the Bean definition to a bean definition with concrete class and id
			if (source instanceof JavaModelSourceLocation) {
				
				IJavaElement javaElement = JavaCore.create(((JavaModelSourceLocation) source).getHandleIdentifier(), DefaultWorkingCopyOwner.PRIMARY);
				if (javaElement instanceof IMethod && beanDefinition instanceof AnnotatedBeanDefinition
						&& source instanceof JavaModelMethodSourceLocation) {

					JdtAnnotationBeanDefinition newBeanDefinition = new JdtAnnotationBeanDefinition(((AnnotatedBeanDefinition) beanDefinition).getMetadata());
					newBeanDefinition.setSource(source);

					String className = ((JavaModelMethodSourceLocation) source).getReturnType();
					newBeanDefinition.setBeanClassName(className);
					
					registry.registerBeanDefinition(beanName, newBeanDefinition);
					
					Map<String, Object> profileAnnotationMetadata = newBeanDefinition.getMetadata().
							getAnnotationAttributes("org.springframework.context.annotation.Profile");
					if (profileAnnotationMetadata != null && profileAnnotationMetadata.get("value") != null
							&& profileAnnotationMetadata.get("value") instanceof String[]) {
						String[] profiles = (String[]) profileAnnotationMetadata.get("value");
						ProfileAwareCompositeComponentDefinition profileAware = new ProfileAwareCompositeComponentDefinition(beanName, newBeanDefinition.getSource(), profiles);
						profileAware.addNestedComponent(new BeanComponentDefinition(newBeanDefinition, beanName));
						beansConfigRegistrationSupport.registerComponent(profileAware);
						profileDefinedBeans.add(beanName);
					}
					else {
						beansConfigRegistrationSupport.registerComponent(new BeanComponentDefinition(newBeanDefinition,
								beanName));
					}
				}
				else {
					if (beanDefinition instanceof BeanMetadataAttributeAccessor && beanDefinition.getSource() != source) {
						((BeanMetadataAttributeAccessor) beanDefinition).setSource(source);
					}
					
					registry.registerBeanDefinition(beanName, beanDefinition);
					beansConfigRegistrationSupport.registerComponent(new BeanComponentDefinition(beanDefinition,
							beanName));
				}
			}
			else {
				if (beanDefinition instanceof BeanMetadataAttributeAccessor && beanDefinition.getSource() != source) {
					((BeanMetadataAttributeAccessor) beanDefinition).setSource(source);
				}
				
				registry.registerBeanDefinition(beanName, beanDefinition);
				beansConfigRegistrationSupport.registerComponent(new BeanComponentDefinition(beanDefinition,
						beanName));
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void removeAlias(String alias) {
			registry.removeAlias(alias);
		}

		/**
		 * {@inheritDoc}
		 */
		public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
			registry.removeBeanDefinition(beanName);
		}

	}

	@SuppressWarnings("serial")
	class JdtAnnotationBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

		private final AnnotationMetadata annotationMetadata;

		public JdtAnnotationBeanDefinition(AnnotationMetadata annotationMetadata) {
			this.annotationMetadata = annotationMetadata;
		}

		/**
		 * {@inheritDoc}
		 */
		public AnnotationMetadata getMetadata() {
			return annotationMetadata;
		}

	}

	class JdtAnnotationMetadataProblemReporter implements ProblemReporter {

		private final IBeansConfigPostProcessingContext postProcessingContext;

		public JdtAnnotationMetadataProblemReporter(IBeansConfigPostProcessingContext postProcessingContext) {
			this.postProcessingContext = postProcessingContext;
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(Problem problem) {
			createProblem(IMarker.SEVERITY_ERROR, problem.getMessage(), problem.getLocation().getSource());
		}

		/**
		 * {@inheritDoc}
		 */
		public void fatal(Problem problem) {
			error(problem);
		}

		/**
		 * {@inheritDoc}
		 */
		public void warning(Problem problem) {
			createProblem(IMarker.SEVERITY_WARNING, problem.getMessage(), problem.getLocation().getSource());
		}

		private void createProblem(int severity, String message, Object source) {
			if (source instanceof JdtConnectedMetadata) {
				IJavaElement je = ((JdtConnectedMetadata) source).getJavaElement();
				createProblem(severity, postProcessingContext, message, je);
			}
		}

		private void createProblem(int severity, IBeansConfigPostProcessingContext postProcessingContext,
				String message, IJavaElement je) {
			try {
				postProcessingContext.reportProblem(new ValidationProblem(severity, message,
						je.getUnderlyingResource(), JdtUtils.getLineNumber(je)));
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

}
