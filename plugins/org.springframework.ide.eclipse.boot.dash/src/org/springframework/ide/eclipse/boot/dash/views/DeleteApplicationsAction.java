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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.ModifiableModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class DeleteApplicationsAction extends AbstractBootDashAction {

	private BootDashModel model;

	public DeleteApplicationsAction(BootDashModel model, MultiSelection<BootDashElement> selection,
			UserInteractions ui) {
		super(selection, ui);

		this.model = model;
		this.setText("Delete Application");
		this.setToolTipText(
				"Delete the selected application. The application will be permanently removed from the target.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/delete_app.gif"));
	}

	@Override
	public void run() {
		final List<BootDashElement> modelElementsToDelete = getSelectedElementsForThisModel();

		if (model instanceof ModifiableModel) {
			Job job = new Job("Deleting application") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					((ModifiableModel) model).delete(modelElementsToDelete, ui);
					return Status.OK_STATUS;
				}

			};
			job.schedule();
		}
	}

	@Override
	public void updateEnablement() {
		Collection<BootDashElement> modelElementsToDelete = getSelectedElementsForThisModel();

		// For now only support deletion of elements ONLY if all selections are in modifiable models.
		// For example, if a selection includes a local app, disable the action

		this.setEnabled(model instanceof ModifiableModel && !modelElementsToDelete.isEmpty()
				&& modelElementsToDelete.size() == getSelectedElements().size());
	}

	protected List<BootDashElement> getSelectedElementsForThisModel() {
		Collection<BootDashElement> all = getSelectedElements();
		List<BootDashElement> toDelete = new ArrayList<BootDashElement>();
		if (all != null) {
			for (BootDashElement el : all) {
				if (el.getParent() == model) {
					toDelete.add(el);
				}
			}
		}
		return toDelete;
	}
}
