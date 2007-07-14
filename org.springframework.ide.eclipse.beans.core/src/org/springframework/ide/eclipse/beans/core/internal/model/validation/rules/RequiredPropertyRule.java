/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;

/**
 * Validates a given {@link IBean}'s if all {@link Required} annotated
 * properties are configured.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class RequiredPropertyRule extends AbstractBeanValidationRule {

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Bean;
	}

	@Override
	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		String className = mergedBd.getBeanClassName();
		if (className != null && !ValidationRuleUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean)
					.getProject(), className);
			if (type != null
					&& checkIfRequiredAnnotationPostProcessorIsRegistered(context)) {
				validatePropertyValues(type, bean, mergedBd, context);
			}
		}
	}

	private void validatePropertyValues(IType type, IBean bean,
			BeanDefinition mergedBd, BeansValidationContext context) {
		try {

			RequiredAnnotationMetadata annotationMetadata = getRequiredAnnotationMetadata(
					context.getClassReaderFactory(), bean, type);

			List<String> missingProperties = new ArrayList<String>();
			Set<IMethod> properties = Introspector
					.findAllWritableProperties(type);
			for (IMethod property : properties) {
				String propertyName = java.beans.Introspector
						.decapitalize(property.getElementName().substring(3));
				if (annotationMetadata.isRequiredProperty(propertyName)
						&& mergedBd.getPropertyValues().getPropertyValue(
								propertyName) == null) {
					missingProperties.add(propertyName);
				}
			}

			// add the error message
			if (missingProperties.size() > 0) {
				String msg = buildExceptionMessage(missingProperties, bean
						.getElementName());
				context.error(bean, "REQUIRED_PROPERTY_MISSING", msg);
			}
		}
		catch (JavaModelException e) {
			BeansCorePlugin.log(e);
		}
	}

	/**
	 * Checks if a {@link RequiredAnnotationBeanPostProcessor} has been
	 * registered in the {@link BeanDefinitionRegistry}.
	 */
	private boolean checkIfRequiredAnnotationPostProcessorIsRegistered(
			BeansValidationContext context) {
		try {
			return context
					.getCompleteRegistry()
					.getBeanDefinition(
							AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME) != null;
		}
		catch (NoSuchBeanDefinitionException e) {
			// fall back for manual installation of the post processor
			for (String name : context.getCompleteRegistry()
					.getBeanDefinitionNames()) {
				BeanDefinition db = context.getCompleteRegistry()
						.getBeanDefinition(name);
				if (db.getBeanClassName() != null
						&& Introspector.doesExtend(JdtUtils
								.getJavaType(context.getRootElementProject(),
										db.getBeanClassName()),
								RequiredAnnotationBeanPostProcessor.class
										.getName())) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Retrieves a instance of {@link RequiredAnnotationMetadata} that contains
	 * information about used annotations in the class under question
	 */
	private RequiredAnnotationMetadata getRequiredAnnotationMetadata(
			final ClassReaderFactory classReaderFactory, final IBean bean,
			final IType type) {
		String className = type.getFullyQualifiedName();
		RequiredAnnotationMetadata visitor = new RequiredAnnotationMetadata();
		try {
			while (className != null && !Object.class.getName().equals(className)) {
				ClassReader classReader = classReaderFactory
						.getClassReader(className);
				classReader.accept(visitor, false);
				className = visitor.getSuperClassName();
			}
		}
		catch (IOException e) {
			// ignore any missing files here as this will be
			// reported as missing bean class
		}
		return visitor;
	}

	/**
	 * ASM based visitor that checks the precedence of an {@link Required}
	 * annotation on <b>any</b> property setter.
	 */
	private static class RequiredAnnotationMetadata extends
			AnnotationMetadataReadingVisitor {

		private static final String REQUIRED_NAME = Type
				.getDescriptor(Required.class);

		private Set<String> requiredPropertyNames = new HashSet<String>();

		@Override
		public MethodVisitor visitMethod(int modifier, final String name,
				String params, String arg3, String[] arg4) {
			if (name.startsWith("set")) {
				return new EmptyVisitor() {
					@Override
					public AnnotationVisitor visitAnnotation(final String desc,
							boolean visible) {
						if (REQUIRED_NAME.equals(desc)) {
							requiredPropertyNames.add(java.beans.Introspector
									.decapitalize(name.substring(3)));
						}
						return new EmptyVisitor();
					}
				};
			}
			return new EmptyVisitor();
		}

		public boolean isRequiredProperty(String propertyName) {
			return requiredPropertyNames.contains(propertyName);
		}
	}

	/**
	 * Build an exception message for the given list of invalid properties.
	 * @param invalidProperties the list of names of invalid properties
	 * @param beanName the name of the bean
	 * @return the exception message
	 */
	private String buildExceptionMessage(List<String> invalidProperties,
			String beanName) {
		int size = invalidProperties.size();
		StringBuilder sb = new StringBuilder();
		sb.append(size == 1 ? "Property" : "Properties");
		for (int i = 0; i < size; i++) {
			String propertyName = invalidProperties.get(i);
			if (i > 0) {
				if (i == (size - 1)) {
					sb.append(" and");
				}
				else {
					sb.append(",");
				}
			}
			sb.append(" '").append(propertyName).append("'");
		}
		sb.append(size == 1 ? " is" : " are");
		sb.append(" required for bean '").append(beanName).append("'");
		return sb.toString();
	}
}
