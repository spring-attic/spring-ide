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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import org.eclipse.osgi.util.NLS;

/**
 * @author Terry Denney
 */
public class NewRooWizardMessages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.roo.ui.internal.wizard.messages"; //$NON-NLS-1$

	public static String NewRooProjectWizardPageOne_Install;

	public static String NewRooProjectWizardPageOne_Message_notOnWorkspaceRoot;

	public static String NewRooProjectWizardPageOne_noRooInstallationConfigured;

	public static String NewRooProjectWizardPageOne_notExisingProjectOnWorkspaceRoot;

	public static String NewRooProjectWizardPageOne_useDefaultRooInstallation;

	public static String NewRooProjectWizardPageOne_useDefaultRooInstallationNoCurrent;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, NewRooWizardMessages.class);
	}

	private NewRooWizardMessages() {
	}
}
