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


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class AbstractInboundChannelAdapterModelElement extends AbstractChannelAdapterModelElement {

	public AbstractInboundChannelAdapterModelElement() {
		super();
	}

	public AbstractInboundChannelAdapterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntegrationSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER;
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(IntegrationSchemaConstants.ATTR_CHANNEL);
	}

}
