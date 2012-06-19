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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.LabelledTransition;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IfThenTransition extends LabelledTransition {

	public IfThenTransition(Activity source, Activity target, IDOMElement input) {
		super(source, target, input);
	}

	@Override
	public String getLabel() {
		String label = WebFlowSchemaConstants.ELEM_IF;
		String test = getInput().getAttribute(WebFlowSchemaConstants.ATTR_TEST);
		if (test != null && test.trim().length() > 0) {
			label = label + " " + test;
		}
		return (label + " true");
	}

}
