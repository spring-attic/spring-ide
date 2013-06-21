/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor.hyperlink.batch;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.config.ui.hyperlinks.XmlBackedHyperlinkProvider;

/**
 * An {@link XmlBackedHyperlinkProvider} that uses
 * {@link StepReferenceHyperlinkCalculator} as its hyperlink calculator.
 * @author Leo Dos Santos
 * @since 2.1.0
 */
@SuppressWarnings("restriction")
public class StepReferenceHyperlinkProvider extends XmlBackedHyperlinkProvider {

	/**
	 * Constructs a hyperlink provider for an XML attribute.
	 * 
	 * @param textViewer the text viewer containing the XML source
	 * @param input the XML element to serve as the model for this hyperlink
	 * @param attrName the name of the attribute to compute a hyperlink action
	 */
	public StepReferenceHyperlinkProvider(ITextViewer textViewer, IDOMElement input, String attrName) {
		super(textViewer, input, attrName);
	}

	@Override
	protected IHyperlinkCalculator createHyperlinkCalculator() {
		return new StepReferenceHyperlinkCalculator();
	}

}
