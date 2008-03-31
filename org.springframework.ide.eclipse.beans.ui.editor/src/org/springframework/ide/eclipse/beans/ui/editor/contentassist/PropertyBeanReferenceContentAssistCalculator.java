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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link IContentAssistCalculator} that extends
 * {@link BeanReferenceContentAssistCalculator} in order to give bean reference
 * proposals that have a matching types a higher proposal priority.
 * @author Christian Dupuis
 * @since 2.0.4
 */
@SuppressWarnings("restriction")
public class PropertyBeanReferenceContentAssistCalculator extends
		BeanReferenceContentAssistCalculator {

	@Override
	public void computeProposals(ContentAssistRequest request, String matchString,
			String attributeName, String namespace, String namepacePrefix) {
		BeansCompletionUtils.addBeanReferenceProposals(request, matchString, request.getNode()
				.getOwnerDocument(), showExternal, calculateType(request, attributeName));
	}

	protected List<String> calculateType(ContentAssistRequest request, String attributeName) {
		if (request.getParent() != null && request.getParent().getParentNode() != null
				&& "bean".equals(request.getParent().getParentNode().getLocalName())) {

			String className = BeansEditorUtils.getClassNameForBean(BeansEditorUtils
					.getFile(request), request.getParent().getParentNode().getOwnerDocument(),
					request.getParent().getParentNode());
			IType type = JdtUtils.getJavaType(BeansEditorUtils.getFile(request).getProject(),
					className);

			String propertyName = BeansEditorUtils.getAttribute(request.getParent(), "name");
			if (propertyName != null) {
				try {
					IMethod method = Introspector.getWritableProperty(type, propertyName);
					if (method != null) {
						// It is safe to assume that we have one parameter since
						// this is a property setter
						String parameterTypeName = JdtUtils.resolveClassName(method
								.getParameterTypes()[0], type);
						if (parameterTypeName != null) {
							IType parameterType = JdtUtils.getJavaType(BeansEditorUtils.getFile(
									request).getProject(), parameterTypeName);
							return JdtUtils
									.getFlatListOfClassAndInterfaceNames(parameterType, type);
						}
					}
				}
				catch (JavaModelException e) {
				}
			}
		}
		return Collections.emptyList();
	}

}
