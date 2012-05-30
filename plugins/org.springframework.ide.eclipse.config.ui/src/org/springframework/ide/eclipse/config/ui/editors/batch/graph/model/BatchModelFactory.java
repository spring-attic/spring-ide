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

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.IDiagramModelFactory;
import org.springframework.ide.eclipse.config.graph.model.SequentialActivity;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class BatchModelFactory implements IDiagramModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(BatchSchemaConstants.ELEM_DECISION)) {
			DecisionModelElement decision = new DecisionModelElement(input, parent.getDiagram());
			list.add(decision);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_END)) {
			EndModelElement end = new EndModelElement(input, parent.getDiagram());
			list.add(end);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_FAIL)) {
			FailModelElement fail = new FailModelElement(input, parent.getDiagram());
			list.add(fail);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_FLOW)) {
			if (parent instanceof SequentialActivity) {
				FlowAnchorElement flow = new FlowAnchorElement(input, parent.getDiagram());
				list.add(flow);
			}
			else {
				FlowModelElement flow = new FlowModelElement(input, parent.getDiagram());
				list.add(flow);
			}
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_JOB)) {
			JobModelElement job = new JobModelElement(input, parent.getDiagram());
			list.add(job);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_NEXT)) {
			NextModelElement next = new NextModelElement(input, parent.getDiagram());
			list.add(next);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_SPLIT)) {
			SplitModelElement split = new SplitModelElement(input, parent.getDiagram());
			list.add(split);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_STEP)) {
			StepModelElement step = new StepModelElement(input, parent.getDiagram());
			list.add(step);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_STOP)) {
			StopModelElement stop = new StopModelElement(input, parent.getDiagram());
			list.add(stop);
		}
	}

	public void getGenericChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		// TODO Auto-generated method stub

	}

	public void getNestedChildrenFromXml(List<Activity> list, IDOMElement input, AbstractConfigGraphDiagram diagram) {
		IDOMElement parent = (IDOMElement) input.getParentNode();
		if (parent.getLocalName().equals(BatchSchemaConstants.ELEM_STEP)) {
			StepContainerElement step = new StepContainerElement(parent, diagram);
			if (!diagram.listContainsElement(list, step)) {
				if (input.getLocalName().equals(BatchSchemaConstants.ELEM_END)
						|| input.getLocalName().equals(BatchSchemaConstants.ELEM_FAIL)
						|| input.getLocalName().equals(BatchSchemaConstants.ELEM_FLOW)
						|| input.getLocalName().equals(BatchSchemaConstants.ELEM_JOB)
						|| input.getLocalName().equals(BatchSchemaConstants.ELEM_NEXT)
						|| input.getLocalName().equals(BatchSchemaConstants.ELEM_STOP)) {
					list.add(step);
				}
			}
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_FLOW)) {
			if (parent.getLocalName().equals(BatchSchemaConstants.ELEM_FLOW)
					|| parent.getLocalName().equals(BatchSchemaConstants.ELEM_JOB)) {
				FlowContainerElement flow = new FlowContainerElement(input, diagram);
				list.add(flow);
			}
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_DECISION)) {
			DecisionContainerElement decision = new DecisionContainerElement(input, diagram);
			list.add(decision);
		}
		else if (input.getLocalName().equals(BatchSchemaConstants.ELEM_SPLIT)) {
			SplitContainerElement split = new SplitContainerElement(input, diagram);
			list.add(split);
		}
	}

}
