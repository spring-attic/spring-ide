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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ChainContainerElement extends ParallelActivity {

	public ChainContainerElement() {
		super();
	}

	public ChainContainerElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntegrationSchemaConstants.ELEM_CHAIN;
	}

	@Override
	protected void updateTransitionsFromXml() {
		super.updateTransitionsFromXml();
		if (getChildren().size() > 1) {
			IDOMElement input = getInput();
			if (input != null && input.hasChildNodes()) {
				IDOMElement prev = null;
				IDOMElement next = null;
				NodeList children = input.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if (child instanceof IDOMElement) {
						prev = next;
						next = (IDOMElement) child;
					}
					if (prev != null && next != null) {
						Activity prevActivity = null;
						Activity nextActivity = null;
						List<Activity> parts = getChildren();
						for (Activity activity : parts) {
							if (prev.equals(activity.getInput())) {
								prevActivity = activity;
							}
							if (next.equals(activity.getInput())) {
								nextActivity = activity;
							}
						}
						if (nextActivity != null && prevActivity != null) {
							new ImplicitTransition(prevActivity, nextActivity, null);
						}
					}
				}
			}
		}
	}

}
