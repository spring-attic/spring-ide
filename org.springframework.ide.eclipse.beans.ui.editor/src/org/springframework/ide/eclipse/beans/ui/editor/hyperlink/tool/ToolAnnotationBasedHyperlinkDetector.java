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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.tool;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractAnnotationBasedHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IAnnotationBasedHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * {@link IAnnotationBasedHyperlinkDetector} that reads out the tool annotations
 * and offers bean reference and java hyperlinks.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class ToolAnnotationBasedHyperlinkDetector extends
		AbstractAnnotationBasedHyperlinkDetector {

	@Override
	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor, Node annotation) {
		ToolAnnotationData annotationData = ToolAnnotationUtils
				.getToolAnnotationData(annotation);
		if ("ref".equals(annotationData.getKind())) {
			Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node
					.getOwnerDocument(), target);
			if (bean != null) {
				IRegion region = getHyperlinkRegion(bean);
				return new NodeElementHyperlink(hyperlinkRegion, region,
						textViewer);
			}
			else {
				IFile file = BeansEditorUtils.getFile(document);
				// assume this is an external reference
				Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(
						file).iterator();
				while (beans.hasNext()) {
					IBean modelBean = (IBean) beans.next();
					if (modelBean.getElementName().equals(target)) {
						return new ExternalBeanHyperlink(modelBean,
								hyperlinkRegion);
					}
				}
			}
		}
		else if (Class.class.getName().equals(annotationData.getExpectedType())) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		return null;
	}

	@Override
	public boolean isLinkableAttr(Attr attr, Node annotation) {
		if (ToolAnnotationUtils.ANNOTATION_ELEMENT.equals(annotation
				.getLocalName())
				&& ToolAnnotationUtils.TOOL_NAMESPACE_URI.equals(annotation
						.getNamespaceURI())) {
			ToolAnnotationData annotationData = ToolAnnotationUtils
					.getToolAnnotationData(annotation);
			return annotationData != null
					&& ("ref".equals(annotationData.getKind()) || Class.class
							.getName().equals(annotationData.getExpectedType()));
		}
		return false;
	}
}
