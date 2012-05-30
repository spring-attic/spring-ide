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
package org.springframework.ide.eclipse.config.ui.editors.integration.jms.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntJmsSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractInboundChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class MessageDrivenChannelAdapterModelElement extends AbstractInboundChannelAdapterModelElement {

	public MessageDrivenChannelAdapterModelElement() {
		super();
	}

	public MessageDrivenChannelAdapterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntJmsSchemaConstants.ELEM_MESSAGE_DRIVEN_CHANNEL_ADAPTER;
	}

}
