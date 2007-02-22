/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaDocUtils;

@SuppressWarnings("restriction")
public class PublicMethodSearchRequestor extends MethodSearchRequestor {

	public PublicMethodSearchRequestor(ContentAssistRequest request) {
		super(request);
	}

	public void acceptSearchMatch(IMethod method) throws CoreException {
		if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags()) && method.exists()
				&& ((IType) method.getParent()).isClass() && !method.isConstructor()) {
			createMethodProposal(method, MethodSearchRequestor.METHOD_RELEVANCE);
		}
	}

	protected void createMethodProposal(IMethod method, int relevance) {
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = getParameterTypes(method);
			String returnType = getReturnType(method, true);
			String key = method.getElementName() + method.getSignature();
			if (!methods.contains(key)) {
				String methodName = method.getElementName();
				String replaceText = methodName;
				StringBuffer buf = new StringBuffer();
				if (parameterTypes.length > 0 && parameterNames.length > 0) {
					buf.append(replaceText + "(");
					for (int i = 0; i < parameterTypes.length; i++) {
						buf.append(parameterTypes[i]);
						buf.append(' ');
						buf.append(parameterNames[i]);
						if (i < (parameterTypes.length - 1)) {
							buf.append(", ");
						}
					}
					buf.append(") ");
				}
				else {
					buf.append(replaceText);
					buf.append("() ");
				}

				if (returnType != null) {
					buf.append(Signature.getSimpleName(returnType));
					buf.append(" - ");
				}
				else {
					buf.append(" void - ");
				}
				buf.append(method.getParent().getElementName());

				String displayText = buf.toString();
				Image image = imageProvider.getImageLabel(method, method.getFlags()
						| JavaElementImageProvider.SMALL_ICONS);
				BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
				String javadoc = utils.getJavaDoc();

				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(replaceText, request
						.getReplacementBeginPosition(), request.getReplacementLength(), replaceText.length(), image,
						displayText, null, javadoc, relevance);

				request.addProposal(proposal);
				methods.add(method.getSignature());
			}
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}
}