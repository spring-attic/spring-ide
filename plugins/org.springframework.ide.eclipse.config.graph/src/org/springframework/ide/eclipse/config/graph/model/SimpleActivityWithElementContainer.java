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
package org.springframework.ide.eclipse.config.graph.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class SimpleActivityWithElementContainer extends StructuredActivity {

	public SimpleActivityWithElementContainer() {
		super();
	}

	public SimpleActivityWithElementContainer(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected List<Activity> getChildrenFromXml() {
		return new ArrayList<Activity>();
	}

	protected abstract String getContainerInputName();

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = super.getOutgoingTransitionsFromXml();
		List<Activity> registry = getDiagram().getModelRegistry();
		NodeList children = getInput().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node container = children.item(i);
			if (container instanceof IDOMElement && container.getLocalName().equals(getContainerInputName())) {
				for (Activity activity : registry) {
					if (activity instanceof ParallelActivity && activity.getInput().equals(container)) {
						Transition trans = new Transition(this, activity, (IDOMElement) container);
						trans.setLineStyle(Transition.DASHED_CONNECTION);
						list.add(trans);
					}
				}
			}
		}
		return list;
	}
}
