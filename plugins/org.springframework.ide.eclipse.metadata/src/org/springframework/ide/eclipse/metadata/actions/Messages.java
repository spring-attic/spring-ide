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
package org.springframework.ide.eclipse.metadata.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.metadata.actions.messages"; //$NON-NLS-1$
	public static String OpenInBrowserAction_TITLE;
	public static String OpenInJavaEditorAction_TITLE;
	public static String ShowRequestMappingsAction_ERROR_OPENING_VIEW;
	public static String ShowRequestMappingsAction_TITLE;
	public static String ToggleBreakPointAction_TITLE;
	public static String ToggleLinkingAction_DESCRIPTION;
	public static String ToggleLinkingAction_LABEL;
	public static String ToggleLinkingAction_TOOLTIP;
	public static String ToggleOrientationAction_DESCRIPTION_HORIZONTAL;
	public static String ToggleOrientationAction_DESCRIPTION_VERTICAL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
