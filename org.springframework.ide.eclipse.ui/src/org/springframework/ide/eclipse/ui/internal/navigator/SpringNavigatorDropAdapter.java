/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.internal.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;
import org.springframework.ide.eclipse.ui.internal.actions.AddRemoveNature;

/**
 * {@link CommonDropAdapterAssistant} that handles drop requests of
 * {@link IProject} instances to the Spring Explorer.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class SpringNavigatorDropAdapter extends CommonDropAdapterAssistant {
	
	/**
	 * Gets the dragged {@link IProject} instance. If this can't be resolved
	 * <code>null</code> is returned.
	 */
	private IProject getProjectFromDropTarget(DropTargetEvent dropTargetEvent) {
		Object object = dropTargetEvent.data;
		if (object instanceof ITreeSelection) {
			object = ((ITreeSelection) object).getFirstElement();
		}
		if (object instanceof IProject) {
			return (IProject) object;
		}
		else if (object instanceof IAdaptable) {
			return (IProject) ((IAdaptable) object).getAdapter(IProject.class);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Executes the drop action. Checks if the Spring Nature is already added to
	 * the project; if so nothing happens. If the project is not applied, it
	 * will be added.
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent dropTargetEvent, Object target) {
		IProject project = getProjectFromDropTarget(dropTargetEvent);
		if (project == null || SpringCoreUtils.isSpringProject(project)) {
			return Status.CANCEL_STATUS;
		}
		else {
			// TODO CD refactor to use common util method; see AddRemoveNature
			IProgressMonitor pm = new NullProgressMonitor();
			try {
				SpringCoreUtils.removeProjectNature(project,
						AddRemoveNature.OLD_NATURE_ID1, pm);
				SpringCoreUtils.removeProjectNature(project,
						AddRemoveNature.OLD_NATURE_ID2, pm);
				SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID,
						pm);
			}
			catch (CoreException e) {
				MessageDialog.openError(SpringUIPlugin
						.getActiveWorkbenchShell(),
						SpringUIMessages.ProjectNature_errorMessage, NLS.bind(
								SpringUIMessages.ProjectNature_addError,
								project.getName(), e.getLocalizedMessage()));
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Checks if the drop request is actually support by this
	 * {@link CommonDropAdapterAssistant}.
	 * <p>
	 * Because JDT's package explorer only supports {@link DND#DROP_COPY}
	 * requests we check if this is the current operation. Eligible target is in
	 * the scope of this implementation only the {@link IWorkspaceRoot}.
	 * <p>
	 * Note: For some reason this method is called a second time (once the drop
	 * has been initiated by a mouse button release) by the common navigator
	 * framework with a possible <code>null</code> target.
	 */
	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		if (operation == DND.DROP_COPY
				&& (target instanceof IWorkspaceRoot || target == null)) {
			return Status.OK_STATUS;
		}
		else {
			return Status.CANCEL_STATUS;
		}
	}
}