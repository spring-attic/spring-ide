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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

@SuppressWarnings("restriction")
public abstract class MethodSearchRequestor {

	public static final int METHOD_RELEVANCE = 10;

	protected ContentAssistRequest request;

	protected JavaElementImageProvider imageProvider;

	protected Set<String> methods;

	public MethodSearchRequestor(ContentAssistRequest request) {
		this.request = request;
		this.methods = new HashSet<String>();
		this.imageProvider = new JavaElementImageProvider();
	}

	protected String[] getParameterTypes(IMethod method) {
		try {
			String[] parameterQualifiedTypes = Signature
					.getParameterTypes(method.getSignature());
			int length = parameterQualifiedTypes == null ? 0
					: parameterQualifiedTypes.length;
			String[] parameterPackages = new String[length];
			for (int i = 0; i < length; i++) {
				parameterQualifiedTypes[i] = parameterQualifiedTypes[i]
						.replace('/', '.');
				parameterPackages[i] = Signature
						.getSignatureSimpleName(parameterQualifiedTypes[i]);
			}
			return parameterPackages;
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	protected String getReturnType(IMethod method, boolean classTypesOnly) {
		try {
			String qualifiedReturnType = Signature.getReturnType(method
					.getSignature());
			if (!classTypesOnly || qualifiedReturnType.startsWith("L")
					|| qualifiedReturnType.startsWith("Q")) {
				return Signature.getSignatureSimpleName(qualifiedReturnType
						.replace('/', '.'));
			}
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}
}
