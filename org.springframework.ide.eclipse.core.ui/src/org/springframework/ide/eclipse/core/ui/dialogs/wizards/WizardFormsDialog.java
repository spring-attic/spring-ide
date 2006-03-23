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
package org.springframework.ide.eclipse.core.ui.dialogs.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer2;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.springframework.ide.eclipse.core.ui.dialogs.internal.ProgressMonitorPart;
import org.springframework.ide.eclipse.core.ui.dialogs.internal.TitleAreaFormsDialog;

/**
 * A dialog to show a wizard to the end user.
 * <p>
 * In typical usage, the client instantiates this class with a particular wizard. The dialog serves as the wizard container and orchestrates the presentation of its pages.
 * <p>
 * The standard layout is roughly as follows: it has an area at the top containing both the wizard's title, description, and image; the actual wizard page appears in the middle; below that is a progress indicator (which is made visible if needed); and at the bottom of the page is message line and a button bar containing Help, Next, Back, Finish, and Cancel buttons (or some subset).
 * </p>
 * <p>
 * Clients may subclass <code>WizardFormsDialog</code>, although this is rarely required.
 * </p>
 * @author Pierre-Antoine Grégoire
 */
public class WizardFormsDialog extends TitleAreaFormsDialog implements IWizardContainer2, IPageChangeProvider {
	/**
	 * Image registry key for error message image (value <code>"dialog_title_error_image"</code>).
	 */
	public static final String WIZ_IMG_ERROR = "dialog_title_error_image"; //$NON-NLS-1$

	// The wizard the dialog is currently showing.
	private IWizard wizard;

	// Wizards to dispose
	private ArrayList createdWizards = new ArrayList();

	// Current nested wizards
	private ArrayList nestedWizards = new ArrayList();

	// The currently displayed page.
	private IWizardPage currentPage = null;

	// The number of long running operation executed from the dialog.
	private long activeRunningOperations = 0;

	// The current page message and description
	private String pageMessage;

	private int pageMessageType = IMessageProvider.NONE;

	private String pageDescription;

	// The progress monitor
	private ProgressMonitorPart progressMonitorPart;

	private Cursor waitCursor;

	private Cursor arrowCursor;

	private MessageDialog windowClosingDialog;

	// Navigation buttons
	private Button backButton;

	private Button nextButton;

	private Button finishButton;

	private Button cancelButton;

	private Button helpButton;

	private SelectionAdapter cancelListener;

	private boolean isMovingToPreviousPage = false;

	private Composite pageContainer;

	private PageContainerFillLayout pageContainerLayout = new PageContainerFillLayout(0, 0, 300, 225);

	private int pageWidth = SWT.DEFAULT;

	private int pageHeight = SWT.DEFAULT;

	private static final String FOCUS_CONTROL = "focusControl"; //$NON-NLS-1$

	private boolean lockedUI = false;

	private ListenerList pageChangedListeners = new ListenerList(3);

	/**
	 * A layout for a container which includes several pages, like a notebook, wizard, or preference dialog. The size computed by this layout is the maximum width and height of all pages currently inserted into the container.
	 */
	protected class PageContainerFillLayout extends Layout {
		/**
		 * The margin width; <code>5</code> pixels by default.
		 */
		public int marginWidth = 5;

		/**
		 * The margin height; <code>5</code> pixels by default.
		 */
		public int marginHeight = 5;

		/**
		 * The minimum width; <code>0</code> pixels by default.
		 */
		public int minimumWidth = 0;

		/**
		 * The minimum height; <code>0</code> pixels by default.
		 */
		public int minimumHeight = 0;

		/**
		 * Creates new layout object.
		 * 
		 * @param mw
		 *            the margin width
		 * @param mh
		 *            the margin height
		 * @param minW
		 *            the minimum width
		 * @param minH
		 *            the minimum height
		 */
		public PageContainerFillLayout(int mw, int mh, int minW, int minH) {
			marginWidth = mw;
			marginHeight = mh;
			minimumWidth = minW;
			minimumHeight = minH;
		}

