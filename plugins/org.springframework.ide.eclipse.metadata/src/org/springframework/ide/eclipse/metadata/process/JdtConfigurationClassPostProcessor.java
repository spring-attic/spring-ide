/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
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
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.DelegatingSourceExtractor;
import org.springframework.ide.eclipse.beans.core.internal.model.ProfileAwareCompositeComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigRegistrationSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.classreading.JdtAnnotationMetadata;
import org.springframework.ide.eclipse.core.java.classreading.JdtMetadataReaderFactory;
import org.springframework.ide.eclipse.core.java.classreading.JdtMethodMetadata;
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

		ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
		processor.setSourceExtractor(new DelegatingSourceExtractor(project.getProject()));
		processor.setMetadataReaderFactory(new JdtMetadataReaderFactory(project));
		processor.setProblemReporter(new JdtAnnotationMetadataProblemReporter(postProcessingContext));

		processor.processConfigBeanDefinitions(new ReaderEventListenerForwardingBeanDefinitionRegistry(
				postProcessingContext.getBeanDefinitionRegistry(), postProcessingContext
						.getBeansConfigRegistrySupport()));
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
	class ReaderEventListenerForwardingBeanDefinitionRegistry implements BeanDefinitionRegistry {

		private final BeanDefinitionRegistry registry;

		private final IBeansConfigRegistrationSupport beansConfigRegistrationSupport;
		
		private final Set<String> profileDefinedBeans;

		public ReaderEventListenerForwardingBeanDefinitionRegistry(BeanDefinitionRegistry registry,
				IBeansConfigRegistrationSupport beansConfigRegistrationSupport) {
			this.registry = registry;
			this.beansConfigRegistrationSupport = beansConfigRegistrationSupport;
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

			// Convert the Bean definition to a bean definition with concrete class and id
			if (beanDefinition.getSource() instanceof JavaModelSourceLocation) {
				IMethod method = (IMethod) JavaCore.create(((JavaModelSourceLocation) beanDefinition.getSource())
						.getHandleIdentifier(), DefaultWorkingCopyOwner.PRIMARY);
				if (method != null && beanDefinition instanceof AnnotatedBeanDefinition) {
					JdtAnnotationBeanDefinition newBeanDefinition = new JdtAnnotationBeanDefinition(
							((AnnotatedBeanDefinition) beanDefinition).getMetadata());
					newBeanDefinition.setSource(beanDefinition.getSource());
					String className = JdtUtils.resolveClassName(JdtUtils.getReturnTypeString(method, false), method
							.getDeclaringType());
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
					registry.registerBeanDefinition(beanName, beanDefinition);
					beansConfigRegistrationSupport.registerComponent(new BeanComponentDefinition(beanDefinition,
							beanName));
				}
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
			if (source instanceof JdtMethodMetadata) {
				IJavaElement je = ((JdtMethodMetadata) source).getMethod();
				createProblem(severity, postProcessingContext, message, je);
			}
			else if (source instanceof JdtAnnotationMetadata) {
				IJavaElement je = ((JdtAnnotationMetadata) source).getType();
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
