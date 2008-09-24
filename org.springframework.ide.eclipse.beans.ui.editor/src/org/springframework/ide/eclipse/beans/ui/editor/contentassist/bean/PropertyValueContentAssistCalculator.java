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
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
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
@SuppressWarnings("restriction")
public class PropertyValueContentAssistCalculator extends ClassContentAssistCalculator implements
		IContentAssistCalculator {

	public PropertyValueContentAssistCalculator() {
		super(false);
	}

	public void computeProposals(ContentAssistRequest request, String matchString,
			String attributeName, String namespace, String namepacePrefix) {
		// If no matchString is given don't do any content assist calculation
		if (matchString == null || matchString.length() == 0) {
			return;
		}
		
		if (request.getParent() != null && request.getParent().getParentNode() != null
				&& "bean".equals(request.getParent().getParentNode().getLocalName())) {
			String propertyName = BeansEditorUtils.getAttribute(request.getNode(), "name");
			if (StringUtils.hasText(propertyName)) {
				String className = BeansEditorUtils.getClassNameForBean(BeansEditorUtils
						.getFile(request), request.getParent().getParentNode().getOwnerDocument(),
						request.getParent().getParentNode());
				IType type = JdtUtils.getJavaType(BeansEditorUtils.getFile(request).getProject(),
						className);
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
								super.computeProposals(request, matchString, attributeName,
										namespace, namepacePrefix);
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
