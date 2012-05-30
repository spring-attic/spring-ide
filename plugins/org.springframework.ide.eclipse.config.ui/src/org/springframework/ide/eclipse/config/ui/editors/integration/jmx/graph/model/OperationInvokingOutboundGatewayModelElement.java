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
package org.springframework.ide.eclipse.config.ui.editors.integration.jmx.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntJmxSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractOutboundGatewayModelElement;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class OperationInvokingOutboundGatewayModelElement extends AbstractOutboundGatewayModelElement {

	public OperationInvokingOutboundGatewayModelElement() {
		super();
	}

	public OperationInvokingOutboundGatewayModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntJmxSchemaConstants.ELEM_OPERATION_INVOKING_OUTBOUND_GATEWAY;
	}

}
