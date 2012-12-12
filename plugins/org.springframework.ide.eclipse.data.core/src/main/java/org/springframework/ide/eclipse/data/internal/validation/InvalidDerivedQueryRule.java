/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.data.jdt.core.RepositoryInformation;
import org.springframework.ide.eclipse.data.jdt.core.SpringDataCompilationParticipant;

/**
 * This is a reincarnation of {@link SpringDataCompilationParticipant}.
 *
 * @author Olivier Gierke
 * @author Tomasz Zarna
 *
 */
@SuppressWarnings("deprecation")
public class InvalidDerivedQueryRule implements IValidationRule<CompilationUnit, SpringDataValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		if (!(context instanceof SpringDataValidationContext))
			return false;
		if( element instanceof CompilationUnit) {
			CompilationUnit cue = (CompilationUnit) element;
			return supports(cue.getTypeRoot());
		}
		return false;
	}

	private boolean supports(ITypeRoot typeRoot) {
		if (typeRoot == null)
			return false;

		IType type = typeRoot.findPrimaryType();

		// Skip non-interfaces
		try {
			if (type == null || !type.isInterface() || type.isAnnotation()) {
				return false;
			}
		} catch (JavaModelException e) {
			SpringCore.log(e);
			return false;
		}

		// Skip non-spring-data repositories
		if (!RepositoryInformation.isSpringDataRepository(type)) {
			return false;
		}

		// resolve repository information and generate problem markers
		RepositoryInformation information = new RepositoryInformation(type);

		Class<?> domainClass = information.getManagedDomainClass();
		if (domainClass == null) {
			return false;
		}
		return true;
	}

	public void validate(CompilationUnit element,
			SpringDataValidationContext context, IProgressMonitor monitor) {

		try {

			ITypeRoot typeRoot = element.getTypeRoot();
			IType type = typeRoot.findPrimaryType();

			if (!supports(typeRoot))
				return;

			// resolve repository information and generate problem markers
			RepositoryInformation information = new RepositoryInformation(type);

			Class<?> domainClass = information.getManagedDomainClass();
			if (domainClass == null) {
				return;
			}

			for (IMethod method : information.getMethodsToValidate()) {

				String methodName = method.getElementName();

				try {
					new PartTree(methodName, domainClass);
				} catch (PropertyReferenceException e) {
					element.setElementSourceLocation(new JavaModelSourceLocation(
							method));
					context.error(element, "INVALID_DERIVED_QUERY",
							"Invalid derived query! " + e.getMessage());
				}
			}

		} catch (JavaModelException e) {
			SpringCore.log(e);
		} catch (Exception e) {
			SpringCore.log(e);
		} catch (Error e) {
			SpringCore.log(e);
		}
	}
}
