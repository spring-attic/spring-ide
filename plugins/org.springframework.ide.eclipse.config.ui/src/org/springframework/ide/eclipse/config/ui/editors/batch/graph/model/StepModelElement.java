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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.SimpleActivityWithContainer;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class StepModelElement extends SimpleActivityWithContainer {

	public StepModelElement() {
		super();
	}

	public StepModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected void createInput(String uri) {
		super.createInput(uri);
		getInput().setAttribute(BatchSchemaConstants.ATTR_ID, getNewStepId());
	}

	@Override
	public String getInputName() {
		return BatchSchemaConstants.ELEM_STEP;
	}

	private String getNewStepId() {
		String id = getInputName() + ((BatchDiagram) getDiagram()).getNewStepId();
		Node ref = getDiagram().getReferencedNode(id);
		if (ref instanceof IDOMElement) {
			// We have a duplicate. Continue to increment.
			return getNewStepId();
		}
		return id;
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(BatchSchemaConstants.ATTR_NEXT);
	}

}
