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
package org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntFileSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntXquerySchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;

@SuppressWarnings("restriction")
public class XqueryTransformerModelElement extends Activity {

	public XqueryTransformerModelElement() {
		super();
	}

	public XqueryTransformerModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntXquerySchemaConstants.ELEM_XQUERY_TRANSFORMER;
	}

	@Override
	public List<String> getPrimaryIncomingAttributes() {
		return Arrays.asList(IntFileSchemaConstants.ATTR_INPUT_CHANNEL);
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(IntFileSchemaConstants.ATTR_OUTPUT_CHANNEL);
	}

}
