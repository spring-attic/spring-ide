/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.MethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PublicMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;

@SuppressWarnings("restriction")
public class BeanActionMethodSearchRequestor extends PublicMethodSearchRequestor {

	private final String EVENT_CLASS = "org.springframework.webflow.execution.Event";

	private final String REQUEST_CONTEXT_CLASS = "org.springframework.webflow.execution.RequestContext";

	public BeanActionMethodSearchRequestor(ContentAssistRequest request) {
		super(request);
	}

	/**
	 * Check methods for the following signatur public Event
	 * execute(RequestContext context) throws Exception;
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
					createMethodProposal(method, MethodSearchRequestor.METHOD_RELEVANCE);
				}
			}
		}
	}
}
