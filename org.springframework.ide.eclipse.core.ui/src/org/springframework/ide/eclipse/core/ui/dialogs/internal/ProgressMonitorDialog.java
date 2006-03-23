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
package org.springframework.ide.eclipse.core.ui.dialogs.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springframework.ide.eclipse.core.ui.dialogs.AbstractMessageAndButtonDialog;

/**
 * A modal dialog that displays progress during a long running operation.
 * <p>
 * This concrete dialog class can be instantiated as is, or further subclassed as required.
 * </p>
 * <p>
 * Typical usage is:
 * 
 * <pre>
 *  
 *   
 *    try {
 *       IRunnableWithProgress op = ...;
 *       new ProgressMonitorDialog(activeShell).run(true, true, op);
 *    } catch (InvocationTargetException e) {
 *       // handle exception
 *    } catch (InterruptedException e) {
 *       // handle cancelation
 *    }
 *    
 *   
 * </pre>
 * 
 * </p>
 * <p>
 * Note that the ProgressMonitorDialog is not intended to be used with multiple runnables - this dialog should be discarded after completion of one IRunnableWithProgress and a new one instantiated for use by a second or sebsequent IRunnableWithProgress to ensure proper initialization.
 * </p>
 * <p>
 * Note that not forking the process will result in it running in the UI which may starve the UI. The most obvious symptom of this problem is non responsiveness of the cancel button. If you are running within the UI Thread you should do the bulk of your work in another Thread to prevent starvation. It is recommended that fork is set to true in most cases.
 * </p>
 * @author Pierre-Antoine Grégoire
 */
public class ProgressMonitorDialog extends AbstractMessageAndButtonDialog implements IRunnableContext {
	/**
	 * Name to use for task when normal task name is empty string.
	 */
	private static String DEFAULT_TASKNAME = JFaceResources.getString("ProgressMonitorDialog.message"); //$NON-NLS-1$

	/**
	 * SpringCoreUIConstants for label and monitor size
	 */
	private static int LABEL_DLUS = 21;

	private static int BAR_DLUS = 9;

	/**
	 * The progress indicator control.
	 */
	protected ProgressIndicator progressIndicator;

	/**
	 * The label control for the task. Kept for backwards compatibility.
	 */
	protected Label taskLabel;

	/**
	 * The label control for the subtask.
	 */
	protected Label subTaskLabel;

	/**
	 * The Cancel button control.
	 */
	protected Button cancel;

	/**
	 * Indicates whether the Cancel button is to be shown.
	 */
	protected boolean operationCancelableState = false;

	/**
	 * Indicates whether the Cancel button is to be enabled.
	 */
	protected boolean enableCancelButton;

	/**
	 * The progress monitor.
	 */
	private ProgressMonitor progressMonitor = new ProgressMonitor();

	/**
	 * The name of the current task (used by ProgressMonitor).
	 */
	private String task;

	/**
	 * The nesting depth of currently running runnables.
	 */
	private int nestingDepth;

	/**
	 * The cursor used in the cancel button;
	 */
	protected Cursor arrowCursor;

	/**
	 * The cursor used in the shell;
	 */
	private Cursor waitCursor;

	/**
	 * Flag indicating whether to open or merely create the dialog before run.
	 */
	private boolean openOnRun = true;

	/**
	 * Internal progress monitor implementation.
	 */
	private class ProgressMonitor implements IProgressMonitorWithBlocking {
		private String fSubTask = "";//$NON-NLS-1$

		private boolean fIsCanceled;

		protected boolean forked = false;

		protected boolean locked = false;

		public void beginTask(String name, int totalWork) {
			if (progressIndicator.isDisposed())
				return;
			if (name == null)
				task = "";//$NON-NLS-1$
			else
				task = name;
			String s = task;
			if (s.length() <= 0)
				s = DEFAULT_TASKNAME;
			setMessage(s);
			if (!forked)
				update();
			if (totalWork == UNKNOWN) {
				progressIndicator.beginAnimatedTask();
			} else {
				progressIndicator.beginTask(totalWork);
			}
		}

		public void done() {
			if (!progressIndicator.isDisposed()) {
				progressIndicator.sendRemainingWork();
				progressIndicator.done();
			}
		}

		public void setTaskName(String name) {
			if (name == null)
				task = "";//$NON-NLS-1$
			else
				task = name;
			String s = task;
			if (s.length() <= 0)
				s = DEFAULT_TASKNAME;
			setMessage(s);
			if (!forked)
				update();
		}

