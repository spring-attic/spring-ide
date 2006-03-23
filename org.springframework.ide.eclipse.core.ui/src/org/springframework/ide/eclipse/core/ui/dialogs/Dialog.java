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
package org.springframework.ide.eclipse.core.ui.dialogs;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A dialog is a specialized window used for narrow-focused communication with the user.
 * <p>
 * Dialogs are usually modal. Consequently, it is generally bad practice to open a dialog without a parent. A modal dialog without a parent is not prevented from disappearing behind the application's other windows, making it very confusing for the user.
 * </p>
 * <p>
 * If there is more than one modal dialog is open the second one should be parented off of the shell of the first one otherwise it is possible that the OS will give focus to the first dialog potentially blocking the UI.
 * </p>
 * This implementation uses the Forms toolkit to build the UI.
 * @author Pierre-Antoine Grégoire
 */
public abstract class Dialog extends Window {

	/**
	 * The dialog area; <code>null</code> until dialog is layed out.
	 */
	protected Control dialogArea;

	/**
	 * The button bar; <code>null</code> until dialog is layed out.
	 */
	public Control buttonBar;

	/**
	 * Collection of buttons created by the <code>createButton</code> method.
	 */
	private HashMap buttons = new HashMap();

	/**
	 * Font metrics to use for determining pixel sizes.
	 */
	private FontMetrics fontMetrics;

	private FormToolkit formToolkit;

	/**
	 * Creates a dialog instance. Note that the window will have no visual representation (no widgets) until it is told to open. By default, <code>open</code> blocks for dialogs.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 */
	protected Dialog(Shell parentShell) {
		this(new SameShellProvider(parentShell));
	}

