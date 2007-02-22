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

package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;

@SuppressWarnings("restriction")
public class PropertyValueSearchRequestor {

	public static final int METHOD_RELEVANCE = 10;

	protected JavaElementImageProvider imageProvider;

	protected Map<String, IMethod> methods;

	protected ContentAssistRequest request;

	private String prefix;

	public PropertyValueSearchRequestor(ContentAssistRequest request,
			String prefix) {
		this.request = request;
		this.methods = new HashMap<String, IMethod>();
		this.imageProvider = new JavaElementImageProvider();
		this.prefix = prefix;
	}

	public void acceptSearchMatch(IMethod method, boolean external)
			throws CoreException {
		int parameterCount = method.getNumberOfParameters();
		String returnType = method.getReturnType();
		if (Flags.isPublic(method.getFlags())
				&& !Flags.isInterface(method.getFlags()) && parameterCount == 1
				&& "V".equals(returnType) && method.exists()
				&& ((IType) method.getParent()).isClass()
				&& !method.isConstructor()) {
			createMethodProposal(method, external);
		}
	}

	protected void createMethodProposal(IMethod method, boolean external) {
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = getParameterTypes(method);
			String key = method.getElementName() + method.getSignature();
			if (!methods.containsKey(key)) {
				String propertyName = getPropertyNameFromMethodName(method);
				String replaceText = prefix + propertyName;
				StringBuffer buf = new StringBuffer();
				buf.append(propertyName);
				buf.append(" - ");
				buf.append(method.getParent().getElementName());
				buf.append('.');
				buf.append(method.getElementName());
				buf.append('(');
				buf.append(parameterTypes[0]);
				buf.append(' ');
				buf.append(parameterNames[0]);
				buf.append(')');
				String displayText = buf.toString();
				Image image = imageProvider.getImageLabel(method, method
						.getFlags()
						| JavaElementImageProvider.SMALL_ICONS);
				if (external) {
					image = BeansModelImages.getDecoratedImage(image,
							BeansModelImages.FLAG_EXTERNAL);
				}
				BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
				String javadoc = utils.getJavaDoc();

				BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
						replaceText, request.getReplacementBeginPosition(),
						request.getReplacementLength(), replaceText.length(),
						image, displayText, null, javadoc,
						PropertyValueSearchRequestor.METHOD_RELEVANCE);

				request.addProposal(proposal);
				methods.put(key, method);
			}
		}
		catch (JavaModelException e) {
			// do nothing
		}
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

	protected String getPropertyNameFromMethodName(IMethod method) {
		String replaceText = method.getElementName().substring("set".length(),
				method.getElementName().length());
		if (replaceText != null) {
			char c = replaceText.charAt(0);
			replaceText = replaceText.substring(1, replaceText.length());
			replaceText = Character.toLowerCase(c) + replaceText;
		}
		return replaceText;
	}

	protected String getReturnType(IMethod method) {
		try {
			String parameterQualifiedTypes = Signature.getReturnType(method
					.getSignature());
			IType type = (IType) method.getParent();
			String tempString = Signature
					.getSignatureSimpleName(parameterQualifiedTypes);
			String[][] parameterPackages = type.resolveType(tempString);
			if (parameterPackages != null) {
				if (parameterPackages[0][0].length() > 0) {
					return parameterPackages[0][0] + "."
							+ parameterPackages[0][1];
				}
				else {
					return parameterPackages[0][1];
				}
			}
		}
		catch (IllegalArgumentException e) {
		}
		catch (JavaModelException e) {
		}
		return null;
	}
}