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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph.parts;

import org.eclipse.gef.EditPart;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.LabelledTransition;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.ActionStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.DecisionStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.EndStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.SubflowStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.ViewStateModelElement;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.WebFlowDiagram;


/**
 * @author Leo Dos Santos
 */
public class WebFlowEditPartFactory extends AbstractConfigEditPartFactory {

	public WebFlowEditPartFactory(AbstractConfigGraphicalEditor editor) {
		super(editor);
	}

	@Override
	protected EditPart createEditPartFromModel(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof WebFlowDiagram) {
			part = new WebFlowDiagramEditPart((WebFlowDiagram) model);
		}
		else if (model instanceof ActionStateModelElement) {
			part = new ActionStateGraphicalEditPart((ActionStateModelElement) model);
		}
		else if (model instanceof DecisionStateModelElement) {
			part = new DecisionStateGraphicalEditPart((DecisionStateModelElement) model);
		}
		else if (model instanceof EndStateModelElement) {
			part = new EndStateGraphicalEditPart((EndStateModelElement) model);
		}
		else if (model instanceof SubflowStateModelElement) {
			part = new SubflowStateGraphicalEditPart((SubflowStateModelElement) model);
		}
		else if (model instanceof ViewStateModelElement) {
			part = new ViewStateGraphicalEditPart((ViewStateModelElement) model);
		}
		else if (model instanceof LabelledTransition) {
			part = new WebFlowTransitionPart((LabelledTransition) model);
		}
		return part;
	}

}
