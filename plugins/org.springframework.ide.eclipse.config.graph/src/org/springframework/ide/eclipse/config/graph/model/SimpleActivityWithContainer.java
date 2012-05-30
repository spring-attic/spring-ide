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

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class SimpleActivityWithContainer extends StructuredActivity {

	public SimpleActivityWithContainer() {
		super();
	}

	public SimpleActivityWithContainer(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	protected List<Activity> getChildrenFromXml() {
		return new ArrayList<Activity>();
	}

	@Override
	protected List<Transition> getOutgoingTransitionsFromXml() {
		List<Transition> list = super.getOutgoingTransitionsFromXml();
		List<Activity> registry = getDiagram().getModelRegistry();
		for (Activity activity : registry) {
			if (activity instanceof ParallelActivity && activity.getInput().equals(getInput())) {
				Transition trans = new Transition(this, activity, getInput());
				trans.setLineStyle(Transition.DASHED_CONNECTION);
				list.add(trans);
			}
		}
		return list;
	}

}
