/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;

/**
 * @author Martin Lippert
 */
public class ExposeAppAction extends AbstractBootDashElementsAction {

	private BootDashViewModel model;
	private ElementStateListener listener;
	private Process process;

	public ExposeAppAction(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.model = model;
		setText("Expose...");

		if (model!=null) {
			model.addElementStateListener(listener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							updateEnablement();
						}
					});
				}
			});
		}
	}

	@Override
	public void updateEnablement() {
		Collection<BootDashElement> selectedElements = getSelectedElements();
		boolean localAppsSelected = true;

		for (Iterator<BootDashElement> iterator = selectedElements.iterator(); iterator.hasNext();) {
			BootDashElement bootDashElement = iterator.next();

			if (!bootDashElement.getTarget().getType().equals(RunTargetTypes.LOCAL)) {
				localAppsSelected = false;
			}
		}

		setEnabled(localAppsSelected & selectedElements.size() > 0);
	}

	@Override
	public void run() {
		ProcessBuilder processBuilder = new ProcessBuilder("/Users/mlippert/Desktop/ngrok", "start", "--none");

		try {
			process = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Expose...");
	}

	@Override
	public void dispose() {
		if (listener==null) {
			model.removeElementStateListener(listener);
			listener = null;
		}

		if (process != null) {
			process.destroy();
		}
		super.dispose();
	}

}
