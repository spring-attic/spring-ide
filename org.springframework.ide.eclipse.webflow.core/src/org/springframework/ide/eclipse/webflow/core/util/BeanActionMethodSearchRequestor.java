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
package org.springframework.ide.eclipse.webflow.core.util;


import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.MethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PublicMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;

/**
 * 
 */
@SuppressWarnings("restriction")
public class BeanActionMethodSearchRequestor extends
		PublicMethodSearchRequestor {

	/**
	 * 
	 */
	private final String EVENT_CLASS = "org.springframework.webflow.execution.Event";

	/**
	 * 
	 */
	private final String REQUEST_CONTEXT_CLASS = "org.springframework.webflow.execution.RequestContext";

	/**
	 * @param request
	 */
	public BeanActionMethodSearchRequestor(ContentAssistRequest request) {
		super(request);
	}

	/**
	 * Check methods for the following signatur public Event
	 * execute(RequestContext context) throws Exception;.
	 * @param prefix
	 * @param method
	 * @throws CoreException
	 */
	public void acceptSearchMatch(IMethod method, String prefix)
			throws CoreException {
		if (method.getElementName().toLowerCase().startsWith(
				prefix.toLowerCase())
				&& Flags.isPublic(method.getFlags())
				&& !Flags.isInterface(method.getFlags())
				&& method.exists()
				&& ((IType) method.getParent()).isClass()
				&& !method.isConstructor()
				&& method.getParameterTypes() != null
				&& method.getParameterTypes().length == 1) {
			IType type = (IType) method.getParent();

			IType returnType = BeansEditorUtils.getTypeForMethodReturnType(
					method, type);
			List<IType> parameterTypes = BeansEditorUtils
					.getTypeForMethodParameterTypes(method, type);
			if (returnType != null && parameterTypes.size() == 1
					&& parameterTypes.get(0) != null) {
				if (EVENT_CLASS.equals(returnType.getFullyQualifiedName())
						&& REQUEST_CONTEXT_CLASS.equals(parameterTypes.get(0)
								.getFullyQualifiedName())) {
					createMethodProposal(method,
							MethodSearchRequestor.METHOD_RELEVANCE);
				}
			}
		}
	}
}
