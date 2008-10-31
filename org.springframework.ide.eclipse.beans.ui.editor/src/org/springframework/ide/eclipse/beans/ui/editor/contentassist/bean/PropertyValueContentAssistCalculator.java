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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;

/**
 * Extension of {@link ClassContentAssistCalculator} that is applied to the name attribute of the
 * property element.
 * <p>
 * This implementation checks the type of the java bean setter and if the type is {@link String} or
 * {@link Class} a class content assist is available.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class PropertyValueContentAssistCalculator extends ClassContentAssistCalculator implements
		IContentAssistCalculator {

	public PropertyValueContentAssistCalculator() {
		super(false);
	}

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String matchString = context.getMatchString();
		// If no matchString is given don't do any content assist calculation
		if (matchString == null || matchString.length() == 0) {
			return;
		}

		if (context.getParentNode() != null
				&& "bean".equals(context.getParentNode().getLocalName())) {
			String propertyName = BeansEditorUtils.getAttribute(context.getNode(), "name");
			if (StringUtils.hasText(propertyName)) {
				String className = BeansEditorUtils.getClassNameForBean(context.getFile(), context
						.getDocument(), context.getParentNode());
				IType type = JdtUtils.getJavaType(context.getFile().getProject(), className);
				if (type != null) {
					try {
						IMethod method = Introspector.getWritableProperty(type, propertyName);
						if (method != null) {
							// Since we got a java bean setter there needs to one parameter
							String parameterType = JdtUtils.resolveClassName(method
									.getParameterTypes()[0], type);

							// Class and String can be converted in Class instances
							if (Class.class.getName().equals(parameterType)
									|| String.class.getName().equals(parameterType)) {
								super.computeProposals(context, recorder);
							}
						}
					}
					catch (JavaModelException e) {
						// do nothing
					}
				}
			}
		}
	}

}
