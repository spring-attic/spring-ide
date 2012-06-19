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

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.ParallelActivity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class AbstractStateModelElement extends Activity {

	public AbstractStateModelElement() {
		super();
	}

	public AbstractStateModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = super.getOutgoingTransitionsFromXml();
		List<Activity> registry = getDiagram().getModelRegistry();
		NodeList transitions = getInput().getChildNodes();
		for (int i = 0; i < transitions.getLength(); i++) {
			Node node = transitions.item(i);
			if (node instanceof IDOMElement && node.getLocalName().equals(WebFlowSchemaConstants.ELEM_TRANSITION)) {
				IDOMElement transition = (IDOMElement) node;
				String state = transition.getAttribute(WebFlowSchemaConstants.ATTR_TO);
				if (state != null && state.trim().length() > 0) {
					Node stateRef = getDiagram().getReferencedNode(state);
					if (stateRef instanceof IDOMElement) {
						for (Activity activity : registry) {
							if (!(activity instanceof ParallelActivity) && activity.getInput().equals(stateRef)) {
								Transition trans = new WebFlowTransition(this, activity, transition);
								list.add(trans);
							}
						}
					}
				}
				else {
					Transition trans = new WebFlowTransition(this, this, transition);
					list.add(trans);
				}
			}
		}
		return list;
	}

}
