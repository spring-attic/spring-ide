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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class OpenConsoleAction extends AbstractBootDashElementsAction {

	private final BootDashViewModel viewModel;

	public OpenConsoleAction(MultiSelection<BootDashElement> selection, BootDashViewModel viewModel,
			UserInteractions ui) {
		super(selection, ui);
		this.setText("Open Console");
		this.setToolTipText("Open Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.gif"));
		this.viewModel = viewModel;
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selecteds = getSelectedElements();

		Job job = new Job("Opening console") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				doShowConsoles(selecteds);
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	protected void doShowConsoles(Collection<BootDashElement> selectedElements) {

		if (selectedElements != null) {

			Iterator<BootDashElement> it = selectedElements.iterator();

			// Show first element only for now
			if (it.hasNext()) {
				BootDashElement element = selectedElements.iterator().next();
				BootDashModel model = element.getBootDashModel();
				try {
					if (model.getElementConsoleManager() != null) {
						model.getElementConsoleManager().showConsole(element);
					}
				} catch (Exception e) {
					ui.errorPopup("Open Console Failure", e.getMessage());
				}
			}
		}
	}
}