		/*
		 * (non-Javadoc) Method declared on Layout.
		 */
		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			Point result = null;
			Control[] children = composite.getChildren();
			if (children.length > 0) {
				result = new Point(0, 0);
				for (int i = 0; i < children.length; i++) {
					Point cp = children[i].computeSize(wHint, hHint, force);
					result.x = Math.min(result.x, cp.x);
					result.y = Math.min(result.y, cp.y);
				}
				result.x = result.x + 2 * marginWidth;
				result.y = result.y + 2 * marginHeight;
			} else {
				Rectangle rect = composite.getClientArea();
				result = new Point(rect.width, rect.height);
			}
			result.x = Math.max(result.x, minimumWidth);
			result.y = Math.max(result.y, minimumHeight);
			if (wHint != SWT.DEFAULT)
				result.x = wHint;
			if (hHint != SWT.DEFAULT)
				result.y = hHint;
			return result;
		}

		/**
		 * Returns the client area for the given composite according to this layout.
		 * 
		 * @param c
		 *            the composite
		 * @return the client area rectangle
		 */
		public Rectangle getClientArea(Composite c) {
			Rectangle rect = c.getClientArea();
			rect.x = rect.x + marginWidth;
			rect.y = rect.y + marginHeight;
			rect.width = rect.width - 2 * marginWidth;
			rect.height = rect.height - 2 * marginHeight;
			return rect;
		}

