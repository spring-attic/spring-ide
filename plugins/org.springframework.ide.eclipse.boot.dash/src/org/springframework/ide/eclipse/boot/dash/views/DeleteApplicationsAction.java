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
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DeleteApplicationsAction extends AbstractBootDashElementsAction {

	public DeleteApplicationsAction(MultiSelection<BootDashElement> selection,
			UserInteractions ui) {
		super(selection, ui);
		Assert.isNotNull(ui);
		this.setText("Delete Application");
		this.setToolTipText(
				"Delete the selected application. The application will be permanently removed from the target.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/delete_app.gif"));
	}

	@Override
	public void run() {
		//Deletes are implemented per BootDashModel. So sort selection into bins per model.
		Multimap<BootDashModel, BootDashElement> sortingBins = HashMultimap.create();
		for (BootDashElement e : getSelectedElements()) {
			BootDashModel model = e.getParent();
			//We are only capable of removing elements from a ModifiableModel (the 'local' model is read-only).
			if (model instanceof ModifiableModel) {
				sortingBins.put(model, e);
			}
		}
		//Now delete elements from corresponding models.
		for (final Entry<BootDashModel, Collection<BootDashElement>> workitem : sortingBins.asMap().entrySet()) {
			BootDashModel model = workitem.getKey();
			final ModifiableModel modifiable = (ModifiableModel)model; //cast is safe. Only ModifiableModel are added to sortingBins
			Job job = new Job("Deleting apps from "+model.getRunTarget().getName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					modifiable.delete(workitem.getValue(), ui);
					return Status.OK_STATUS;
				}

			};
			job.schedule();
		}
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(shouldEnableFor(getSelectedElements()));
	}

	private boolean shouldEnableFor(Collection<BootDashElement> selectedElements) {
		if (selectedElements.isEmpty()) {
			//If no elements are selected, then action would do nothing, so disable it.
			return false;
		}
		//All selected elements must be deletable, then this action is enabled.
		for (BootDashElement bde : selectedElements) {
			if (!canDelete(bde)) {
				return false;
			}
		}
		return true;
	}

	private boolean canDelete(BootDashElement bde) {
		return bde.getParent() instanceof ModifiableModel;
	}

}