	/**
	 * Creates a dialog with the given parent.
	 * 
	 * @param parentShell
	 *            object that returns the current parent shell
	 * 
	 * @since 3.1
	 */
	protected Dialog(IShellProvider parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());
		setBlockOnOpen(true);
	}

	/**
	 * Notifies that this dialog's button with the given id has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method calls <code>okPressed</code> if the ok button is the pressed, and <code>cancelPressed</code> if the cancel button is the pressed. All other button presses are ignored. Subclasses may override to handle other buttons, but should call <code>super.buttonPressed</code> if the default handling of the ok and cancel buttons is desired.
	 * </p>
	 * 
	 * @param buttonId
	 *            the id of the button that was pressed (see <code>IDialogConstants.*_ID</code> constants)
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId)
			okPressed();
		else if (IDialogConstants.CANCEL_ID == buttonId)
			cancelPressed();
	}

	/**
	 * Notifies that the cancel button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets this dialog's return code to <code>Window.CANCEL</code> and closes the dialog. Subclasses may override if desired.
	 * </p>
	 */
	protected void cancelPressed() {
		setReturnCode(CANCEL);
		close();
	}

	/**
	 * Returns the number of pixels corresponding to the height of the given number of characters.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @param chars
	 *            the number of characters
	 * @return the number of pixels
	 */
	protected int convertHeightInCharsToPixels(int chars) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null)
			return 0;
		return org.eclipse.jface.dialogs.Dialog.convertHeightInCharsToPixels(fontMetrics, chars);
	}

	/**
	 * Returns the number of pixels corresponding to the given number of horizontal dialog units.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @param dlus
	 *            the number of horizontal dialog units
	 * @return the number of pixels
	 */
	protected int convertHorizontalDLUsToPixels(int dlus) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null)
			return 0;
		return org.eclipse.jface.dialogs.Dialog.convertHorizontalDLUsToPixels(fontMetrics, dlus);
	}

	/**
	 * Returns the number of pixels corresponding to the given number of vertical dialog units.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @param dlus
	 *            the number of vertical dialog units
	 * @return the number of pixels
	 */
	protected int convertVerticalDLUsToPixels(int dlus) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null)
			return 0;
		return org.eclipse.jface.dialogs.Dialog.convertVerticalDLUsToPixels(fontMetrics, dlus);
	}

	/**
	 * Returns the number of pixels corresponding to the width of the given number of characters.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @param chars
	 *            the number of characters
	 * @return the number of pixels
	 */
	protected int convertWidthInCharsToPixels(int chars) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null)
			return 0;
		return org.eclipse.jface.dialogs.Dialog.convertWidthInCharsToPixels(fontMetrics, chars);
	}

	/**
	 * Creates a new button with the given id.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates a standard push button, registers it for selection events including button presses, and registers default buttons with its shell. The button id is stored as the button's client data. If the button id is <code>IDialogConstants.CANCEL_ID</code>, the new button will be accessible from <code>getCancelButton()</code>. If the button id is <code>IDialogConstants.OK_ID</code>, the new button will be accesible from <code>getOKButton()</code>. Note that the parent's layout is assumed to be a <code>GridLayout</code> and the number of columns in this layout is incremented. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see <code>IDialogConstants.*_ID</code> constants for standard dialog button ids)
	 * @param label
	 *            the label from the button
	 * @param defaultButton
	 *            <code>true</code> if the button is to be the default button, and <code>false</code> otherwise
	 * 
	 * @return the new button
	 * 
	 * @see #getCancelButton
	 * @see #getOKButton()
	 */
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton, int style) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = formToolkit.createButton(parent, "", style);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(((Integer) event.widget.getData()).intValue());
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		buttons.put(new Integer(id), button);
		setButtonLayoutData(button);
		return button;
	}

	/**
	 * Creates and returns the contents of this dialog's button bar.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method lays out a button bar and calls the <code>createButtonsForButtonBar</code> framework method to populate it. Subclasses may override.
	 * </p>
	 * <p>
	 * The returned control's layout data must be an instance of <code>GridData</code>.
	 * </p>
	 * 
	 * @param parent
	 *            the parent composite to contain the button bar
	 * @return the button bar control
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = getFormToolkit().createComposite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		composite.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);
		return composite;
	}

	/**
	 * Adds buttons to this dialog's button bar.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method adds standard ok and cancel buttons using the <code>createButton</code> framework method. These standard buttons will be accessible from <code>getCancelButton</code>, and <code>getOKButton</code>. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true, SWT.FLAT);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false, SWT.FLAT);
	}

	/*
	 * @see Window.initializeBounds()
	 */
	protected void initializeBounds() {
		String platform = SWT.getPlatform();
		if ("carbon".equals(platform)) { //$NON-NLS-1$
			// On Mac OS X the default button must be the right-most button
			Shell shell = getShell();
			if (shell != null) {
				Button defaultButton = shell.getDefaultButton();
				if (defaultButton != null && isContained(buttonBar, defaultButton))
					defaultButton.moveBelow(null);
			}
		}
		super.initializeBounds();
	}

	/**
	 * Returns true if the given Control is a direct or indirect child of container.
	 * 
	 * @param container
	 *            the potential parent
	 * @param control
	 * @return boolean <code>true</code> if control is a child of container
	 */
	private boolean isContained(Control container, Control control) {
		Composite parent;
		while ((parent = control.getParent()) != null) {
			if (parent == container)
				return true;
			control = parent;
		}
		return false;
	}

	/**
	 * The <code>Dialog</code> implementation of this <code>Window</code> method creates and lays out the top level composite for the dialog, and determines the appropriate horizontal and vertical dialog units based on the font size. It then calls the <code>createDialogArea</code> and <code>createButtonBar</code> methods to create the dialog area and button bar, respectively. Overriding <code>createDialogArea</code> and <code>createButtonBar</code> are recommended rather than overriding this method.
	 */
	protected Control createContents(Composite parent) {
		formToolkit = new FormToolkit(parent.getDisplay());
		// create the top level composite for the dialog
		Composite composite = formToolkit.createComposite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
		// initialize the dialog units
		
		// create the dialog area and button bar
		dialogArea = createDialogArea(composite);
		buttonBar = createButtonBar(composite);
		initializeDialogUnits(composite);
		return composite;
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above the button bar).
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates and returns a new <code>Composite</code> with standard margins and spacing.
	 * </p>
	 * <p>
	 * The returned control's layout data must be an instance of <code>GridData</code>. This method must not modify the parent's layout.
	 * </p>
	 * <p>
	 * Subclasses must override this method but may call <code>super</code> as in the following example:
	 * </p>
	 * 
	 * <pre>
	 * Composite composite = (Composite) super.createDialogArea(parent);
	 * //add controls to composite as necessary
	 * return composite;
	 * </pre>
	 * 
	 * @param parent
	 *            the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {

		// create a composite with standard margins and spacing
		Composite composite = formToolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// org.eclipse.jface.dialogs.Dialog.applyDialogFont(composite);
		return composite;
	}

	/**
	 * Returns the button created by the method <code>createButton</code> for the specified ID as defined on <code>IDialogConstants</code>. If <code>createButton</code> was never called with this ID, or if <code>createButton</code> is overridden, this method will return <code>null</code>.
	 * 
	 * @param id
	 *            the id of the button to look for
	 * 
	 * @return the button for the ID or <code>null</code>
	 * 
	 * @see #createButton(Composite, int, String, boolean)
	 * @since 2.0
	 */
	protected Button getButton(int id) {
		return (Button) buttons.get(new Integer(id));
	}

	/**
	 * Returns the button bar control.
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @return the button bar, or <code>null</code> if the button bar has not been created yet
	 */
	protected Control getButtonBar() {
		return buttonBar;
	}

	/**
	 * Returns the button created when <code>createButton</code> is called with an ID of <code>IDialogConstants.CANCEL_ID</code>. If <code>createButton</code> was never called with this parameter, or if <code>createButton</code> is overridden, <code>getCancelButton</code> will return <code>null</code>.
	 * 
	 * @return the cancel button or <code>null</code>
	 * 
	 * @see #createButton(Composite, int, String, boolean)
	 * @since 2.0
	 * @deprecated Use <code>getButton(IDialogConstants.CANCEL_ID)</code> instead. This method will be removed soon.
	 */
	protected Button getCancelButton() {
		return getButton(IDialogConstants.CANCEL_ID);
	}

	/**
	 * Returns the dialog area control.
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 * 
	 * @return the dialog area, or <code>null</code> if the dialog area has not been created yet
	 */
	protected Control getDialogArea() {
		return dialogArea;
	}

	/**
	 * Returns the button created when <code>createButton</code> is called with an ID of <code>IDialogConstants.OK_ID</code>. If <code>createButton</code> was never called with this parameter, or if <code>createButton</code> is overridden, <code>getOKButton</code> will return <code>null</code>.
	 * 
	 * @return the OK button or <code>null</code>
	 * 
	 * @see #createButton(Composite, int, String, boolean)
	 * @since 2.0
	 * @deprecated Use <code>getButton(IDialogConstants.OK_ID)</code> instead. This method will be removed soon.
	 */
	protected Button getOKButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	/**
	 * Initializes the computation of horizontal and vertical dialog units based on the size of current font.
	 * <p>
	 * This method must be called before any of the dialog unit based conversion methods are called.
	 * </p>
	 * 
	 * @param control
	 *            a control from which to obtain the current font
	 */
	protected void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets this dialog's return code to <code>Window.OK</code> and closes the dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed() {
		setReturnCode(OK);
		close();
	}

	/**
	 * Set the layout data of the button to a GridData with appropriate heights and widths.
	 * 
	 * @param button
	 */
	protected void setButtonLayoutData(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}

	/**
	 * Set the layout data of the button to a FormData with appropriate heights and widths.
	 * 
	 * @param button
	 */
	protected void setButtonLayoutFormData(Button button) {
		FormData data = new FormData();
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.width = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		boolean returnValue = super.close();
		if (returnValue) {
			buttons = new HashMap();
			buttonBar = null;
			dialogArea = null;
		}
		return returnValue;
	}

	/**
	 * Return whether or not the dialog font is currently the same as the default font.
	 * 
	 * @return boolean if the two are the same
	 */
	protected static boolean dialogFontIsDefault() {
		FontData[] dialogFontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT);
		FontData[] defaultFontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DEFAULT_FONT);
		return Arrays.equals(dialogFontData, defaultFontData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		org.eclipse.jface.dialogs.Dialog.applyDialogFont(buttonBar);
	}

	protected FormToolkit getFormToolkit() {
		return formToolkit;
	}

	protected void setFormToolkit(FormToolkit formToolkit) {
		this.formToolkit = formToolkit;
	}
}
