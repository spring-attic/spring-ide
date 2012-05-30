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
package org.springframework.ide.eclipse.config.ui.editors.integration.ws.graph.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntWsSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class HeaderEnricherModelElement extends Activity {

	public HeaderEnricherModelElement() {
		super();
	}

	public HeaderEnricherModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntWsSchemaConstants.ELEM_HEADER_ENRICHER;
	}

	@Override
	public List<String> getPrimaryIncomingAttributes() {
		return Arrays.asList(IntWsSchemaConstants.ATTR_INPUT_CHANNEL);
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(IntWsSchemaConstants.ATTR_OUTPUT_CHANNEL);
	}

}
