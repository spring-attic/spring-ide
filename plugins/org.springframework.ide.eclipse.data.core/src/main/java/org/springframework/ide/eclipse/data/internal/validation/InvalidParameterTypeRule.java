/*******************************************************************************
 * Copyright (c) 2012, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal.validation;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.data.jdt.core.RepositoryInformation;

/**
 * @author Terry Denney
 * @since 3.2.0
 */
public class InvalidParameterTypeRule implements
		IValidationRule<CompilationUnit, SpringDataValidationContext> {
	
	public static final String PROBLEM_ID = "INVALID_PARAMETER_TYPE";
	public static final String PROPERTY_TYPE_ATTR = "PROPERTY_TYPE_ATTR";
	public static final String PROPERTY_TYPE_PACKAGE_ATTR = "PROPERTY_TYPE_PACKAGE_ATTR";

	public boolean supports(IModelElement element, IValidationContext context) {
		if (!(context instanceof SpringDataValidationContext)) {
			return false;
		}

		if (element instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) element;
			ITypeRoot typeRoot = cu.getTypeRoot();

			if (typeRoot == null) {
				return false;
			}

			IType type = typeRoot.findPrimaryType();
			if (type == null) {
				return false;
			}

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
		return false;
	}

	public void validate(CompilationUnit element,
			SpringDataValidationContext context, IProgressMonitor monitor) {

		ITypeRoot typeRoot = element.getTypeRoot();
		IType type = typeRoot.findPrimaryType();

		// resolve repository information and generate problem markers
		RepositoryInformation information = new RepositoryInformation(type);

		Class<?> domainClass = information.getManagedDomainClass();
		if (domainClass == null) {
			return;
		}

		try {
			for (IMethod method : type.getMethods()) {
				String methodName = method.getElementName();
				if (methodName.startsWith("findBy")) {
					String propertyName = StringUtils.uncapitalize(methodName
							.substring("findBy".length()));

					ILocalVariable[] params = method.getParameters();

					if (params.length == 1) {
						String paramTypeSignature = params[0].getTypeSignature();
						Method propertyMethod = null;
						try {
							propertyMethod = domainClass.getMethod("get"
									+ StringUtils.capitalize(propertyName));
						} catch (NoSuchMethodException e) {
							// not a property method... ignore
							continue;
						}
						
						if (propertyMethod != null) {
							Class<?> propertyReturnType = propertyMethod.getReturnType();
							String propertySimpleType = propertyReturnType.getSimpleName();
							String paramSimpleType = Signature.getSignatureSimpleName(paramTypeSignature);
							if (propertySimpleType != null && !(propertySimpleType.equals(paramSimpleType))) {
								element.setElementSourceLocation(new JavaModelSourceLocation(params[0]));
								ISourceRange paramSourceRange = params[0].getSourceRange();
								ValidationProblemAttribute start = new ValidationProblemAttribute(
										IMarker.CHAR_START,	paramSourceRange.getOffset());
								ValidationProblemAttribute end = new ValidationProblemAttribute(
										IMarker.CHAR_END, paramSourceRange.getOffset() + paramSourceRange.getLength());
								ValidationProblemAttribute problemId = new ValidationProblemAttribute(IMarker.PROBLEM, PROBLEM_ID);
								ValidationProblemAttribute propertyType = new ValidationProblemAttribute(PROPERTY_TYPE_ATTR, propertyReturnType.getSimpleName());
								
								String packageName = propertyReturnType.getPackage() != null ? propertyReturnType.getPackage().getName() : "";
								ValidationProblemAttribute propertyTypePackage = new ValidationProblemAttribute(PROPERTY_TYPE_PACKAGE_ATTR, packageName);

								context.warning(element, "SpringDataProbleMarker",
										"Parameter type (" + paramSimpleType + ") does not match domain class property definition (" + propertySimpleType + ").",
										new ValidationProblemAttribute[] {start, end, problemId, propertyType, propertyTypePackage});
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			SpringCore.log(e);
		} catch (SecurityException e) {
			SpringCore.log(e);
		}
	}

}
