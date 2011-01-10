/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class that creates content assist proposals for attribute names used
 * in the p namespace.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class PropertyNameSearchRequestor {

	public static final int PROPERTY_RELEVANCE = 10;

	protected JavaElementImageProvider imageProvider;

	protected Map<String, IMethod> methods;

	protected ContentAssistRequest request;

	private String prefix;

	private boolean attrAtLocationHasValue;

	private String namespacePrefix = "";

	public PropertyNameSearchRequestor(ContentAssistRequest request,
			String prefix, boolean attrAtLocationHasValue,
			String nameSpacePrefix) {
		this.request = request;
		this.methods = new HashMap<String, IMethod>();
		this.imageProvider = new JavaElementImageProvider();
		this.prefix = prefix;
		this.attrAtLocationHasValue = attrAtLocationHasValue;
		if (nameSpacePrefix != null) {
			this.namespacePrefix = nameSpacePrefix + ":";
		}
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
			String[] parameterTypes = JdtUtils.getParameterTypesString(method);
			String key = method.getElementName() + method.getSignature();
			if (!methods.containsKey(key)) {
				String propertyName = JdtUtils
						.getPropertyNameFromMethodName(method);
				String replaceText = namespacePrefix + prefix + propertyName;
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

				Image image = imageProvider.getImageLabel(method, method
						.getFlags()
						| JavaElementImageProvider.SMALL_ICONS);

				int cursor = replaceText.length();
				String refReplaceText = replaceText + "-ref";
				if (!attrAtLocationHasValue) {
					replaceText += "=\"\"";
					cursor = replaceText.length() - 1;
				}
				else {
					cursor = replaceText.length() + 2;
				}

				CustomCompletionProposal proposal1 = new CustomCompletionProposal(
						replaceText, request.getReplacementBeginPosition(),
						request.getReplacementLength(), cursor, image,
						namespacePrefix + propertyName + displayText, null,
						null, PROPERTY_RELEVANCE);

				if (!attrAtLocationHasValue) {
					refReplaceText += "=\"\"";
					cursor = refReplaceText.length() - 1;
				}
				else {
					cursor = refReplaceText.length() + 2;
				}
				image = BeansModelImages.getDecoratedImage(image,
						BeansModelImages.FLAG_EXTERNAL);
				CustomCompletionProposal proposal2 = new CustomCompletionProposal(
						refReplaceText, request.getReplacementBeginPosition(),
						request.getReplacementLength(), cursor, image,
						namespacePrefix + propertyName + "-ref" + displayText,
						null, null, PROPERTY_RELEVANCE);

				request.addProposal(proposal1);
				request.addProposal(proposal2);
				methods.put(key, method);
			}
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}
}
