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
package org.springframework.ide.eclipse.batch.ui.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.batch.ui.editor.contentassist.batch.StepReferenceContentProposalProvider;
import org.springframework.ide.eclipse.batch.ui.editor.hyperlink.batch.StepReferenceHyperlinkProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.BeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.hyperlinks.XmlBackedHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.widgets.HyperlinkedTextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class BatchDetailsSectionPart extends SpringConfigDetailsSectionPart {

	private FormToolkit toolkit;
	
	public BatchDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent, FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
		this.toolkit = toolkit;
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// BatchContentAssistProcessor and BatchHyperlinkDetector
		String elem = getInput().getLocalName();
		if ((BatchSchemaConstants.ELEM_NEXT.equals(elem) && BatchSchemaConstants.ATTR_TO.equals(attr))
				|| (BatchSchemaConstants.ELEM_STOP.equals(elem) && BatchSchemaConstants.ATTR_RESTART.equals(attr))
				|| (BatchSchemaConstants.ELEM_SPLIT.equals(elem) && BatchSchemaConstants.ATTR_NEXT.equals(attr))
				|| (BatchSchemaConstants.ELEM_STEP.equals(elem) && BatchSchemaConstants.ATTR_NEXT.equals(attr))) {
			TextAttribute attrControl = createStepAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new StepReferenceContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		if ((BatchSchemaConstants.ELEM_JOB.equals(elem) && BatchSchemaConstants.ATTR_PARENT.equals(attr))
				|| (BatchSchemaConstants.ELEM_STEP.equals(elem) && BatchSchemaConstants.ATTR_PARENT.equals(attr))
				|| (BatchSchemaConstants.ELEM_CHUNK.equals(elem) && (BatchSchemaConstants.ATTR_PROCESSOR.equals(attr)
						|| BatchSchemaConstants.ATTR_READER.equals(attr) || BatchSchemaConstants.ATTR_WRITER
						.equals(attr)))) {
			TextAttribute attrControl = createBeanAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanReferenceContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		if (BatchSchemaConstants.ATTR_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, true, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassContentProposalProvider(getInput(), attr)));
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}
	
	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Batch step. Clicking the hyperlink will open
	 * the configuration file containing the step definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createStepAttribute(Composite client, String attr, boolean required) {
		// TODO: make this return a HyperlinkedComboAttribute?
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new StepReferenceHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

}
