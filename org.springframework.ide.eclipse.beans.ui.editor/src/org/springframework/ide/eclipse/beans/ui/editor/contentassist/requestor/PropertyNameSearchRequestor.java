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
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaDocUtils;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;

@SuppressWarnings("restriction")
public class PropertyNameSearchRequestor {

	public static final int METHOD_RELEVANCE = 10;

	protected JavaElementImageProvider imageProvider;

	protected Map<String, IMethod> methods;

	protected ContentAssistRequest request;

	private String prefix;

	private boolean attrAtLocationHasValue;

	private String nameSpacePrefix = "";

	public PropertyNameSearchRequestor(ContentAssistRequest request, String prefix, boolean attrAtLocationHasValue,
			String nameSpacePrefix) {
		this.request = request;
		this.methods = new HashMap<String, IMethod>();
		this.imageProvider = new JavaElementImageProvider();
		this.prefix = prefix;
		this.attrAtLocationHasValue = attrAtLocationHasValue;
		if (nameSpacePrefix != null) {
			this.nameSpacePrefix = nameSpacePrefix + ":";
		}
	}

	public void acceptSearchMatch(IMethod method, boolean external) throws CoreException {
		int parameterCount = method.getNumberOfParameters();
		String returnType = method.getReturnType();
		if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags()) && parameterCount == 1
				&& "V".equals(returnType) && method.exists() && ((IType) method.getParent()).isClass()
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
				String replaceText = nameSpacePrefix + prefix + propertyName;
				StringBuffer buf = new StringBuffer();
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
				Image image = imageProvider.getImageLabel(method, method.getFlags()
						| JavaElementImageProvider.SMALL_ICONS);
				BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
				String javadoc = utils.getJavaDoc();

				int cursor = replaceText.length();
				String refReplaceText = replaceText + "-ref";
				if (!attrAtLocationHasValue) {
					replaceText += "=\"\"";
					cursor = replaceText.length() - 1;
				}
				else {
					cursor = replaceText.length() + 2;
				}

				CustomCompletionProposal proposal1 = new CustomCompletionProposal(replaceText, request
						.getReplacementBeginPosition(), request.getReplacementLength(), cursor, image, nameSpacePrefix
						+ propertyName + displayText, null, javadoc, PropertyNameSearchRequestor.METHOD_RELEVANCE);

				if (!attrAtLocationHasValue) {
					refReplaceText += "=\"\"";
					cursor = refReplaceText.length() - 1;
				}
				else {
					cursor = refReplaceText.length() + 2;
				}
				image = BeansModelImages.getDecoratedImage(image, BeansModelImages.FLAG_EXTERNAL);
				CustomCompletionProposal proposal2 = new CustomCompletionProposal(refReplaceText, request
						.getReplacementBeginPosition(), request.getReplacementLength(), cursor, image, nameSpacePrefix
						+ propertyName + "-ref" + displayText, null, javadoc,
						PropertyNameSearchRequestor.METHOD_RELEVANCE);

				request.addProposal(proposal1);
				request.addProposal(proposal2);
				methods.put(key, method);
			}
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}

	protected String[] getParameterTypes(IMethod method) {
		try {
			String[] parameterQualifiedTypes = Signature.getParameterTypes(method.getSignature());
			int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
			String[] parameterPackages = new String[length];
			for (int i = 0; i < length; i++) {
				parameterQualifiedTypes[i] = parameterQualifiedTypes[i].replace('/', '.');
				parameterPackages[i] = Signature.getSignatureSimpleName(parameterQualifiedTypes[i]);
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
		String replaceText = method.getElementName().substring("set".length(), method.getElementName().length());
		if (replaceText != null) {
			char c = replaceText.charAt(0);
			replaceText = replaceText.substring(1, replaceText.length());
			replaceText = Character.toLowerCase(c) + replaceText;
			replaceText = BeansEditorUtils.propertyNameToAttributeName(replaceText);
		}
		return replaceText;
	}

	protected String getReturnType(IMethod method) {
		try {
			String parameterQualifiedTypes = Signature.getReturnType(method.getSignature());
			IType type = (IType) method.getParent();
			String tempString = Signature.getSignatureSimpleName(parameterQualifiedTypes);
			String[][] parameterPackages = type.resolveType(tempString);
			if (parameterPackages != null) {
				if (parameterPackages[0][0].length() > 0) {
					return parameterPackages[0][0] + "." + parameterPackages[0][1];
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