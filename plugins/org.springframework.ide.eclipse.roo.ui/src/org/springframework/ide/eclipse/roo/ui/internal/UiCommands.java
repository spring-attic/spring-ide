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
package org.springframework.ide.eclipse.roo.ui.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;

/**
 * @author Christian Dupuis
 */
public class UiCommands {

	private final RooShellTab rooShellTab;

	public UiCommands(RooShellTab rooShellTab) {
		this.rooShellTab = rooShellTab;
	}

	public void exit() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				rooShellTab.shellView.removeTab(rooShellTab.project);
			}
		});
	}

	public void exitAndRestart(final int returnCode) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				IProject project = rooShellTab.project;
				rooShellTab.shellView.removeTab(project);
				rooShellTab.shellView.openShell(project);
			}
		});
	}

}