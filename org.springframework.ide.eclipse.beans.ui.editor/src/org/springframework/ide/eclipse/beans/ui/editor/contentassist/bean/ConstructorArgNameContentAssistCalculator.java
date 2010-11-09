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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.Set;

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
 * {@link IContentAssistCalculator} implementation that calculates proposals for constructor arg names.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class ConstructorArgNameContentAssistCalculator implements IContentAssistCalculator {

	public static final int CONSTRUCTOR_RELEVANCE = 10;

	private JavaElementImageProvider imageProvider = new JavaElementImageProvider();

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {

		if (context.getParentNode() != null
				&& "bean".equals(context.getParentNode().getLocalName())) {

			String className = BeansEditorUtils.getClassNameForBean(context.getFile(), context
					.getDocument(), context.getParentNode());
			IType type = JdtUtils.getJavaType(context.getFile().getProject(), className);
			if (type != null) {
			try {
				Set<IMethod> constructors = Introspector.getAllConstructors(type);
				for(IMethod constructor: constructors) {
					String[] paramNames = constructor.getParameterNames();
					for(String paramName: paramNames) {
						createMethodProposal(recorder, constructor, paramName);
					}
				}
			} catch (JavaModelException e) {
			}
			}
		}
	}

	protected void createMethodProposal(IContentAssistProposalRecorder recorder, IMethod method,
			String paramName) {
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = JdtUtils.getParameterTypesString(method);
//			String propertyName = JdtUtils.getPropertyNameFromMethodName(method);

			String replaceText = paramName;

			StringBuilder buf = new StringBuilder();
			buf.append(paramName);
			buf.append(" - ");
			buf.append(method.getParent().getElementName());
			buf.append('.');
			buf.append(method.getElementName());
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

			recorder.recordProposal(image, CONSTRUCTOR_RELEVANCE, displayText, replaceText, method);
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}

}
