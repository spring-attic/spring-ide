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
package org.springframework.ide.eclipse.config.ui.editors.jms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.providers.BeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ListenerMethodContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.JmsSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class JmsDetailsSectionPart extends SpringConfigDetailsSectionPart {

	public JmsDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent, FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// JmsContentAssistProcessor and JmsHyperlinkDetector
		String elem = getInput().getLocalName();
		if ((JmsSchemaConstants.ELEM_LISTENER.equals(elem) && JmsSchemaConstants.ATTR_REF.equals(attr))
				|| (JmsSchemaConstants.ELEM_LISTENER_CONTAINER.equals(elem) && (JmsSchemaConstants.ATTR_CONNECTION_FACTORY
						.equals(attr)
						|| JmsSchemaConstants.ATTR_TASK_EXECUTOR.equals(attr)
						|| JmsSchemaConstants.ATTR_DESTINATION_RESOLVER.equals(attr)
						|| JmsSchemaConstants.ATTR_MESSAGE_CONVERTER.equals(attr) || JmsSchemaConstants.ATTR_TRANSACTION_MANAGER
						.equals(attr)))
				|| (JmsSchemaConstants.ELEM_JCA_LISTENER_CONTAINER.equals(elem) && (JmsSchemaConstants.ATTR_RESOURCE_ADAPTER
						.equals(attr) || JmsSchemaConstants.ATTR_ACTIVATION_SPEC_FACTORY.equals(attr) || JmsSchemaConstants.ATTR_MESSAGE_CONVERTER
						.equals(attr)))) {
			TextAttribute attrControl = createBeanAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanReferenceContentProposalProvider(
					getInput(), attr, true)));
			return true;
		}
		if (JmsSchemaConstants.ELEM_LISTENER.equals(elem) && JmsSchemaConstants.ATTR_METHOD.equals(attr)) {
			TextAttribute attrControl = createListenerMethodAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ListenerMethodContentProposalProvider(
					getInput(), attr)));
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}

}
