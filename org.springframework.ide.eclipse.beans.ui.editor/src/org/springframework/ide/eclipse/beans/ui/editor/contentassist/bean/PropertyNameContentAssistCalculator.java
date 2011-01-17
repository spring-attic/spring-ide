/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link IContentAssistCalculator} implementation that calculates proposals for property names.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class PropertyNameContentAssistCalculator implements IContentAssistCalculator {

	public static final int PROPERTY_RELEVANCE = 10;

	private JavaElementImageProvider imageProvider = new JavaElementImageProvider();

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {

		if (context.getParentNode() != null
				&& "bean".equals(context.getParentNode().getLocalName())) {

			String className = BeansEditorUtils.getClassNameForBean(context.getFile(), context
					.getDocument(), context.getParentNode());
			IType type = JdtUtils.getJavaType(context.getFile().getProject(), className);
			if (type != null) {
				addPropertyNameAttributeValueProposals(recorder, context.getMatchString(), "", type);
			}
		}
	}

	protected void addPropertyNameAttributeValueProposals(IContentAssistProposalRecorder recorder,
			String prefix, String oldPrefix, IType type) {

		// resolve type of indexed and nested property path
		if (prefix.lastIndexOf(".") >= 0) {
			int firstIndex = prefix.indexOf(".");
			String firstPrefix = prefix.substring(0, firstIndex);
			String lastPrefix = prefix.substring(firstIndex);
			if (".".equals(lastPrefix)) {
				lastPrefix = "";
			}
			else if (lastPrefix.startsWith(".")) {
				lastPrefix = lastPrefix.substring(1);
			}
			try {
				Collection<?> methods = Introspector
						.findReadableProperties(type, firstPrefix, true);
				if (methods != null && methods.size() == 1) {

					Iterator<?> iterator = methods.iterator();
					while (iterator.hasNext()) {
						IMethod method = (IMethod) iterator.next();
						IType returnType = JdtUtils.getJavaTypeForMethodReturnType(method, type);
						if (returnType != null) {
							String newPrefix = oldPrefix + firstPrefix + ".";

							addPropertyNameAttributeValueProposals(recorder, lastPrefix, newPrefix,
									returnType);
						}
						return;
					}
				}
			}
			catch (CoreException e) {
				// do nothing
			}
		}
		else {
			try {
				Collection<?> methods = Introspector.findWritableProperties(type, prefix, true);
				if (methods != null && methods.size() > 0) {
					Iterator<?> iterator = methods.iterator();
					while (iterator.hasNext()) {
						createMethodProposal(recorder, (IMethod) iterator.next(), oldPrefix);
					}
				}
			}
			catch (JavaModelException e1) {
				// do nothing
			}
		}
	}

	protected void createMethodProposal(IContentAssistProposalRecorder recorder, IMethod method,
			String prefix) {
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = JdtUtils.getParameterTypesString(method);
			String propertyName = JdtUtils.getPropertyNameFromMethodName(method);
			
			String methodParentName = JdtUtils.getParentName(method);
			String methodName = JdtUtils.getMethodName(method);

			String replaceText = prefix + propertyName;

			StringBuilder buf = new StringBuilder();
			buf.append(propertyName);
			buf.append(" - ");
			buf.append(methodParentName);
			buf.append('.');
			buf.append(methodName);
			buf.append('(');
			if (parameterTypes != null && parameterNames != null && parameterTypes.length > 0
					&& parameterNames.length > 0) {
				buf.append(parameterTypes[0]);
				buf.append(' ');
				buf.append(parameterNames[0]);
			}
			buf.append(')');
			String displayText = buf.toString();

			Image image = imageProvider.getImageLabel(method, method.getFlags()
					| JavaElementImageProvider.SMALL_ICONS);

			recorder.recordProposal(image, PROPERTY_RELEVANCE, displayText, replaceText, method);
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}

}