		/*
		 * (non-Javadoc) Method declared on Layout.
		 */
		public void layout(Composite composite, boolean force) {
			Rectangle rect = getClientArea(composite);
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setBounds(rect);
			}
		}

		/**
		 * Lays outs the page according to this layout.
		 * 
		 * @param w
		 *            the control
		 */
		public void layoutPage(Control w) {
			w.setBounds(getClientArea(w.getParent()));
		}

		/**
		 * Sets the location of the page so that its origin is in the upper left corner.
		 * 
		 * @param w
		 *            the control
		 */
		public void setPageLocation(Control w) {
			w.setLocation(marginWidth, marginHeight);
		}
	}

	/**
	 * Creates a new wizard dialog for the given wizard.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param newWizard
	 *            the wizard this dialog is working on
	 */
	public WizardFormsDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
		setWizard(newWizard);
		// since VAJava can't initialize an instance var with an anonymous
		// class outside a constructor we do it here:
		cancelListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		};
	}

	/**
	 * About to start a long running operation triggered through the wizard. Shows the progress monitor and disables the wizard's buttons and controls.
	 * 
	 * @param enableCancelButton
	 *            <code>true</code> if the Cancel button should be enabled, and <code>false</code> if it should be disabled
	 * @return the saved UI state
	 */
	private Object aboutToStart(boolean enableCancelButton) {
		Map savedState = null;
		if (getShell() != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell())
				focusControl = null;
			boolean needsProgressMonitor = wizard.needsProgressMonitor();
			cancelButton.removeSelectionListener(cancelListener);
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(waitCursor);
			// Set the arrow cursor to the cancel component.
			arrowCursor = new Cursor(d, SWT.CURSOR_ARROW);
			cancelButton.setCursor(arrowCursor);
			// Deactivate shell
			savedState = saveUIState(needsProgressMonitor && enableCancelButton);
			if (focusControl != null)
				savedState.put(FOCUS_CONTROL, focusControl);
			// Attach the progress monitor part to the cancel button
			if (needsProgressMonitor) {
				progressMonitorPart.attachToCancelComponent(cancelButton);
				progressMonitorPart.setVisible(true);
			}
		}
		return savedState;
	}

	/**
	 * The Back button has been pressed.
	 */
	protected void backPressed() {
		IWizardPage page = currentPage.getPreviousPage();
		if (page == null)
			// should never happen since we have already visited the page
			return;
		// set flag to indicate that we are moving back
		isMovingToPreviousPage = true;
		// show the page
		showPage(page);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case IDialogConstants.HELP_ID: {
			helpPressed();
			break;
		}
		case IDialogConstants.BACK_ID: {
			backPressed();
			break;
		}
		case IDialogConstants.NEXT_ID: {
			nextPressed();
			break;
		}
		case IDialogConstants.FINISH_ID: {
			finishPressed();
			break;
		}
		// The Cancel button has a listener which calls cancelPressed directly
		}
	}

	/**
	 * Calculates the difference in size between the given page and the page container. A larger page results in a positive delta.
	 * 
	 * @param page
	 *            the page
	 * @return the size difference encoded as a <code>new Point(deltaWidth,deltaHeight)</code>
	 */
	private Point calculatePageSizeDelta(IWizardPage page) {
		Control pageControl = page.getControl();
		if (pageControl == null)
			// control not created yet
			return new Point(0, 0);
		Point contentSize = pageControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Rectangle rect = pageContainerLayout.getClientArea(pageContainer);
		Point containerSize = new Point(rect.width, rect.height);
		return new Point(Math.max(0, contentSize.x - containerSize.x), Math.max(0, contentSize.y - containerSize.y));
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void cancelPressed() {
		if (activeRunningOperations <= 0) {
			// Close the dialog. The check whether the dialog can be
			// closed or not is done in <code>okToClose</code>.
			// This ensures that the check is also evaluated when the user
			// presses the window's close button.
			setReturnCode(CANCEL);
			close();
		} else {
			cancelButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (okToClose())
			return hardClose();
		return false;
	}

	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		// Register help listener on the shell
		newShell.addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				// call perform help on the current page
				if (currentPage != null) {
					currentPage.performHelp();
				}
			}
		});
	}

	/**
	 * Creates and returns the contents of this dialog's button bar.
	 * <p>
	 * The <code>WizardFormsDialog</code> implementation of this framework method prevents the composite's columns from being made equal width in order to remove the margin between the Back and Next buttons.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite to contain the button bar
	 * @return the button bar control
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = (Composite) super.createButtonBar(parent);
		composite.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		((GridLayout) composite.getLayout()).makeColumnsEqualWidth = false;
		return composite;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (wizard.isHelpAvailable()) {
			helpButton = createButton(parent, IDialogConstants.HELP_ID, IDialogConstants.HELP_LABEL, false, SWT.FLAT);
		}
		if (wizard.needsPreviousAndNextButtons())
			createPreviousAndNextButtons(parent);
		finishButton = createButton(parent, IDialogConstants.FINISH_ID, IDialogConstants.FINISH_LABEL, true, SWT.FLAT);
		cancelButton = createCancelButton(parent);
	}

	/**
	 * Creates the Cancel button for this wizard dialog. Creates a standard (<code>SWT.PUSH</code>) button and registers for its selection events. Note that the number of columns in the button bar composite is incremented. The Cancel button is created specially to give it a removeable listener.
	 * 
	 * @param parent
	 *            the parent button bar
	 * @return the new Cancel button
	 */
	private Button createCancelButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.FLAT);
		button.setText(IDialogConstants.CANCEL_LABEL);
		setButtonLayoutData(button);
		button.setFont(parent.getFont());
		button.setData(new Integer(IDialogConstants.CANCEL_ID));
		button.addSelectionListener(cancelListener);
		return button;
	}

	/**
	 * Return the cancel button if the id is a the cancel id.
	 * 
	 * @param id
	 *            the button id
	 * @return the button corresponding to the button id
	 */
	protected Button getButton(int id) {
		if (id == IDialogConstants.CANCEL_ID)
			return cancelButton;
		return super.getButton(id);
	}

	/**
	 * The <code>WizardFormsDialog</code> implementation of this <code>Window</code> method calls call <code>IWizard.addPages</code> to allow the current wizard to add extra pages, then <code>super.createContents</code> to create the controls. It then calls <code>IWizard.createPageControls</code> to allow the wizard to pre-create their page controls prior to opening, so that the wizard opens to the correct size. And finally it shows the first page.
	 */
	protected Control createContents(Composite parent) {
		setFormToolkit(new FormToolkit(parent.getDisplay()));
		// Allow the wizard to add pages to itself
		// Need to call this now so page count is correct
		// for determining if next/previous buttons are needed
		wizard.addPages();
		Control contents = super.createContents(parent);
		contents.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// Allow the wizard pages to precreate their page controls
		createPageControls();
		// Show the first page
		showStartingPage();
		return contents;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		// Build the Page container
		pageContainer = createPageContainer(composite);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = pageWidth;
		gd.heightHint = pageHeight;
		pageContainer.setLayoutData(gd);
		pageContainer.setFont(parent.getFont());
		Label separator = getFormToolkit().createLabel(composite, "", SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// Insert a progress monitor
		GridLayout pmlayout = new GridLayout();
		pmlayout.numColumns = 1;
		progressMonitorPart = createProgressMonitorPart(composite, pmlayout);
		progressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressMonitorPart.setVisible(false);
		// Build the separator line
		separator = getFormToolkit().createLabel(composite, "", SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		pageContainer.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		Dialog.applyDialogFont(progressMonitorPart);
		return composite;
	}

	/**
	 * Create the progress monitor part in the receiver.
	 * 
	 * @param composite
	 * @param pmlayout
	 * @return ProgressMonitorPart
	 */
	protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {
		return new ProgressMonitorPart(composite, pmlayout, SWT.DEFAULT) {
			String currentTask = null;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#setBlocked(org.eclipse.core.runtime.IStatus)
			 */
			public void setBlocked(IStatus reason) {
				super.setBlocked(reason);
				if (!lockedUI)// Do not show blocked if we are locking the UI
					Dialog.getBlockedHandler().showBlocked(getShell(), this, reason, currentTask);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#clearBlocked()
			 */
			public void clearBlocked() {
				super.clearBlocked();
				if (!lockedUI)// Do not vlear if we never set it
					Dialog.getBlockedHandler().clearBlocked();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#beginTask(java.lang.String, int)
			 */
			public void beginTask(String name, int totalWork) {
				super.beginTask(name, totalWork);
				currentTask = name;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#setTaskName(java.lang.String)
			 */
			public void setTaskName(String name) {
				super.setTaskName(name);
				currentTask = name;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.wizard.ProgressMonitorPart#subTask(java.lang.String)
			 */
			public void subTask(String name) {
				super.subTask(name);
				// If we haven't got anything yet use this value for more context
				if (currentTask == null)
					currentTask = name;
			}
		};
	}

	/**
	 * Creates the container that holds all pages.
	 * 
	 * @param parent
	 * @return Composite
	 */
	private Composite createPageContainer(Composite parent) {
		Composite result = getFormToolkit().createComposite(parent, SWT.NULL);
		result.setLayout(pageContainerLayout);
		return result;
	}

	/**
	 * Allow the wizard's pages to pre-create their page controls. This allows the wizard dialog to open to the correct size.
	 */
	private void createPageControls() {
		// Allow the wizard pages to precreate their page controls
		// This allows the wizard to open to the correct size
		wizard.createPageControls(pageContainer);
		// Ensure that all of the created pages are initially not visible
		IWizardPage[] pages = wizard.getPages();
		for (int i = 0; i < pages.length; i++) {
			IWizardPage page = pages[i];
			if (page.getControl() != null)
				page.getControl().setVisible(false);
		}
	}

	/**
	 * Creates the Previous and Next buttons for this wizard dialog. Creates standard (<code>SWT.PUSH</code>) buttons and registers for their selection events. Note that the number of columns in the button bar composite is incremented. These buttons are created specially to prevent any space between them.
	 * 
	 * @param parent
	 *            the parent button bar
	 * @return a composite containing the new buttons
	 */
	private Composite createPreviousAndNextButtons(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Composite composite = getFormToolkit().createComposite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // will be incremented by createButton
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		backButton = createButton(composite, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false, SWT.FLAT);
		nextButton = createButton(composite, IDialogConstants.NEXT_ID, IDialogConstants.NEXT_LABEL, false, SWT.FLAT);
		return composite;
	}

	/**
	 * Creates and return a new wizard closing dialog without openiong it.
	 * 
	 * @return MessageDalog
	 */
	private MessageDialog createWizardClosingDialog() {
		MessageDialog result = new MessageDialog(getShell(), JFaceResources.getString("WizardClosingDialog.title"), //$NON-NLS-1$
				null, JFaceResources.getString("WizardClosingDialog.message"), //$NON-NLS-1$
				MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL }, 0);
		return result;
	}

	/**
	 * The Finish button has been pressed.
	 */
	protected void finishPressed() {
		// Wizards are added to the nested wizards list in setWizard.
		// This means that the current wizard is always the last wizard in the list.
		// Note that we first call the current wizard directly (to give it a chance to
		// abort, do work, and save state) then call the remaining n-1 wizards in the
		// list (to save state).
		if (wizard.performFinish()) {
			// Call perform finish on outer wizards in the nested chain
			// (to allow them to save state for example)
			for (int i = 0; i < nestedWizards.size() - 1; i++) {
				((IWizard) nestedWizards.get(i)).performFinish();
			}
			// Hard close the dialog.
			setReturnCode(OK);
			hardClose();
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public IWizardPage getCurrentPage() {
		return currentPage;
	}

	/**
	 * Returns the progress monitor for this wizard dialog (if it has one).
	 * 
	 * @return the progress monitor, or <code>null</code> if this wizard dialog does not have one
	 */
	protected IProgressMonitor getProgressMonitor() {
		return progressMonitorPart;
	}

	/**
	 * Returns the wizard this dialog is currently displaying.
	 * 
	 * @return the current wizard
	 */
	protected IWizard getWizard() {
		return wizard;
	}

	/**
	 * Closes this window.
	 * 
	 * @return <code>true</code> if the window is (or was already) closed, and <code>false</code> if it is still open
	 */
	private boolean hardClose() {
		// inform wizards
		for (int i = 0; i < createdWizards.size(); i++) {
			IWizard createdWizard = (IWizard) createdWizards.get(i);
			createdWizard.dispose();
			// Remove this dialog as a parent from the managed wizard.
			// Note that we do this after calling dispose as the wizard or
			// its pages may need access to the container during
			// dispose code
			createdWizard.setContainer(null);
		}
		return super.close();
	}

	/**
	 * The Help button has been pressed.
	 */
	protected void helpPressed() {
		if (currentPage != null) {
			currentPage.performHelp();
		}
	}

	/**
	 * The Next button has been pressed.
	 */
	protected void nextPressed() {
		IWizardPage page = currentPage.getNextPage();
		if (page == null) {
			// something must have happend getting the next page
			return;
		}
		// show the next page
		showPage(page);
	}

	/**
	 * Checks whether it is alright to close this wizard dialog and performed standard cancel processing. If there is a long running operation in progress, this method posts an alert message saying that the wizard cannot be closed.
	 * 
	 * @return <code>true</code> if it is alright to close this dialog, and <code>false</code> if it is not
	 */
	private boolean okToClose() {
		if (activeRunningOperations > 0) {
			synchronized (this) {
				windowClosingDialog = createWizardClosingDialog();
			}
			windowClosingDialog.open();
			synchronized (this) {
				windowClosingDialog = null;
			}
			return false;
		}
		return wizard.performCancel();
	}

	/**
	 * Restores the enabled/disabled state of the given control.
	 * 
	 * @param w
	 *            the control
	 * @param h
	 *            the map (key type: <code>String</code>, element type: <code>Boolean</code>)
	 * @param key
	 *            the key
	 * @see #saveEnableStateAndSet
	 */
	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}

	/**
	 * Restores the enabled/disabled state of the wizard dialog's buttons and the tree of controls for the currently showing page.
	 * 
	 * @param state
	 *            a map containing the saved state as returned by <code>saveUIState</code>
	 * @see #saveUIState
	 */
	private void restoreUIState(Map state) {
		restoreEnableState(backButton, state, "back"); //$NON-NLS-1$
		restoreEnableState(nextButton, state, "next"); //$NON-NLS-1$
		restoreEnableState(finishButton, state, "finish"); //$NON-NLS-1$
		restoreEnableState(cancelButton, state, "cancel"); //$NON-NLS-1$
		restoreEnableState(helpButton, state, "help"); //$NON-NLS-1$
		Object pageValue = state.get("page"); //$NON-NLS-1$
		if (pageValue != null)// page may never have been created
			((ControlEnableState) pageValue).restore();
	}

	/*
	 * (non-Javadoc) Method declared on IRunnableContext.
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		// The operation can only be canceled if it is executed in a separate thread.
		// Otherwise the UI is blocked anyway.
		Object state = null;
		if (activeRunningOperations == 0)
			state = aboutToStart(fork && cancelable);
		activeRunningOperations++;
		try {
			if (!fork)// If we are not forking do not open other dialogs
				lockedUI = true;
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
			lockedUI = false;
		} finally {
			activeRunningOperations--;
			// Stop if this is the last one
			if (state != null)
				stopped(state);
		}
	}

	/**
	 * Saves the enabled/disabled state of the given control in the given map, which must be modifiable.
	 * 
	 * @param w
	 *            the control, or <code>null</code> if none
	 * @param h
	 *            the map (key type: <code>String</code>, element type: <code>Boolean</code>)
	 * @param key
	 *            the key
	 * @param enabled
	 *            <code>true</code> to enable the control, and <code>false</code> to disable it
	 * @see #restoreEnableState(Control, Map, String)
	 */
	private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
		if (w != null) {
			h.put(key, new Boolean(w.getEnabled()));
			w.setEnabled(enabled);
		}
	}

	/**
	 * Captures and returns the enabled/disabled state of the wizard dialog's buttons and the tree of controls for the currently showing page. All these controls are disabled in the process, with the possible excepton of the Cancel button.
	 * 
	 * @param keepCancelEnabled
	 *            <code>true</code> if the Cancel button should remain enabled, and <code>false</code> if it should be disabled
	 * @return a map containing the saved state suitable for restoring later with <code>restoreUIState</code>
	 * @see #restoreUIState
	 */
	private Map saveUIState(boolean keepCancelEnabled) {
		Map savedState = new HashMap(10);
		saveEnableStateAndSet(backButton, savedState, "back", false); //$NON-NLS-1$
		saveEnableStateAndSet(nextButton, savedState, "next", false); //$NON-NLS-1$
		saveEnableStateAndSet(finishButton, savedState, "finish", false); //$NON-NLS-1$
		saveEnableStateAndSet(cancelButton, savedState, "cancel", keepCancelEnabled); //$NON-NLS-1$
		saveEnableStateAndSet(helpButton, savedState, "help", false); //$NON-NLS-1$
		if (currentPage != null)
			savedState.put("page", ControlEnableState.disable(currentPage.getControl())); //$NON-NLS-1$
		return savedState;
	}

	/**
	 * Sets the given cursor for all shells currently active for this window's display.
	 * 
	 * @param c
	 *            the cursor
	 */
	private void setDisplayCursor(Cursor c) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}

	/**
	 * Sets the minimum page size used for the pages.
	 * 
	 * @param minWidth
	 *            the minimum page width
	 * @param minHeight
	 *            the minimum page height
	 * @see #setMinimumPageSize(Point)
	 */
	public void setMinimumPageSize(int minWidth, int minHeight) {
		Assert.isTrue(minWidth >= 0 && minHeight >= 0);
		pageContainerLayout.minimumWidth = minWidth;
		pageContainerLayout.minimumHeight = minHeight;
	}

	/**
	 * Sets the minimum page size used for the pages.
	 * 
	 * @param size
	 *            the page size encoded as <code>new Point(width,height)</code>
	 * @see #setMinimumPageSize(int,int)
	 */
	public void setMinimumPageSize(Point size) {
		setMinimumPageSize(size.x, size.y);
	}

	/**
	 * Sets the size of all pages. The given size takes precedence over computed sizes.
	 * 
	 * @param width
	 *            the page width
	 * @param height
	 *            the page height
	 * @see #setPageSize(Point)
	 */
	public void setPageSize(int width, int height) {
		pageWidth = width;
		pageHeight = height;
	}

	/**
	 * Sets the size of all pages. The given size takes precedence over computed sizes.
	 * 
	 * @param size
	 *            the page size encoded as <code>new Point(width,height)</code>
	 * @see #setPageSize(int,int)
	 */
	public void setPageSize(Point size) {
		setPageSize(size.x, size.y);
	}

	/**
	 * Sets the wizard this dialog is currently displaying.
	 * 
	 * @param newWizard
	 *            the wizard
	 */
	protected void setWizard(IWizard newWizard) {
		wizard = newWizard;
		wizard.setContainer(this);
		if (!createdWizards.contains(wizard)) {
			createdWizards.add(wizard);
			// New wizard so just add it to the end of our nested list
			nestedWizards.add(wizard);
			if (pageContainer != null) {
				// Dialog is already open
				// Allow the wizard pages to precreate their page controls
				// This allows the wizard to open to the correct size
				createPageControls();
				// Ensure the dialog is large enough for the wizard
				updateSizeForWizard(wizard);
				pageContainer.layout(true);
			}
		} else {
			// We have already seen this wizard, if it is the previous wizard
			// on the nested list then we assume we have gone back and remove
			// the last wizard from the list
			int size = nestedWizards.size();
			if (size >= 2 && nestedWizards.get(size - 2) == wizard)
				nestedWizards.remove(size - 1);
			else
				// Assume we are going forward to revisit a wizard
				nestedWizards.add(wizard);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void showPage(IWizardPage page) {
		if (page == null || page == currentPage) {
			return;
		}
		if (!isMovingToPreviousPage)
			// remember my previous page.
			page.setPreviousPage(currentPage);
		else
			isMovingToPreviousPage = false;
		// Update for the new page ina busy cursor if possible
		if (getContents() == null)
			updateForPage(page);

		else {
			final IWizardPage finalPage = page;
			BusyIndicator.showWhile(getContents().getDisplay(), new Runnable() {
				public void run() {
					updateForPage(finalPage);
				}
			});
		}
	}

	/**
	 * Update the receiver for the new page.
	 * 
	 * @param page
	 */
	private void updateForPage(IWizardPage page) {
		// ensure this page belongs to the current wizard
		if (wizard != page.getWizard())
			setWizard(page.getWizard());
		// ensure that page control has been created
		// (this allows lazy page control creation)
		if (page.getControl() == null) {
			page.createControl(pageContainer);
			// the page is responsible for ensuring the created control is accessable
			// via getControl.
			Assert.isNotNull(page.getControl());
			// ensure the dialog is large enough for this page
			updateSize(page);
		}
		// make the new page visible
		IWizardPage oldPage = currentPage;
		currentPage = page;
		currentPage.setVisible(true);
		if (oldPage != null)
			oldPage.setVisible(false);
		// update the dialog controls
		update();
	}

	/**
	 * Shows the starting page of the wizard.
	 */
	private void showStartingPage() {
		currentPage = wizard.getStartingPage();
		if (currentPage == null) {
			// something must have happend getting the page
			return;
		}
		// ensure the page control has been created
		if (currentPage.getControl() == null) {
			currentPage.createControl(pageContainer);
			// the page is responsible for ensuring the created control is accessable
			// via getControl.
			Assert.isNotNull(currentPage.getControl());
			// we do not need to update the size since the call
			// to initialize bounds has not been made yet.
		}
		// make the new page visible
		currentPage.setVisible(true);
		// update the dialog controls
		update();
	}

	/**
	 * A long running operation triggered through the wizard was stopped either by user input or by normal end. Hides the progress monitor and restores the enable state wizard's buttons and controls.
	 * 
	 * @param savedState
	 *            the saved UI state as returned by <code>aboutToStart</code>
	 * @see #aboutToStart
	 */
	private void stopped(Object savedState) {
		if (getShell() != null) {
			if (wizard.needsProgressMonitor()) {
				progressMonitorPart.setVisible(false);
				progressMonitorPart.removeFromCancelComponent(cancelButton);
			}
			Map state = (Map) savedState;
			restoreUIState(state);
			cancelButton.addSelectionListener(cancelListener);
			setDisplayCursor(null);
			cancelButton.setCursor(null);
			waitCursor.dispose();
			waitCursor = null;
			arrowCursor.dispose();
			arrowCursor = null;
			Control focusControl = (Control) state.get(FOCUS_CONTROL);
			if (focusControl != null)
				focusControl.setFocus();
		}
	}

	/**
	 * Updates this dialog's controls to reflect the current page.
	 */
	protected void update() {
		// Update the window title
		updateWindowTitle();
		// Update the title bar
		updateTitleBar();
		// Update the buttons
		updateButtons();
		// Fires the page change event
		firePageChanged(new PageChangedEvent(this, getCurrentPage()));
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateButtons() {
		boolean canFlipToNextPage = false;
		boolean canFinish = wizard.canFinish();
		if (backButton != null)
			backButton.setEnabled(currentPage.getPreviousPage() != null);
		if (nextButton != null) {
			canFlipToNextPage = currentPage.canFlipToNextPage();
			nextButton.setEnabled(canFlipToNextPage);
		}
		finishButton.setEnabled(canFinish);
		// finish is default unless it is diabled and next is enabled
		if (canFlipToNextPage && !canFinish)
			getShell().setDefaultButton(nextButton);
		else
			getShell().setDefaultButton(finishButton);
	}

	/**
	 * Update the message line with the page's description.
	 * <p>
	 * A discription is shown only if there is no message or error message.
	 * </p>
	 */
	private void updateDescriptionMessage() {
		pageDescription = currentPage.getDescription();
		if (pageMessage == null)
			setMessage(currentPage.getDescription());
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateMessage() {

		if (currentPage == null)
			return;

		pageMessage = currentPage.getMessage();
		if (pageMessage != null && currentPage instanceof IMessageProvider)
			pageMessageType = ((IMessageProvider) currentPage).getMessageType();
		else
			pageMessageType = IMessageProvider.NONE;
		if (pageMessage == null)
			setMessage(pageDescription);
		else
			setMessage(pageMessage, pageMessageType);
		setErrorMessage(currentPage.getErrorMessage());
	}

	/**
	 * Changes the shell size to the given size, ensuring that it is no larger than the display bounds.
	 * 
	 * @param width
	 *            the shell width
	 * @param height
	 *            the shell height
	 */
	private void setShellSize(int width, int height) {
		Rectangle size = getShell().getBounds();
		size.height = height;
		size.width = width;
		getShell().setBounds(getConstrainedShellBounds(size));
	}

	/**
	 * Computes the correct dialog size for the current page and resizes its shell if nessessary. Also causes the container to refresh its layout.
	 * 
	 * @param page
	 *            the wizard page to use to resize the dialog
	 * @since 2.0
	 */
	protected void updateSize(IWizardPage page) {
		if (page == null || page.getControl() == null)
			return;
		updateSizeForPage(page);
		pageContainerLayout.layoutPage(page.getControl());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardContainer2#updateSize()
	 */
	public void updateSize() {
		updateSize(currentPage);
	}

	/**
	 * Computes the correct dialog size for the given page and resizes its shell if nessessary.
	 * 
	 * @param page
	 *            the wizard page
	 */
	private void updateSizeForPage(IWizardPage page) {
		// ensure the page container is large enough
		Point delta = calculatePageSizeDelta(page);
		if (delta.x > 0 || delta.y > 0) {
			// increase the size of the shell
			Shell shell = getShell();
			Point shellSize = shell.getSize();
			setShellSize(shellSize.x + delta.x, shellSize.y + delta.y);
			constrainShellSize();
		}
	}

	/**
	 * Computes the correct dialog size for the given wizard and resizes its shell if nessessary.
	 * 
	 * @param sizingWizard
	 *            the wizard
	 */
	private void updateSizeForWizard(IWizard sizingWizard) {
		Point delta = new Point(0, 0);
		IWizardPage[] pages = sizingWizard.getPages();
		for (int i = 0; i < pages.length; i++) {
			// ensure the page container is large enough
			Point pageDelta = calculatePageSizeDelta(pages[i]);
			delta.x = Math.max(delta.x, pageDelta.x);
			delta.y = Math.max(delta.y, pageDelta.y);
		}
		if (delta.x > 0 || delta.y > 0) {
			// increase the size of the shell
			Shell shell = getShell();
			Point shellSize = shell.getSize();
			setShellSize(shellSize.x + delta.x, shellSize.y + delta.y);
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateTitleBar() {
		String s = null;
		if (currentPage != null)
			s = currentPage.getTitle();
		if (s == null)
			s = ""; //$NON-NLS-1$
		setTitle(s);
		if (currentPage != null)
			setTitleImage(currentPage.getImage());
		updateDescriptionMessage();
		updateMessage();
	}

	/*
	 * (non-Javadoc) Method declared on IWizardContainer.
	 */
	public void updateWindowTitle() {
		if (getShell() == null)
			// Not created yet
			return;
		String title = wizard.getWindowTitle();
		if (title == null)
			title = ""; //$NON-NLS-1$
		getShell().setText(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#getSelectedPage()
	 */
	public Object getSelectedPage() {
		return getCurrentPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialog.IPageChangeProvider#addPageChangedListener()
	 */
	public void addPageChangedListener(IPageChangedListener listener) {
		pageChangedListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialog.IPageChangeProvider#removePageChangedListener()
	 */
	public void removePageChangedListener(IPageChangedListener listener) {
		pageChangedListeners.remove(listener);
	}

	/**
	 * Notifies any selection changed listeners that the selected page has changed. Only listeners registered at the time this method is called are notified.
	 * 
	 * @param event
	 *            a selection changed event
	 * 
	 * @see IPageChangedListener#pageChanged
	 * 
	 * @since 3.1
	 */
	protected void firePageChanged(final PageChangedEvent event) {
		Object[] listeners = pageChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			final IPageChangedListener l = (IPageChangedListener) listeners[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.pageChanged(event);
				}
			});
		}
	}
}
