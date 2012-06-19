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
package org.springframework.ide.eclipse.config.ui.editors.batch.graph.parts;

import org.eclipse.gef.EditPart;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.TransitionPart;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.BatchDiagram;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.DecisionContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.DecisionModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.EndModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FailModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FlowAnchorElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FlowContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.FlowModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.JobModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.NextModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.SplitContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.SplitModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.StepContainerElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.StepModelElement;
import org.springframework.ide.eclipse.config.ui.editors.batch.graph.model.StopModelElement;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BatchEditPartFactory extends AbstractConfigEditPartFactory {

	public BatchEditPartFactory(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected EditPart createEditPartFromModel(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof BatchDiagram) {
			part = new BatchDiagramEditPart((BatchDiagram) model);
		}
		else if (model instanceof DecisionContainerElement) {
			part = new DecisionContainerEditPart((DecisionContainerElement) model);
		}
		else if (model instanceof DecisionModelElement) {
			part = new DecisionGraphicalEditPart((DecisionModelElement) model);
		}
		else if (model instanceof EndModelElement) {
			part = new EndGraphicalEditPart((EndModelElement) model);
		}
		else if (model instanceof FailModelElement) {
			part = new FailGraphicalEditPart((FailModelElement) model);
		}
		else if (model instanceof FlowAnchorElement) {
			part = new FlowAnchorEditPart((FlowAnchorElement) model);
		}
		else if (model instanceof FlowContainerElement) {
			part = new FlowContainerEditPart((FlowContainerElement) model);
		}
		else if (model instanceof FlowModelElement) {
			part = new FlowGraphicalEditPart((FlowModelElement) model);
		}
		else if (model instanceof JobModelElement) {
			part = new JobGraphicalEditPart((JobModelElement) model);
		}
		else if (model instanceof NextModelElement) {
			part = new NextGraphicalEditPart((NextModelElement) model);
		}
		else if (model instanceof SplitContainerElement) {
			part = new SplitContainerEditPart((SplitContainerElement) model);
		}
		else if (model instanceof SplitModelElement) {
			part = new SplitGraphicalEditPart((SplitModelElement) model);
		}
		else if (model instanceof StepContainerElement) {
			part = new StepContainerEditPart((StepContainerElement) model);
		}
		else if (model instanceof StepModelElement) {
			part = new StepGraphicalEditPart((StepModelElement) model);
		}
		else if (model instanceof StopModelElement) {
			part = new StopGraphicalEditPart((StopModelElement) model);
		}
		else if (model instanceof Transition) {
			part = new TransitionPart((Transition) model);
		}
		return part;
	}

}
