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
import org.springframework.ide.eclipse.config.graph.model.IModelFactory;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class AbstractIntegrationModelFactory implements IModelFactory {

	public void getNestedChildrenFromXml(List<Activity> list, IDOMElement input, AbstractConfigGraphDiagram diagram) {
		String channelRef = input.getAttribute(IntegrationSchemaConstants.ATTR_INPUT_CHANNEL);
		if (channelRef == null || channelRef.trim().length() == 0) {
			channelRef = input.getAttribute(IntegrationSchemaConstants.ATTR_REQUEST_CHANNEL);
		}
		if (channelRef != null && channelRef.trim().length() > 0) {
			Node channelNode = diagram.getReferencedNode(channelRef);
			if (channelNode == null) {
				ImplicitChannelModelElement channel = new ImplicitChannelModelElement(input, diagram);
				if (!diagram.listContainsElement(list, channel)) {
					list.add(channel);
				}
			}
		}

		channelRef = input.getAttribute(IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL);
		if (channelRef != null
				&& (channelRef.equalsIgnoreCase("errorChannel") || channelRef.equalsIgnoreCase("nullChannel"))) { //$NON-NLS-1$ //$NON-NLS-2$
			Node channelNode = diagram.getReferencedNode(channelRef);
			if (channelNode == null) {
				ImplicitOutputChannelModelElement channel = new ImplicitOutputChannelModelElement(input, diagram);
				if (!diagram.listContainsElement(list, channel)) {
					list.add(channel);
				}
			}
		}
	}

}
