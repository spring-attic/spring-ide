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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link IContentAssistCalculator} that can be used to calculate proposals for
 * {@link IMethod}.
 * <p>
 * This implementation uses a customizable {@link IMethodFilter} to filter for
 * appropriate methods.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public abstract class MethodContentAssistCalculator implements
		IContentAssistCalculator {

	public static final int METHOD_RELEVANCE = 10;

	private final IMethodFilter filter;

	private final JavaElementImageProvider imageProvider;

	/**
	 * Constructor
	 * @param filter filter to be used for filtering {@link IMethod}s
	 */
	public MethodContentAssistCalculator(IMethodFilter filter) {
		this.filter = filter;
		this.imageProvider = new JavaElementImageProvider();
	}

	/**
	 * Calculate the {@link IType} that should be searched for matching methods.
	 * This method is intended to be implemented by subclasses as there is no
	 * common approach to locate the {@link IType}.
	 * @param request the content assist request to access meta data
	 * @param attributeName the name of the current attribute
	 * @return the {@link IType} that should be used for searching
	 */
	protected abstract IType calculateType(ContentAssistRequest request,
			String attributeName);

	/**
	 * Calculate proposals. This implementation calls {@link #calculateType} to
	 * get the root for the search and passes the returned {@link IType} and the
	 * instance's {@link IMethodFilter} to
	 * {@link Introspector#findAllMethods(IType, String, IMethodFilter)}.
	 * <p>
	 * If a match is found the {@link #createMethodProposal} is called to report
	 * the match as a proposal in the content assist request.
	 */
	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		for (IMethod method : Introspector.findAllMethods(calculateType(
				request, attributeName), matchString, filter)) {
			createMethodProposal(request, method);
		}
	}

	/**
	 * Create a {@link BeansJavaCompletionProposal} for the given
	 * {@link IMethod} and report it on the {@link ContentAssistRequest}.
	 */
	protected void createMethodProposal(ContentAssistRequest request,
			IMethod method) {
		try {
			String[] parameterNames = method.getParameterNames();
			String[] parameterTypes = JdtUtils.getParameterTypesString(method);
			String returnType = JdtUtils.getReturnTypeString(method, true);
			String methodName = method.getElementName();

			String replaceText = methodName;

			StringBuilder buf = new StringBuilder();

			// add method name
			buf.append(replaceText);

			// add method parameters
			if (parameterTypes.length > 0 && parameterNames.length > 0) {
				buf.append(" (");
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
				buf.append("() ");
			}

			// add return type
			if (returnType != null) {
				buf.append(Signature.getSimpleName(returnType));
				buf.append(" - ");
			}
			else {
				buf.append(" void - ");
			}

			// add class name
			buf.append(method.getParent().getElementName());

			String displayText = buf.toString();
			Image image = imageProvider.getImageLabel(method, method.getFlags()
					| JavaElementImageProvider.SMALL_ICONS);

			BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
					replaceText, request.getReplacementBeginPosition(), request
							.getReplacementLength(), replaceText.length(),
					image, displayText, null, METHOD_RELEVANCE, method);
			request.addProposal(proposal);
		}
		catch (JavaModelException e) {
			// do nothing
		}
	}
}
