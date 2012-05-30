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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ImplicitOutputChannelModelElement extends ImplicitChannelModelElement {

	public ImplicitOutputChannelModelElement() {
		super();
	}

	public ImplicitOutputChannelModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected List<Transition> getIncomingTransitionsFromXml() {
		List<Transition> list = new ArrayList<Transition>();
		List<Activity> registry = getDiagram().getModelRegistry();
		for (Activity activity : registry) {
			List<String> labels = new ArrayList<String>();
			labels.addAll(activity.getPrimaryOutgoingAttributes());
			labels.addAll(activity.getSecondaryOutgoingAttributes());
			for (String attr : labels) {
				String ref = activity.getInput().getAttribute(attr);
				String id = getInput().getAttribute(IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL);
				if (ref != null && id != null && ref.equals(id)) {
					Transition trans = new ImplicitTransition(activity, this, (IDOMAttr) activity.getInput()
							.getAttributeNode(attr));
					list.add(trans);
				}
			}
		}
		return list;
	}

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = new ArrayList<Transition>();
		List<Activity> registry = getDiagram().getModelRegistry();
		for (Activity activity : registry) {
			List<String> labels = new ArrayList<String>();
			labels.addAll(activity.getPrimaryIncomingAttributes());
			labels.addAll(activity.getSecondaryIncomingAttributes());
			for (String attr : labels) {
				String ref = activity.getInput().getAttribute(attr);
				String id = getInput().getAttribute(IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL);
				if (ref != null && id != null && ref.equals(id)) {
					Transition trans = new ImplicitTransition(this, activity, (IDOMAttr) activity.getInput()
							.getAttributeNode(attr));
					list.add(trans);
				}
			}
		}
		return list;
	}

	@Override
	protected void internalSetName() {
		String id = getInput().getAttribute(IntegrationSchemaConstants.ATTR_OUTPUT_CHANNEL);
		if (id != null && id.trim().length() > 0) {
			setName(id);
		}
		else {
			super.internalSetName();
		}
	}

}
