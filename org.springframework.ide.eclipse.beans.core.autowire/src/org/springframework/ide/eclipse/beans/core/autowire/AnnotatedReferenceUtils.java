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
package org.springframework.ide.eclipse.beans.core.autowire;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class for loading and storing annotation reference information.
 * @author Jared Rodriguez
 * @since 2.0.5
 * @deprecated As of 2.2.7 this is deprecated in flavor for the new autowiring support
 */
@Deprecated
public class AnnotatedReferenceUtils {

	/**
	 * Gets a fully qualified type name from a type signature.
	 * @param signature the type signature to resolve
	 * @return the fully qualified name or null if none found.
	 * @throws JavaModelException
	 */
	public static String getFullyQualifiedTypeName(String signature, IType type)
			throws JavaModelException {
		String packageName = null;
		String fullName = null;

		packageName = Signature.getSignatureQualifier(signature);
		fullName = (packageName.trim().equals("") ? "" : packageName + ".")
				+ Signature.getElementType(Signature.getSignatureSimpleName(signature));

		if (fullName.indexOf("[") > 0)
			fullName = fullName.substring(0, fullName.indexOf("["));

		String resolvedType = JdtUtils.resolveClassName(fullName, type);
		if (resolvedType != null && resolvedType.length() > 0)
			return resolvedType;
		return fullName;
	}

}
