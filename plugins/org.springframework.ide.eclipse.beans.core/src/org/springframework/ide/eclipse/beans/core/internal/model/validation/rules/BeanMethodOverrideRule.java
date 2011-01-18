/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReplaceMethodOverride;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanMethodOverride;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractNonInfrastructureBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * Validates a given {@link IBeanMethodOverride#getBeanName()} in bean class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanMethodOverrideRule extends AbstractNonInfrastructureBeanValidationRule implements
		IValidationRule<IBeanMethodOverride, IBeansValidationContext> {

	/**
	 * Checks if this validation rule supports given <code>element</code> and <code>context</code>.
	 * @return true if and only element is a {@link IBeanMethodOverride} and the parent element a
	 * {@link IBean}
	 */
	@Override
	protected boolean supportsModelElementForNonInfrastructureBean(IModelElement element,
			IBeansValidationContext context) {
		return element instanceof IBeanMethodOverride
				&& element.getElementParent() instanceof IBean;
	}

	/**
	 * Validates the given {@link IBeanMethodOverride} for matching methods to override.
	 */
	public void validate(IBeanMethodOverride override, IBeansValidationContext context,
			IProgressMonitor monitor) {
		IBean bean = (IBean) override.getElementParent();
		AbstractBeanDefinition mergedBd = (AbstractBeanDefinition) BeansModelUtils
				.getMergedBeanDefinition(bean, context.getContextElement());

		String mergedClassName = mergedBd.getBeanClassName();
		if (mergedClassName != null && !SpringCoreUtils.hasPlaceHolder(mergedClassName)) {
			IType type = ValidationRuleUtils.extractBeanClass(mergedBd, bean, mergedClassName,
					context);
			if (type != null) {
				if (override.getType() == IBeanMethodOverride.TYPE.LOOKUP) {
					validateLookupOverride(override, type, context);
				}
				else if (override.getType() == IBeanMethodOverride.TYPE.REPLACE) {
					validateReplaceOverride(override, type, context);
				}
			}
		}
	}

	/**
	 * Validates if the given bean type has a method that matches the following signature:
	 * <p>
	 * 
	 * <pre>
	 * &lt;public|protected&gt; [abstract] &lt;return-type&gt; theMethodName(..);
	 * </pre>
	 * 
	 * @param override the MethodOverride to check
	 * @param type the bean type
	 * @param context the validation context
	 */
	private void validateReplaceOverride(IBeanMethodOverride override, IType type,
			IBeansValidationContext context) {
		if (override instanceof BeanReplaceMethodOverride) {
			String methodName = override.getMethodName();
			try {
				// TODO CD once typeIdentifier get exposed in Spring core add
				// parameter check to following method check
				// ((ReplaceOverride)((BeanReplaceMethodOverride)
				// override).getMethodOverride()).getTypeIdentifier();

				Set<IMethod> methods = Introspector.getAllMethods(type);
				for (IMethod method : methods) {
					if (method.getElementName().equals(methodName)
							&& (Flags.isProtected(method.getFlags()) || Flags.isPublic(method
									.getFlags()))) {
						return;
					}
				}

				// if we reach here, no matching method could be found!
				context.error(override, "UNDEFINED_REPLACE_METHOD", "Replace-method '" + methodName
						+ "' not found in bean class '" + type.getFullyQualifiedName() + "'");
			}
			catch (JavaModelException e) {
			}
		}
	}

	/**
	 * Validates if the given bean type has a method that matches the following signature:
	 * <p>
	 * 
	 * <pre>
	 * &lt;public|protected&gt; [abstract] &lt;return-type&gt; theMethodName(no-arguments);
	 * </pre>
	 * 
	 * @param override the MethodOverride to check
	 * @param type the bean type
	 * @param context the validation context
	 */
	private void validateLookupOverride(IBeanMethodOverride override, IType type,
			IBeansValidationContext context) {
		String methodName = override.getMethodName();
		try {
			Set<IMethod> methods = Introspector.findAllNoParameterMethods(type, methodName);
			for (IMethod method : methods) {
				if (type.isInterface() || Flags.isProtected(method.getFlags())
						|| Flags.isPublic(method.getFlags())) {
					return;
				}
			}

			// if we reach here, no matching method could be found!
			context.error(override, "UNDEFINED_LOOKUP_METHOD", "Lookup-method '" + methodName
					+ "' not found in bean class '" + type.getFullyQualifiedName() + "'");
		}
		catch (JavaModelException e) {
		}
	}
}
