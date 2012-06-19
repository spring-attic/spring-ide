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
package org.springframework.ide.eclipse.config.core.contentassist;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;

/**
 * A pre-configured content proposal adapter that is set to use an
 * {@link XmlBackedContentProposalProvider} as its proposal provider.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @see ContentProposalAdapter
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public class XmlBackedContentProposalAdapter extends ContentProposalAdapter {

	private final Control control;

	private final XmlBackedContentProposalProvider xmlProposalProvider;

	/**
	 * Constructs a content proposal adapter that can assist the user with
	 * choosing content for the field. Content proposals are invoked through the
	 * use of "Ctrl+Space".
	 * 
	 * @param control the control for which the adapter is providing content
	 * assist. May not be null.
	 * @param contentAdapter the {@link IControlContentAdapter} used to obtain
	 * and update the control's contents as proposals are accepted. May not be
	 * <code>null</code>.
	 * @param proposalProvider the {@link XmlBackedContentProposalProvider} used
	 * to obtain content proposals for this control, or null if no content
	 * proposal is available.
	 */
	public XmlBackedContentProposalAdapter(Control control, IControlContentAdapter contentAdapter,
			XmlBackedContentProposalProvider proposalProvider) {
		this(control, contentAdapter, proposalProvider, KeyStroke.getInstance(SWT.CTRL, ' '));
	}

	/**
	 * Constructs a content proposal adapter that can assist the user with
	 * choosing content for the field. Clients may use this constructor to
	 * define their own keystroke for invoking the content proposals.
	 * 
	 * @param control the control for which the adapter is providing content
	 * assist. May not be null.
	 * @param contentAdapter the {@link IControlContentAdapter} used to obtain
	 * and update the control's contents as proposals are accepted. May not be
	 * <code>null</code>.
	 * @param proposalProvider the {@link XmlBackedContentProposalProvider} used
	 * to obtain content proposals for this control, or null if no content
	 * proposal is available.
	 * @param keyStroke the key combination used to invoke the content proposal
	 * popup
	 */
	public XmlBackedContentProposalAdapter(Control control, IControlContentAdapter contentAdapter,
			XmlBackedContentProposalProvider proposalProvider, KeyStroke keyStroke) {
		super(control, contentAdapter, proposalProvider, keyStroke, null);
		this.control = control;
		this.xmlProposalProvider = proposalProvider;
		setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		setLabelProvider(new XmlBackedContentProposalLabelProvider());
		addControlDecoration();
	}

	private void addControlDecoration() {
		ControlDecoration controlDec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDec = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		controlDec.setImage(fieldDec.getImage());
		controlDec.setShowOnlyOnFocus(true);
	}

	/**
	 * Updates the proposal provider with the new model from which content
	 * proposals will be obtained.
	 * 
	 * @param input the XML element to serve as the model for the proposal
	 * provider
	 */
	public void update(IDOMElement input) {
		if (xmlProposalProvider != null) {
			xmlProposalProvider.setInput(input);
		}
	}

}
