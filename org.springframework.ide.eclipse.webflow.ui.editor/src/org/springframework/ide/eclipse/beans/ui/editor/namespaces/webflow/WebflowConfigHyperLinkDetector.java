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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.webflow;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection of bean classes and bean
 * properties in attribute values. Resolves bean references (including
 * references to parent beans or factory beans).
 * 
 * @author Christian Dupuis
 */
public class WebflowConfigHyperLinkDetector extends AbstractHyperlinkDetector implements IHyperlinkDetector {

	/**
	 * Returns <code>true</code> if given attribute is openable.
	 * 
	 * @param attr 
	 * 
	 * @return 
	 */
	@Override
	protected boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();
		return ("registry-ref".equals(attrName) || "conversation-manager-ref".equals(attrName)
				|| "ref".equals(attrName) || "type".equals(attrName));
	}

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector#createHyperlink(java.lang.String, java.lang.String, org.w3c.dom.Node, org.eclipse.jface.text.IRegion, org.eclipse.jface.text.IDocument, org.w3c.dom.Node, org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected IHyperlink createHyperlink(String name, String target, Node parentNode, IRegion hyperlinkRegion,
			IDocument document, Node node, ITextViewer textViewer, IRegion cursor) {
		if (name == null) {
			return null;
		}
		if ("type".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		else if ("registry-ref".equals(name) || "conversation-manager-ref".equals(name) || "ref".equals(name)) {
			Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node.getOwnerDocument(), target);
			if (bean != null) {
				IRegion region = getHyperlinkRegion(bean);
				return new NodeElementHyperlink(hyperlinkRegion, region, textViewer);
			}
			else {
				IFile file = BeansEditorUtils.getFile(document);
				// assume this is an external reference
				Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(file).iterator();
				while (beans.hasNext()) {
					IBean modelBean = (IBean) beans.next();
					if (modelBean.getElementName().equals(target)) {
						return new ExternalBeanHyperlink(modelBean, hyperlinkRegion);
					}
				}
			}
		}
		return null;
	}
}
