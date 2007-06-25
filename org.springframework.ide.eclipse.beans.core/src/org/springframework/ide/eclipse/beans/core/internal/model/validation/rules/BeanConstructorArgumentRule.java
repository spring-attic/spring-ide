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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.ScannedRootBeanDefinition;
import org.springframework.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.asm.ClassReaderFactory;
import org.springframework.core.type.asm.SimpleClassReaderFactory;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;

/**
 * Validates a given {@link IBean}'s constructor argument. Skips abstract
 * beans.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanConstructorArgumentRule extends AbstractBeanValidationRule {

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof Bean && !((Bean) element).isAbstract());
	}

	@Override
	public void validate(IBean bean, BeansValidationContext context,
			IProgressMonitor monitor) {
		BeanDefinition bd = ((Bean) bean).getBeanDefinition();
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());

		// Validate merged constructor arguments in bean's class (child beans
		// not supported)
		String className = bd.getBeanClassName();
		if (className != null && !ValidationRuleUtils.hasPlaceHolder(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean)
					.getProject(), className);
			if (type != null) {
				validateConstructorArguments(bean, type, mergedBd
						.getConstructorArgumentValues(), context);
			}
		}

		// If any constructor argument defined in bean the validate the merged
		// constructor arguments in merged bean's class (child beans fully
		// supported)
		if (!bd.getConstructorArgumentValues().isEmpty()) {
			String mergedClassName = mergedBd.getBeanClassName();
			if (mergedClassName != null
					&& !ValidationRuleUtils.hasPlaceHolder(mergedClassName)) {
				IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(
						bean).getProject(), mergedClassName);
				if (type != null) {
					validateConstructorArguments(bean, type, mergedBd
							.getConstructorArgumentValues(), context);
				}
			}
		}
	}

	protected void validateConstructorArguments(final IBean bean, final IType type,
			ConstructorArgumentValues argumentValues, final IValidationContext context) {

		// TODO CD need to add a check here if constructor has @Autowired
		// annotation; for now we just skip validation of
		// ScannedRootBeanDefinition

		// Skip validation if auto-wiring or a factory are involved
		AbstractBeanDefinition bd = (AbstractBeanDefinition) ((Bean) bean)
				.getBeanDefinition();
		if (!(bd instanceof ScannedRootBeanDefinition)
				&& bd.getAutowireMode() == AbstractBeanDefinition.AUTOWIRE_NO
				&& bd.getFactoryBeanName() == null
				&& bd.getFactoryMethodName() == null) {

			// Check for default constructor if no constructor arguments are
			// available
			final int numArguments = (argumentValues == null ? 0 : argumentValues
					.getArgumentCount());
			try {
				if (!Introspector.hasConstructor(type, numArguments, true)) {
					ISourceModelElement element = BeansModelUtils
							.getFirstConstructorArgument(bean);
					if (element == null) {
						element = bean;
					}
					
					// add check if prototype and configurable; do this at the 
					// latest possible stage due to performance considerations
					if (!(bd.isPrototype() && 
							checkForPrecedenceOfConfigurableAnnotation(bean, type,
								context, numArguments))) {
						context.error(bean, "NO_CONSTRUCTOR",
							"No constructor with "
									+ numArguments
									+ (numArguments == 1 ? " argument"
											: " arguments")
									+ " defined in class '"
									+ type.getFullyQualifiedName() + "'");
					}
				}
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
	}
	
	/**
	 * Checks if the class under question is annotated with {@link Configurable}.
	 */
	private boolean checkForPrecedenceOfConfigurableAnnotation(final IBean bean, 
			final IType type, final IValidationContext context, final int numArguments) {
		IJavaProject jp = JdtUtils.getJavaProject(bean.getElementResource().getProject());
		final String className = type.getFullyQualifiedName();
		final AnnotationMetadataReadingVisitor visitor = 
			new AnnotationMetadataReadingVisitor();
		if (jp != null) {
			try {
				JdtUtils.getProjectClassLoaderSupport(jp).executeCallback(
						new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

					public void doWithActiveProjectClassLoader() throws Throwable {
						ClassReaderFactory classReaderFactory = new SimpleClassReaderFactory();
						ClassReader classReader = classReaderFactory.getClassReader(className);
						classReader.accept(visitor, false);
					}});
			}
			catch (Throwable e) {
				BeansCorePlugin.log(e);
			}
		}
		return visitor.hasAnnotation(Configurable.class.getName());
	}
}
