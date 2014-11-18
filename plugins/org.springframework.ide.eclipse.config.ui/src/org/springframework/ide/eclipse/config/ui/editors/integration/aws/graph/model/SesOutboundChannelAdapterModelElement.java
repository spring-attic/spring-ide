/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.aws.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntAwsSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractOutboundChannelAdapterModelElement;

@SuppressWarnings("restriction")
public class SesOutboundChannelAdapterModelElement extends AbstractOutboundChannelAdapterModelElement {

	public SesOutboundChannelAdapterModelElement() {
		super();
	}

	public SesOutboundChannelAdapterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntAwsSchemaConstants.ELEM_SES_OUTBOUND_CHANNEL_ADAPTER;
	}

}
