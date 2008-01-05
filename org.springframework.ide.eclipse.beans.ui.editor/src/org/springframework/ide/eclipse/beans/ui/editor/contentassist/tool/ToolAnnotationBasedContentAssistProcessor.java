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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.tool;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IAnnotationBasedContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils.ToolAnnotationData;
import org.w3c.dom.Node;

/**
 * TODO
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
public class ToolAnnotationBasedContentAssistProcessor implements
		IAnnotationBasedContentAssistProcessor {

	private static final IContentAssistCalculator BEAN_REFERENCE_CALCULATOR = new BeanReferenceContentAssistCalculator(
			true);

	private static final IContentAssistCalculator CLASS_CALCULATOR = new ClassContentAssistCalculator();

	
	public void addAttributeValueProposals(
			IContentAssistProcessor delegatingContentAssistProcessor,
			ContentAssistRequest request, Node annotation) {
		if (ToolAnnotationUtils.ANNOTATION_ELEMENT.equals(annotation
				.getLocalName())
				&& ToolAnnotationUtils.TOOL_NAMESPACE_URI.equals(annotation
						.getNamespaceURI())) {

			ToolAnnotationData annotationData = ToolAnnotationUtils
					.getToolAnnotationData(annotation);

			String matchString = BeansEditorUtils.prepareMatchString(request);

			if ("ref".equals(annotationData.getKind())) {
				// bean reference content assist
				// TODO CD: add support for typed reference content assist
				BEAN_REFERENCE_CALCULATOR.computeProposals(request,
						matchString, null, null, null);
			}
			else if (Class.class.getName().equals(
					annotationData.getExpectedType())) {
				// class content assist
				if (annotationData.getAssignableTo() == null) {
					CLASS_CALCULATOR.computeProposals(request, matchString,
							null, null, null);
				}
				else {
					new ClassHierachyContentAssistCalculator(annotationData
							.getAssignableTo()).computeProposals(request,
							matchString, null, null, null);
				}
			}
		}
	}

	public void init() {
		// nothing to do
	}

}
