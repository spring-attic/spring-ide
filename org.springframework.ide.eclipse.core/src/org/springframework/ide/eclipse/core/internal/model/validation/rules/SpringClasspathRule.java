/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.ide.eclipse.core.internal.model.validation.SpringValidationContext;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;

/**
 * {@link IValidationRule} that checks if the Spring Framework is on the
 * project's build path.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class SpringClasspathRule implements
		IValidationRule<ISpringProject, SpringValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof ISpringProject;
	}

	/**
	 * Tries to load the Spring {@link BeanFactory} class in order to check if
	 * Spring is on the build path of the given {@link ISpringProject}.
	 */
	public void validate(ISpringProject element,
			SpringValidationContext context, IProgressMonitor monitor) {
		if (JdtUtils.isJavaProject(element.getProject())) {
			IType type = JdtUtils.getJavaType(element.getProject(),
					BeanFactory.class.getName());
			if (type == null) {
				context
						.warning(element, "NO_SPRING_ON_CLASSPATH",
								"Spring appears to be missing from the project's Build Path");
			}
		}
	}

}
