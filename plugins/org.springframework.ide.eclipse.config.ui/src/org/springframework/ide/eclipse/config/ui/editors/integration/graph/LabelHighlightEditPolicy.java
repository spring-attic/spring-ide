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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BorderedIntegrationPart;


/**
 * @author Leo Dos Santos
 */
public class LabelHighlightEditPolicy extends GraphicalEditPolicy {

	@Override
	public void eraseTargetFeedback(Request request) {
		EditPart part = getHost();
		if (part instanceof BorderedIntegrationPart) {
			BorderedIntegrationPart activityPart = (BorderedIntegrationPart) part;
			Activity activity = activityPart.getModelElement();
			activity.setDisplayLabel(activity.getShortName());
			activityPart.refresh();
		}
	}

	@Override
	public void showTargetFeedback(Request request) {
		EditPart part = getHost();
		if (part instanceof BorderedIntegrationPart) {
			BorderedIntegrationPart activityPart = (BorderedIntegrationPart) part;
			Activity activity = activityPart.getModelElement();
			activity.setDisplayLabel(activity.getInputName());
			activityPart.refresh();
		}
	}

}
