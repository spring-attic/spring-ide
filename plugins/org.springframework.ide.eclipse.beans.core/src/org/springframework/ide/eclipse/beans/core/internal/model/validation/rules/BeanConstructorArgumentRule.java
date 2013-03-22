/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Type;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * Validates a given {@link IBean}'s constructor argument. Skips abstract beans and those beans that use a
 * <code>factory-bean</code> and/or <code>factory-method</code> attributes.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanConstructorArgumentRule extends AbstractBeanValidationRule {

	@Override
	protected boolean supportsBean(IBean bean, IBeansValidationContext context) {
		return !bean.isAbstract();
	}

	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean, context.getContextElement());

		// If any constructor argument defined in bean the validate the merged constructor arguments
		// in merged bean's class (child beans fully supported)
		String mergedClassName = mergedBd.getBeanClassName();
		if (mergedClassName != null && !SpringCoreUtils.hasPlaceHolder(mergedClassName)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), mergedClassName);
			if (type != null) {
				validateConstructorArguments(bean, type, context);
			}
		}
	}

	protected void validateConstructorArguments(final IBean bean, final IType type,
			final IBeansValidationContext context) {

		// Skip validation if auto-wiring or a factory are involved
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean).getBeanDefinition();
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());
		if (!(bd instanceof AnnotatedBeanDefinition)
				&& mergedBd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO
				&& mergedBd.getFactoryBeanName() == null && mergedBd.getFactoryMethodName() == null) {

			// Check for default constructor if no constructor arguments are available
			final int numArguments = (mergedBd.getConstructorArgumentValues() == null ? 0 : mergedBd
					.getConstructorArgumentValues().getArgumentCount());
			try {
				// Make sure that the constructor exists and we need
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element = BeansModelUtils.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}

					AnnotationMetadata metadata = getAnnotationMetadata(context.getClassReaderFactory(), bean, type);
					// add check if prototype and configurable and if constructor
					// is autowired do this at the latest possible stage due to
					// performance considerations
					if (!(bd.isPrototype() && metadata.hasConfigurableAnnotation())
							&& !(metadata.isConstructorAutowired() && context.isBeanRegistered(
									AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
									AutowiredAnnotationBeanPostProcessor.class.getName()))) {
						String className = type.getFullyQualifiedName();
						context.error(bean, "NO_CONSTRUCTOR", "No constructor with " + numArguments
								+ (numArguments == 1 ? " argument" : " arguments") + " defined in class '" + className
								+ "'", new ValidationProblemAttribute("CLASS", className));
					}
				}
				
				// Validate the actual constructors for name, type matches
				else if (numArguments > 0) {
					Set<IMethod> ctors = Introspector.getConstructors(type, numArguments, true);
					
					for (IBeanConstructorArgument argument : bean.getConstructorArguments()) {
						String name = argument.getName();
						if (name != null) {
							boolean found = false;
							for(IMethod ctor: ctors) {
								List<String> parameterNames = Arrays.asList(ctor.getParameterNames());
								if (parameterNames.contains(name)) {
									found = true;
								}
							}
							
							if (!found) {
								context.warning(argument, "MISSING_CONSTRUCTOR_ARG_NAME", String.format(
										"Cannot find constructor parameter with name '%s'", name),
										new ValidationProblemAttribute("CLASS", type.getFullyQualifiedName()),
										new ValidationProblemAttribute("NUM_CONSTRUCTOR_ARGS", numArguments));
							}
						}
					}
				}
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}

	/**
	 * Retrieves a instance of {@link AnnotationMetadata} that contains information about used annotations in the class
	 * under question
	 */
	private AnnotationMetadata getAnnotationMetadata(final ClassReaderFactory classReaderFactory, final IBean bean,
			final IType type) {
		final String className = type.getFullyQualifiedName();
		final AnnotationMetadata visitor = new AnnotationMetadata();
		try {
			ClassReader classReader = classReaderFactory.getClassReader(className);
			classReader.accept(visitor, false);
		}
		catch (IOException e) {
			// ignore any missing files here as this will be
			// reported as missing bean class
		}
		return visitor;
	}

	/**
	 * ASM based visitor that checks the precedence of an {@link Autowired} annotation on <b>any</b> constructor.
	 */
	private static class AnnotationMetadata extends AnnotationMetadataReadingVisitor {

		private static final String CONSTRUCTOR_NAME = "<init>";

		private static final String AUTOWIRED_NAME = Type.getDescriptor(Autowired.class);

		private static final String INJECT_NAME = "Ljavax/inject/Inject;";

		private boolean isConstructorAutowired = false;

		@Override
		public MethodVisitor visitMethod(int modifier, String name, String params, String arg3, String[] arg4) {
			if (CONSTRUCTOR_NAME.equals(name)) {
				return new EmptyVisitor() {
					@Override
					public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
						if (AUTOWIRED_NAME.equals(desc)) {
							isConstructorAutowired = true;
						}
						else if (INJECT_NAME.equals(desc)) {
							isConstructorAutowired = true;
						}
						return new EmptyVisitor();
					}
				};
			}
			return new EmptyVisitor();
		}

		public boolean isConstructorAutowired() {
			return isConstructorAutowired;
		}

		public boolean hasConfigurableAnnotation() {
			return super.hasAnnotation(Configurable.class.getName());
		}
	}

}
