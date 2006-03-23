/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ActionDelegate;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.dialogs.message.ErrorDialog;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * The abstract superclass of all ActionDelegate implementors.
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractActionDelegate extends ActionDelegate {

	public final static int PROGRESS_DIALOG = 1;

	public final static int PROGRESS_BUSYCURSOR = 2;

	public final static boolean SYNC_EXEC = true;

	public final static boolean ASYNC_EXEC = false;

	// The current selection
	private IStructuredSelection selection;

	// The shell, required for the progress dialog
	private Shell defaultShell;

	public abstract boolean isEnabled();

	/**
	 * Convenience method for getting the current shell.
	 * @return the shell
	 */
	abstract protected Shell getShell();

	/**
	 * @return Returns the defaultShell.
	 */
	protected Shell getDefaultShell() {
		return defaultShell;
	}

	/**
	 * @param shell
	 *            The shell to set.
	 */
	protected void setDefaultShell(Shell shell) {
		this.defaultShell = shell;
	}

	/**
	 * @param selection
	 *            The selection to set.
	 */
	protected void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * Returns the selection.
	 * 
	 * @return IStructuredSelection
	 */
	protected IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Returns the selected projects.
	 * 
	 * @return the selected projects
	 */
	protected static IProject[] getSelectedProjects(IStructuredSelection selection) {
		// project list
		ArrayList projects = null;
		// if selection not empty
		if (!selection.isEmpty()) {
			projects = new ArrayList();
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				// if the selected element is an IProject
				if (next instanceof IProject) {
					projects.add(next);
					continue;
				}
				// if the selected element is an IAdaptable
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IProject) {
						projects.add(adapter);
						continue;
					}
				}
			}
		}
		// if projectList is not Empty
		IProject[] result = new IProject[0];
		if (projects != null && !projects.isEmpty()) {
			result = new IProject[projects.size()];
			projects.toArray(result);
			return result;
		}
		return result;
	}

	/**
	 * Returns the selected resources.
	 * 
	 * @return the selected resources
	 */
	protected static IResource[] getSelectedResources(IStructuredSelection selection) {
		// resource list
		ArrayList resources = null;
		// if selection not empty
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = selection.iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof IResource) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IResource) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		IResource[] result = new IResource[0];
		if (resources != null && !resources.isEmpty()) {
			result = new IResource[resources.size()];
			resources.toArray(result);
			return result;
		}
		return result;
	}

	/**
	 * Convenience method for running an operation with progress and error feedback.
	 * 
	 * @param runnable
	 *            the runnable which executes the operation
	 * @param problemMessage
	 *            the message to display in the case of errors
	 * @param progressKind
	 *            one of PROGRESS_BUSYCURSOR or PROGRESS_DIALOG
	 */
	final protected void run(final IRunnableWithProgress runnableWithProgress, final String problemMessage, final IRunnableContext progressMonitorDialog, boolean syncExec) {
		final Exception[] exceptions = new Exception[] { null };
		Display d = Display.findDisplay(Thread.currentThread());
		try {
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						progressMonitorDialog.run(false, true, runnableWithProgress);
					} catch (InvocationTargetException e) {
						SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, e.getMessage()));
						exceptions[0] = e;
					} catch (InterruptedException e) {
						SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, e.getMessage()));
						exceptions[0] = null;
					}
				}
			};
			if (syncExec) {
				d.syncExec(runnable);
			} else {
				d.asyncExec(runnable);
			}
		} catch (Exception e) {
			SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, e.getMessage()));
		}
	}

	/**
	 * Convenience method for running an operation without progress nor error feedback.
	 * 
	 * @param runnable
	 *            the runnable which executes the operation
	 * @param problemMessage
	 *            the message to display in the case of errors
	 * @param progressKind
	 *            one of PROGRESS_BUSYCURSOR or PROGRESS_DIALOG
	 */
	final protected void run(Runnable runnable, boolean syncExec) {
		Display d = Display.findDisplay(Thread.currentThread());
		try {
			if (syncExec) {
				d.syncExec(runnable);
			} else {
				d.asyncExec(runnable);
			}
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog("Launch error", "Impossible to launch runnable from action", e);
			errorDialog.open();
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			if (action != null) {
				try {
					action.setEnabled(isEnabled());
				} catch (Exception e) {
					SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, e.getMessage()));
					action.setEnabled(false);
				}
			}
		}
	}

}