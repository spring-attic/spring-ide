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
package org.springframework.ide.eclipse.config.ui.editors.webflow.config;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.providers.BeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassHierarchyContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowConfigSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class WebFlowConfigDetailsSectionPart extends SpringConfigDetailsSectionPart {

	public WebFlowConfigDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// WebflowConfigContentAssistProcessor and
		// WebflowConfigHyperLinkDetector
		String elem = getInput().getLocalName();
		if ((WebFlowConfigSchemaConstants.ELEM_EXECUTOR.equals(elem) && WebFlowConfigSchemaConstants.ATTR_REGISTRY_REF
				.equals(attr))
				|| (WebFlowConfigSchemaConstants.ELEM_REPOSITORY.equals(elem) && WebFlowConfigSchemaConstants.ATTR_CONVERSATION_MANAGER_REF
						.equals(attr))
				|| (WebFlowConfigSchemaConstants.ELEM_LISTENER.equals(elem) && WebFlowConfigSchemaConstants.ATTR_REF
						.equals(attr))
				|| (WebFlowConfigSchemaConstants.ELEM_FLOW_EXECUTOR.equals(elem) && WebFlowConfigSchemaConstants.ATTR_FLOW_REGISTRY
						.equals(attr))
				|| (WebFlowConfigSchemaConstants.ELEM_FLOW_REGISTRY.equals(elem) && (WebFlowConfigSchemaConstants.ATTR_PARENT
						.equals(attr) || WebFlowConfigSchemaConstants.ATTR_FLOW_BUILDER_SERVICES.equals(attr)))
				|| (WebFlowConfigSchemaConstants.ELEM_FLOW_BUILDER_SERVICES.equals(elem) && (WebFlowConfigSchemaConstants.ATTR_VIEW_FACTORY_CREATOR
						.equals(attr) || WebFlowConfigSchemaConstants.ATTR_EXPRESSION_PARSER.equals(attr) || WebFlowConfigSchemaConstants.ATTR_CONVERSION_SERVICE
						.equals(attr)))) {
			TextAttribute attrControl = createBeanAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanReferenceContentProposalProvider(
					getInput(), attr, true)));
			return true;
		}
		if (WebFlowConfigSchemaConstants.ELEM_ATTRIBUTE.equals(elem)
				&& WebFlowConfigSchemaConstants.ATTR_TYPE.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassContentProposalProvider(getInput(), attr,
					false)));
			return true;
		}
		if (WebFlowConfigSchemaConstants.ELEM_FLOW_BUILDER.equals(elem)
				&& WebFlowConfigSchemaConstants.ATTR_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, "org.springframework.webflow.engine.builder.FlowBuilder"))); //$NON-NLS-1$
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}

}
