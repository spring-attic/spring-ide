/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * {@link Action} implementation that wraps {@link OpenConfigFileAction} and
 * {@link OpenJavaElementAction} and gets installed as global double click
 * action.
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenActionWrapperAction extends AbstractNavigatorAction {

	private OpenConfigFileAction openConfigFileAction;

	private OpenJavaElementAction openJavaElementAction;

	private Action action;

	public OpenActionWrapperAction(ICommonActionExtensionSite site,
			OpenConfigFileAction openConfigFileAction,
			OpenJavaElementAction openJavaElementAction) {
		super(site);
		this.openConfigFileAction = openConfigFileAction;
		this.openJavaElementAction = openJavaElementAction;
		setText("Op&en"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			boolean javaActionCanRun = openJavaElementAction.isEnabled(selection); 
			boolean configActionCanRun = openConfigFileAction.isEnabled(selection);
			
			if (BeansUIUtils.shouldOpenConfigFile()) {
				if (configActionCanRun) {
					this.action = openConfigFileAction;
					return true;
				}
				else if (javaActionCanRun) {
					this.action = openJavaElementAction;
					return true;
				}
				else {
					return false;
				}
			}
			else {
				if (javaActionCanRun) {
					this.action = openJavaElementAction;
					return true;
				}
				else if (configActionCanRun) {
					this.action = openConfigFileAction;
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		this.action.run();
	}
	
}
