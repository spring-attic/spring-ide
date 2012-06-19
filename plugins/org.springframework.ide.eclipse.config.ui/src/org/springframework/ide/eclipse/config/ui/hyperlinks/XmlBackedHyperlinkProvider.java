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
package org.springframework.ide.eclipse.config.ui.hyperlinks;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.HyperlinkUtils;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * {@link XmlBackedHyperlinkProvider} provides hyperlink actions for XML
 * attributes outside of the source viewer.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class XmlBackedHyperlinkProvider {

	private final ITextViewer textViewer;

	private final IDOMElement input;

	private final String name;

	/**
	 * Constructs a hyperlink provider for an XML attribute.
	 * 
	 * @param textViewer the text viewer containing the XML source
	 * @param input the XML element to serve as the model for this hyperlink
	 * provider
	 * @param attrName the name of the attribute to compute a hyperlink action
	 * for
	 */
	public XmlBackedHyperlinkProvider(ITextViewer textViewer, IDOMElement input, String attrName) {
		this.textViewer = textViewer;
		this.input = input;
		this.name = attrName;
	}

	protected IHyperlink createHyperlink(String value, IRegion region) {
		IHyperlinkCalculator calc = createHyperlinkCalculator();
		IHyperlink hyperlink = calc.createHyperlink(name, value, input, input.getParentNode(), input
				.getStructuredDocument(), textViewer, region, region);

		IFile file = BeansEditorUtils.getFile(input.getStructuredDocument());
		if (hyperlink instanceof NodeElementHyperlink) {
			Node bean = BeansEditorUtils.getFirstReferenceableNodeById(input.getOwnerDocument(), value, file);
			if (bean != null) {
				IRegion targetRegion = HyperlinkUtils.getHyperlinkRegion(bean);
				return new ExtendedNodeElementHyperlink(bean, region, targetRegion, textViewer);
			}
		}
		if (hyperlink instanceof ExternalBeanHyperlink) {
			Iterator<IBean> beans = BeansEditorUtils.getBeansFromConfigSets(file).iterator();
			while (beans.hasNext()) {
				IBean modelBean = beans.next();
				if (modelBean.getElementName().equals(value)) {
					return new ExtendedExternalBeanHyperlink(modelBean, region);
				}
			}
		}
		return hyperlink;
	}

	/**
	 * This method is called when a request is made to open the hyperlink
	 * provider. Clients must provide an appropriate
	 * {@link IHyperlinkCalculator} in this method.
	 * 
	 * @return hyperlink calculator for this provider
	 */
	protected abstract IHyperlinkCalculator createHyperlinkCalculator();

	/**
	 * Returns the attribute to compute a hyperlink for, or null if the
	 * attribute does not exist.
	 * 
	 * @return the attribute to compute a hyperlink for
	 */
	protected IDOMAttr getAttribute() {
		Attr attrNode = input.getAttributeNode(name);
		if (attrNode != null) {
			return (IDOMAttr) attrNode;
		}
		return null;
	}

	private ITextRegion getCompletionRegion() {
		IDOMAttr attr = getAttribute();
		if (attr != null) {
			return attr.getValueRegion();
		}
		return null;
	}

	/**
	 * Creates an {@link IHyperlink} for the give text value and opens it.
	 * Returns true if the hyperlink was successfully created and opened, false
	 * otherwise.
	 * 
	 * @param value the attribute value to create a hyperlink for
	 * @return true if the hyperlink opened successfully, false otherwise
	 */
	public boolean open(String value) {
		IRegion region = null;
		if (getAttribute() != null && getCompletionRegion() != null) {
			int start = input.getStartStructuredDocumentRegion().getStart() + getCompletionRegion().getStart();
			region = new Region(start, getCompletionRegion().getLength());
		}

		IHyperlink hyperlink = createHyperlink(value, region);
		if (hyperlink != null) {
			hyperlink.open();
			return true;
		}
		return false;
	}

}