		public boolean isCanceled() {
			return fIsCanceled;
		}

		public void setCanceled(boolean b) {
			fIsCanceled = b;
			if (locked)
				clearBlocked();
		}

		public void subTask(String name) {
			if (subTaskLabel.isDisposed())
				return;
			if (name == null)
				fSubTask = "";//$NON-NLS-1$
			else
				fSubTask = name;
			subTaskLabel.setText(Dialog.shortenText(fSubTask, subTaskLabel));
			if (!forked)
				subTaskLabel.update();
		}

		public void worked(int work) {
			internalWorked(work);
		}

		public void internalWorked(double work) {
			if (!progressIndicator.isDisposed())
				progressIndicator.worked(work);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
		 */
		public void clearBlocked() {
			locked = false;
			updateForClearBlocked();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
		 */
		public void setBlocked(IStatus reason) {
			locked = true;
			updateForSetBlocked(reason);
		}
	}

	/**
	 * Clear blocked state from the receiver.
	 */
	protected void updateForClearBlocked() {
		setMessage(task);
		imageLabel.setImage(getImage());
	}

	/**
	 * Set blocked state from the receiver.
	 * 
	 * @param reason
	 *            IStatus that gives the details
	 */
	protected void updateForSetBlocked(IStatus reason) {
		setMessage(reason.getMessage());
		imageLabel.setImage(getImage());
	}

	/**
	 * Creates a progress monitor dialog under the given shell. The dialog has a standard title and no image. <code>open</code> is non-blocking.
	 * 
	 * @param parent
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 */
	public ProgressMonitorDialog(Shell parent) {
		super(parent);
		setShellStyle(getDefaultOrientation() | SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL); // no
		// close
		// button
		setBlockOnOpen(false);
	}

	/**
	 * Enables the cancel button (asynchronously).
	 * 
	 * @param b
	 *            The state to set the button to.
	 */
	private void asyncSetOperationCancelButtonEnabled(final boolean b) {
		if (getShell() != null) {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					setOperationCancelButtonEnabled(b);
				}
			});
		}
	}

	/**
	 * The cancel button has been pressed.
	 * 
	 * @since 3.0
	 */
	protected void cancelPressed() {
		// NOTE: this was previously done from a listener installed on the
		// cancel button. On GTK, the listener installed by
		// Dialog.createButton is called first and this was throwing an
		// exception because the cancel button was already disposed
		cancel.setEnabled(false);
		progressMonitor.setCanceled(true);
		super.cancelPressed();
	}

	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	/**
	 * The <code>ProgressMonitorDialog</code> implementation of this method only closes the dialog if there are no currently running runnables.
	 */
	public boolean close() {
		if (getNestingDepth() <= 0) {
			clearCursors();
			return super.close();
		}
		return false;
	}

	/**
	 * Clear the cursors in the dialog.
	 * 
	 * @since 3.0
	 */
	protected void clearCursors() {
		if (cancel != null && !cancel.isDisposed()) {
			cancel.setCursor(null);
		}
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.setCursor(null);
		}
		if (arrowCursor != null)
			arrowCursor.dispose();
		if (waitCursor != null)
			waitCursor.dispose();
		arrowCursor = null;
		waitCursor = null;
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(JFaceResources.getString("ProgressMonitorDialog.title")); //$NON-NLS-1$
		if (waitCursor == null)
			waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(waitCursor);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// cancel button
		createCancelButton(parent);
	}

	/**
	 * Creates the cancel button.
	 * 
	 * @param parent
	 *            the parent composite
	 * @since 3.0
	 */
	protected void createCancelButton(Composite parent) {
		cancel = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true, SWT.FLAT);
		if (arrowCursor == null)
			arrowCursor = new Cursor(cancel.getDisplay(), SWT.CURSOR_ARROW);
		cancel.setCursor(arrowCursor);
		setOperationCancelButtonEnabled(enableCancelButton);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		setMessage(DEFAULT_TASKNAME);
		createMessageArea(parent);
		// Only set for backwards compatibility
		taskLabel = messageLabel;
		// progress indicator
		progressIndicator = new ProgressIndicator(parent);
		GridData gd = new GridData();
		gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		progressIndicator.setLayoutData(gd);
		progressIndicator.setData(FormToolkit.KEY_DRAW_BORDER,FormToolkit.TEXT_BORDER);
		progressIndicator.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// label showing current task
		subTaskLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = convertVerticalDLUsToPixels(LABEL_DLUS);
		gd.horizontalSpan = 2;
		subTaskLabel.setLayoutData(gd);
		subTaskLabel.setFont(parent.getFont());
		subTaskLabel.setData(FormToolkit.KEY_DRAW_BORDER,FormToolkit.TEXT_BORDER);
		subTaskLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		getFormToolkit().paintBordersFor(parent);
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point calculatedSize = super.getInitialSize();
//		if (calculatedSize.x < 450)
//			calculatedSize.x = 450;
		return calculatedSize;
	}

	/**
	 * Returns the progress monitor to use for operations run in this progress dialog.
	 * 
	 * @return the progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	/*
	 * (non-Javadoc) Method declared on IRunnableContext. Runs the given <code> IRunnableWithProgress </code> with the progress monitor for this progress dialog. The dialog is opened before it is run, and closed after it completes.
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		setCancelable(cancelable);
		try {
			aboutToRun();
			// Let the progress monitor know if they need to update in UI Thread
			progressMonitor.forked = fork;
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
		} finally {
			finishedRun();
		}
	}

	/**
	 * Returns whether the dialog should be opened before the operation is run. Defaults to <code>true</code>
	 * 
	 * @return <code>true</code> to open the dialog before run, <code>false</code> to only create the dialog, but not open it
	 * @since 3.0
	 */
	public boolean getOpenOnRun() {
		return openOnRun;
	}

	/**
	 * Sets whether the dialog should be opened before the operation is run. NOTE: Setting this to false and not forking a process may starve any asyncExec that tries to open the dialog later.
	 * 
	 * @param openOnRun
	 *            <code>true</code> to open the dialog before run, <code>false</code> to only create the dialog, but not open it
	 * @since 3.0
	 */
	public void setOpenOnRun(boolean openOnRun) {
		this.openOnRun = openOnRun;
	}

	/**
	 * Returns the nesting depth of running operations.
	 * 
	 * @return the nesting depth of running operations
	 * @since 3.0
	 */
	protected int getNestingDepth() {
		return nestingDepth;
	}

	/**
	 * Increments the nesting depth of running operations.
	 * 
	 * @since 3.0
	 */
	protected void incrementNestingDepth() {
		nestingDepth++;
	}

	/**
	 * Decrements the nesting depth of running operations.
	 * 
	 * @since 3.0
	 * 
	 */
	protected void decrementNestingDepth() {
		nestingDepth--;
	}

	/**
	 * Called just before the operation is run. Default behaviour is to open or create the dialog, based on the setting of <code>getOpenOnRun</code>, and increment the nesting depth.
	 * 
	 * @since 3.0
	 */
	protected void aboutToRun() {
		if (getOpenOnRun()) {
			open();
		} else {
			create();
		}
		incrementNestingDepth();
	}

	/**
	 * Called just after the operation is run. Default behaviour is to decrement the nesting depth, and close the dialog.
	 * 
	 * @since 3.0
	 */
	protected void finishedRun() {
		decrementNestingDepth();
		close();
	}

	/**
	 * Sets whether the progress dialog is cancelable or not.
	 * 
	 * @param cancelable
	 *            <code>true</code> if the end user can cancel this progress dialog, and <code>false</code> if it cannot be canceled
	 */
	public void setCancelable(boolean cancelable) {
		if (cancel == null)
			enableCancelButton = cancelable;
		else
			asyncSetOperationCancelButtonEnabled(cancelable);
	}

	/**
	 * Helper to enable/disable Cancel button for this dialog.
	 * 
	 * @param b
	 *            <code>true</code> to enable the cancel button, and <code>false</code> to disable it
	 * @since 3.0
	 */
	protected void setOperationCancelButtonEnabled(boolean b) {
		operationCancelableState = b;
		cancel.setEnabled(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {
		return getImage();
	}

	/**
	 * Set the message in the message label.
	 * 
	 * @param messageString
	 *            The string for the new message.
	 */
	private void setMessage(String messageString) {
		// must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.setText(Dialog.shortenText(message, messageLabel));
	}

	/**
	 * Update the message label. Required if the monitor is forked.
	 */
	private void update() {
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		// Check to be sure it is not already done. If it is just return OK.
		if (!getOpenOnRun()) {
			if (getNestingDepth() == 0)
				return OK;
		}
		return super.open();
	}
}
