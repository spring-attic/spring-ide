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
package org.springframework.ide.eclipse.config.ui.widgets;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalAdapter;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalProvider;


/**
 * A pre-configured content proposal adapter for a text field that is set to use
 * an {@link XmlBackedContentProposalProvider} as its proposal provider.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @see ContentProposalAdapter
 * @since 2.0.0
 */
public class TextAttributeProposalAdapter extends XmlBackedContentProposalAdapter {

	/**
	 * Constructs a content proposal adapter that can assist the user with
	 * choosing content for the field. Content proposals are invoked through the
	 * use of "Ctrl+Space".
	 * 
	 * @param attrControl the {@link TextAttribute} widget set for which the
	 * adapter is providing content
	 * @param proposalProvider the {@link XmlBackedContentProposalProvider} used
	 * to obtain content proposals for this control, or null if no content
	 */
	public TextAttributeProposalAdapter(TextAttribute attrControl, XmlBackedContentProposalProvider proposalProvider) {
		super(attrControl.getTextControl(), new TextContentAdapter(), proposalProvider);
	}

}
