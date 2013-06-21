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
package org.springframework.ide.eclipse.webflow.ui.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassHierarchyContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.hyperlinks.XmlBackedHyperlinkProvider;
import org.springframework.ide.eclipse.config.ui.widgets.HyperlinkedTextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;
import org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow.BeanActionMethodContentProposalProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow.BeanMethodContentProposalProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow.StateReferenceContentProposalProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow.SubflowReferenceContentProposalProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow.WebFlowBeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow.StateReferenceHyperlinkProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow.SubflowReferenceHyperlinkProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow.WebFlowActionMethodHyperlinkProvider;
import org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow.WebFlowBeanReferenceHyperlinkProvider;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class WebFlowDetailsSectionPart extends SpringConfigDetailsSectionPart {

	private FormToolkit toolkit;
	
	public WebFlowDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
		this.toolkit = toolkit;
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// WebflowContentAssistProcessor and WebflowHyperlinkDetector
		String elem = getInput().getLocalName();
		if ((WebFlowSchemaConstants.ELEM_ARGUMENT.equals(elem) && WebFlowSchemaConstants.ATTR_PARAMETER_TYPE
				.equals(attr))
				|| (WebFlowSchemaConstants.ELEM_MAPPING.equals(elem) && (WebFlowSchemaConstants.ATTR_TO.equals(attr) || WebFlowSchemaConstants.ATTR_FROM
						.equals(attr)))
				|| (WebFlowSchemaConstants.ELEM_EVALUATE.equals(elem) && WebFlowSchemaConstants.ATTR_RESULT_TYPE
						.equals(attr))
				|| WebFlowSchemaConstants.ATTR_CLASS.equals(attr)
				|| WebFlowSchemaConstants.ATTR_TYPE.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassContentProposalProvider(getInput(), attr,
					false)));
			return true;
		}
		if ((WebFlowSchemaConstants.ELEM_VAR.equals(elem) && WebFlowSchemaConstants.ATTR_NAME.equals(attr))
				|| (WebFlowSchemaConstants.ELEM_SUBFLOW_STATE.equals(elem) && WebFlowSchemaConstants.ATTR_SUBFLOW_ATTRIBUTE_MAPPER
						.equals(attr)) || WebFlowSchemaConstants.ATTR_BEAN.equals(attr)) {
			TextAttribute attrControl = createWebFlowBeanAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new WebFlowBeanReferenceContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		if ((WebFlowSchemaConstants.ELEM_TRANSITION.equals(elem) && WebFlowSchemaConstants.ATTR_TO.equals(attr))
				|| (WebFlowSchemaConstants.ELEM_START_STATE.equals(elem) && WebFlowSchemaConstants.ATTR_IDREF
						.equals(attr))
				|| (WebFlowSchemaConstants.ELEM_IF.equals(elem) && (WebFlowSchemaConstants.ATTR_THEN.equals(attr) || WebFlowSchemaConstants.ATTR_ELSE
						.equals(attr)))) {
			TextAttribute attrControl = createStateAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new StateReferenceContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		if (WebFlowSchemaConstants.ELEM_TRANSITION.equals(elem)
				&& WebFlowSchemaConstants.ATTR_ON_EXCEPTION.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, Throwable.class.getName())));
			return true;
		}
		if ((WebFlowSchemaConstants.ELEM_SUBFLOW_STATE.equals(elem) && (WebFlowSchemaConstants.ATTR_FLOW.equals(attr) || WebFlowSchemaConstants.ATTR_SUBFLOW
				.equals(attr)))
				|| (WebFlowSchemaConstants.ELEM_FLOW.equals(elem) && WebFlowSchemaConstants.ATTR_PARENT.equals(attr))) {
			TextAttribute attrControl = createSubflowAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new SubflowReferenceContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		if (WebFlowSchemaConstants.ELEM_ACTION.equals(elem) && WebFlowSchemaConstants.ATTR_METHOD.equals(attr)) {
			TextAttribute attrControl = createWebFlowMethodAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanMethodContentProposalProvider(getInput(),
					attr)));
			return true;
		}
		if (WebFlowSchemaConstants.ELEM_BEAN_ACTION.equals(elem) && WebFlowSchemaConstants.ATTR_METHOD.equals(attr)) {
			TextAttribute attrControl = createWebFlowMethodAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanActionMethodContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}
	
	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Web Flow state. Clicking the hyperlink will
	 * open the configuration file containing the state definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createStateAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new StateReferenceHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}
	
	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Web Flow subflow. Clicking the hyperlink will
	 * open the configuration file containing the subflow definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createSubflowAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new SubflowReferenceHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}
	
	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to another Web Flow bean. Clicking the hyperlink
	 * will open the configuration file containing the bean definition.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createWebFlowBeanAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new WebFlowBeanReferenceHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

	/**
	 * Creates a {@link HyperlinkedTextAttribute} widget set for displaying an
	 * attribute that refers to a Java method. Clicking the hyperlink will open
	 * the class file at the method displayed in the text field.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return {@link HyperlinkedTextAttribute} widget set
	 */
	protected HyperlinkedTextAttribute createWebFlowMethodAttribute(Composite client, String attr, boolean required) {
		HyperlinkedTextAttribute linkAttr = new XmlBackedHyperlinkTextAttribute(client, toolkit, attr, required) {
			public void openHyperlink() {
				XmlBackedHyperlinkProvider provider = new WebFlowActionMethodHyperlinkProvider(getConfigEditor()
						.getTextViewer(), getInput(), attr);
				provider.open(text.getText());
			}
		};
		linkAttr.createAttribute(2);
		return linkAttr;
	}

}
