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
public abstract class AbstractChannelModelElement extends Activity {

	public AbstractChannelModelElement() {
		super();
	}

	public AbstractChannelModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected void createInput(String uri) {
		super.createInput(uri);
		getInput().setAttribute(IntegrationSchemaConstants.ATTR_ID, getNewChannelId());
	}

	@Override
	public String getInputName() {
		return IntegrationSchemaConstants.ELEM_CHANNEL;
	}

	private String getNewChannelId() {
		String id = getInputName() + ((IntegrationDiagram) getDiagram()).getNewChannelId();
		Node ref = getDiagram().getReferencedNode(id);
		if (ref instanceof IDOMElement) {
			// We have a duplicate. Continue to increment.
			return getNewChannelId();
		}
		return id;
	}

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = super.getOutgoingTransitionsFromXml();
		List<Activity> registry = getDiagram().getModelRegistry();
		NodeList interceptors = getInput().getChildNodes();
		for (int i = 0; i < interceptors.getLength(); i++) {
			Node iNode = interceptors.item(i);
			if (iNode instanceof IDOMElement
					&& iNode.getLocalName().equals(IntegrationSchemaConstants.ELEM_INTERCEPTORS)) {
				IDOMElement interceptor = (IDOMElement) iNode;
				NodeList wiretaps = interceptor.getChildNodes();
				for (int w = 0; w < wiretaps.getLength(); w++) {
					Node wNode = wiretaps.item(w);
					if (wNode instanceof IDOMElement
							&& wNode.getLocalName().equals(IntegrationSchemaConstants.ELEM_WIRE_TAP)) {
						IDOMElement wiretap = (IDOMElement) wNode;
						String channel = wiretap.getAttribute(IntegrationSchemaConstants.ATTR_CHANNEL);
						if (channel != null && channel.trim().length() > 0) {
							Node channelRef = getDiagram().getReferencedNode(channel);
							if (channelRef != null) {
								for (Activity activity : registry) {
									if (!(activity instanceof ParallelActivity)
											&& activity.getInput().equals(channelRef)) {
										Transition trans = new AlternateTransition(this, activity, wiretap);
										list.add(trans);
									}
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

}
