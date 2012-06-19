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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.ui.editors.webflow.graph.messages"; //$NON-NLS-1$

	public static String ToggleActionStateAction_ACTION_LABEL;

	public static String ToggleDecisionStateAction_ACTION_LABEL;

	public static String ToggleEndStateAction_ACTION_LABEL;

	public static String ToggleSubflowStateAction_ACTION_LABEL;

	public static String ToggleTransitionStateAction_ACTION_LABEL;

	public static String ToggleViewStateAction_ACTION_LABEL;

	public static String WebFlowEditorPaletteFactory_ACTION_STATE_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_DECISION_STATE_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_END_STATE_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_IF_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_STATE_PALETTE_TITLE;

	public static String WebFlowEditorPaletteFactory_SUBFLOW_STATE_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_TO_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_TO_COMPONENT_TITLE;

	public static String WebFlowEditorPaletteFactory_TRANSITION_COMPONENT_DESCRIPTION;

	public static String WebFlowEditorPaletteFactory_VIEW_STATE_COMPONENT_DESCRIPTION;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
