/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.proposals;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.refresh.RefreshUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Quick fix proposal for creating a new bean
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class CreateNewBeanQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final String beanName;

	private final IDOMNode beanNode;

	private int cursorPosition = -1;

	public CreateNewBeanQuickFixProposal(int offset, int length, boolean missingEndQuote, String beanName,
			IDOMNode beanNode) {
		super(offset, length, missingEndQuote);
		this.beanName = beanName;
		this.beanNode = beanNode;
	}

	private void addNode(Element newBean, Node nextNode, IFile file, IDOMElement beansNode) {
		if (nextNode == null) {
			beansNode.appendChild(newBean);
		}
		else {
			beansNode.insertBefore(newBean, nextNode);
		}

		RefreshUtils.refreshCurrentEditor(file);
	}

	@Override
	public void applyQuickFix(IDocument document) {
		Node parentNode = beanNode.getParentNode();
		IDOMElement beansNode = (IDOMElement) beanNode.getOwnerDocument().getDocumentElement();
		Node currentNode = beanNode;

		while (parentNode != null && !parentNode.equals(beansNode)) {
			currentNode = parentNode;
			parentNode = parentNode.getParentNode();
		}

		Node nextNode = currentNode.getNextSibling();
		Document ownerDocument = beansNode.getOwnerDocument();

		Element newBean = createNewBean(ownerDocument);
		Attr classAttribute = createClassAttribute(newBean, ownerDocument);
		addNode(newBean, nextNode, BeansEditorUtils.getFile(document), beansNode);
		try {
			document.replace(getOffset(), 0, "");

			ShallowFormatProcessorXML formatter = new ShallowFormatProcessorXML();
			formatter.formatNode(newBean);

			if (parentNode instanceof IDOMElement) {
				int docLength = document.getLength();
				int startOffset = ((IDOMElement) newBean).getStartOffset() - 1;
				int length = ((IDOMElement) newBean).getLength() + 2;
				formatter.formatDocument(document, startOffset >= 0 ? startOffset : 0,
						length + startOffset <= docLength ? length : docLength - startOffset);
			}

			if (classAttribute instanceof AttrImpl && newBean instanceof IDOMNode) {
				cursorPosition = ((IDOMNode) newBean).getStartOffset()
						+ ((AttrImpl) classAttribute).getValueRegion().getTextEnd() - 1;
			}
		}
		catch (BadLocationException e) {
		}
		catch (IOException e) {
		}
		catch (CoreException e) {
		}
	}

	private Attr createClassAttribute(Element newBean, Document ownerDocument) {
		Attr classAttribute = ownerDocument.createAttribute("class");
		classAttribute.setValue("");
		newBean.setAttributeNode(classAttribute);
		return classAttribute;
	}

	private Element createNewBean(Document ownerDocument) {
		String localName = BeansSchemaConstants.ELEM_BEAN;
		String tagName = localName;
		String prefix = ConfigCoreUtils
				.getPrefixForNamespaceUri((IDOMDocument) ownerDocument, BeansSchemaConstants.URI);
		if (prefix != null && prefix.length() > 0) {
			tagName = prefix + ":" + localName;
		}

		Element newBean = ownerDocument.createElement(tagName);

		Attr idAttribute = ownerDocument.createAttribute("id");
		idAttribute.setValue(beanName);
		newBean.setAttributeNode(idAttribute);

		return newBean;
	}

	public String getDisplayString() {
		return "Create missing bean \'" + beanName + "\'";
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);
	}

	@Override
	public Point getSelection(IDocument document) {
		if (cursorPosition < 0) {
			return super.getSelection(document);
		}

		return new Point(cursorPosition, 0);
	}

	@Override
	public void run(IMarker marker) {
		try {
			String beanName = (String) marker.getAttribute("BEAN");
			if (beanName != null) {
				IDocument document = QuickfixUtils.getDocument(marker);
				if (document != null) {
					applyQuickFix(document);
				}
			}
		}
		catch (CoreException e) {

		}
	}
}