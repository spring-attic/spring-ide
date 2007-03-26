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
		if (Flags.isPublic(method.getFlags())
				&& !Flags.isInterface(method.getFlags()) && method.exists()
				&& ((IType) method.getParent()).isClass()
				&& !method.isConstructor()) {
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
				Image image = imageProvider.getImageLabel(method, method
						.getFlags()
						| JavaElementImageProvider.SMALL_ICONS);
				BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
				String javadoc = utils.getJavaDoc();

				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
						replaceText, request.getReplacementBeginPosition(),
						request.getReplacementLength(), replaceText.length(),
						image, displayText, null, javadoc, relevance);

				request.addProposal(proposal);
				methods.add(method.getSignature());
			}
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}
}
