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

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.WebFlowDiagram;


/**
 * @author Leo Dos Santos
 */
public class WebFlowDiagramEditPart extends ActivityDiagramPart {

	private final IPreferenceStore uiPrefStore;

	public WebFlowDiagramEditPart(WebFlowDiagram diagram) {
		super(diagram, PositionConstants.EAST);
		uiPrefStore = ConfigUiPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public void activate() {
		super.activate();
		uiPrefStore.addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		uiPrefStore.removePropertyChangeListener(this);
		super.deactivate();
	}

	@Override
	public WebFlowDiagram getModelElement() {
		return (WebFlowDiagram) getModel();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (SpringConfigPreferenceConstants.PREF_DISPLAY_ACTION_STATE.equals(event.getProperty())
				|| SpringConfigPreferenceConstants.PREF_DISPLAY_DECISION_STATE.equals(event.getProperty())
				|| SpringConfigPreferenceConstants.PREF_DISPLAY_END_STATE.equals(event.getProperty())
				|| SpringConfigPreferenceConstants.PREF_DISPLAY_SUBFLOW_STATE.equals(event.getProperty())
				|| SpringConfigPreferenceConstants.PREF_DISPLAY_VIEW_STATE.equals(event.getProperty())) {
			refreshAll();
		}
	}

}
