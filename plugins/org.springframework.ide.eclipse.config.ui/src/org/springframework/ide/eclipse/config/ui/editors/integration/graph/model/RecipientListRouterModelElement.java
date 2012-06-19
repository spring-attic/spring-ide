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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.ParallelActivity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class RecipientListRouterModelElement extends Activity {

	public RecipientListRouterModelElement() {
		super();
	}

	public RecipientListRouterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntegrationSchemaConstants.ELEM_RECIPIENT_LIST_ROUTER;
	}

	@Override
	public List<String> getPrimaryIncomingAttributes() {
		return Arrays.asList(IntegrationSchemaConstants.ATTR_INPUT_CHANNEL);
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL);
	}

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = super.getOutgoingTransitionsFromXml();
		List<Activity> registry = getDiagram().getModelRegistry();
		NodeList mappings = getInput().getChildNodes();
		for (int i = 0; i < mappings.getLength(); i++) {
			Node node = mappings.item(i);
			if (node instanceof IDOMElement && node.getLocalName().equals(IntegrationSchemaConstants.ELEM_RECIPIENT)) {
				IDOMElement recipient = (IDOMElement) node;
				String channel = recipient.getAttribute(IntegrationSchemaConstants.ATTR_CHANNEL);
				if (channel != null && channel.trim().length() > 0) {
					Node channelRef = getDiagram().getReferencedNode(channel);
					if (channelRef instanceof IDOMElement) {
						for (Activity activity : registry) {
							if (!(activity instanceof ParallelActivity) && activity.getInput().equals(channelRef)) {
								Transition trans = new AlternateTransition(this, activity, recipient);
								list.add(trans);
							}
						}
					}
				}
			}
		}
		return list;
	}

}
