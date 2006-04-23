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
package org.springframework.ide.eclipse.core.ui.dialogs.input;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.springframework.ide.eclipse.core.ui.dialogs.AbstractMessageAndButtonDialog;

/**
 * Abstract class for dialogs asking the user for an input.
 * <p>
 * 
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractInputDialog extends
		AbstractMessageAndButtonDialog {

	/**
	 * Size of the text in lines.
	 */
	protected static final int TEXT_LINE_COUNT = 15;

	/**
	 * Labels for buttons in the button bar (localized strings).
	 */
	private String[] buttonLabels;

	/**
	 * The buttons. Parallels <code>buttonLabels</code>.
	 */
	private Button[] buttons;

	/**
	 * Index into <code>buttonLabels</code> of the default button.
	 */
	private int defaultButtonIndex;

	/**
	 * Dialog title (a localized string).
	 */
	private String title;

	/**
	 * Dialog title image.
	 */
	private Image titleImage;

	/**
	 * Create a message dialog. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 * <p>
	 * The labels of the buttons to appear in the button bar are supplied in
	 * this constructor as an array. The <code>open</code> method will return
	 * the index of the label in this array corresponding to the button that was
	 * pressed to close the dialog. If the dialog was dismissed without pressing
	 * a button (ESC, etc.) then -1 is returned. Note that the <code>open</code>
	 * method blocks.
	 * </p>
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogSubTitle
	 *            the dialog subTitle, or <code>null</code> if none
	 * @param dialogTitleImage
	 *            the dialog title image, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message
	 * @param dialogImageType
	 *            one of the following values:
	 *            <ul>
	 *            <li><code>AbstractInputDialog.NONE</code> for a dialog with
	 *            no image</li>
	 *            <li><code>AbstractInputDialog.ERROR</code> for a dialog
	 *            with an error image</li>
	 *            <li><code>AbstractInputDialog.INFORMATION</code> for a
	 *            dialog with an information image</li>
	 *            <li><code>AbstractInputDialog.QUESTION </code> for a dialog
	 *            with a question image</li>
	 *            <li><code>AbstractInputDialog.WARNING</code> for a dialog
	 *            with a warning image</li>
	 *            </ul>
	 * @param dialogButtonLabels
	 *            an array of labels for the buttons in the button bar
	 * @param defaultIndex
	 *            the index in the button label array of the default button
	 */
	public AbstractInputDialog(Shell parentShell, String dialogTitle,
			String dialogSubTitle, Image dialogTitleImage, String dialogMessage) {
		super(parentShell);
		this.title = dialogTitle;
		this.subTitle = dialogSubTitle;
		this.titleImage = dialogTitleImage;
		this.message = dialogMessage;
		this.buttonLabels = new String[] { IDialogConstants.OK_LABEL,
				IDialogConstants.CANCEL_LABEL };
		this.defaultButtonIndex = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
		if (titleImage != null)
			shell.setImage(titleImage);
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite,
	 *      int, java.lang.String, boolean)
	 */
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton,
				SWT.FLAT);
		// Be sure to set the focus if the custom area cannot so as not
		// to lose the defaultButton.
		if (defaultButton) {
			button.setFocus();
		}
		return button;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		buttons = new Button[buttonLabels.length];
		for (int i = 0; i < buttonLabels.length; i++) {
			String label = buttonLabels[i];
			Button button = createButton(parent, i, label,
					defaultButtonIndex == i);
			buttons[i] = button;
		}
	}

	/**
	 * Creates and returns the contents of an area of the dialog which appears
	 * below the message and above the button bar.
	 * <p>
	 * The default implementation of this framework method returns
	 * <code>null</code>. Subclasses may override.
	 * </p>
	 * 
	 * @param parent
	 *            parent composite to contain the custom area
	 * @return the custom area control, or <code>null</code>
	 */
	protected Control createCustomArea(Composite parent) {
		return null;
	}

	/**
	 * This implementation of the <code>Dialog</code> framework method creates
	 * and lays out a composite and calls <code>createMessageArea</code> and
	 * <code>createCustomArea</code> to populate it. Subclasses should
	 * override <code>createCustomArea</code> to add contents below the
	 * message.
	 */
	protected Control createDialogArea(Composite parent) {
		// create message area
		return createMessageArea(parent);
	}

	protected Control createMessageArea(Composite composite) {
		Section messageSection = getFormToolkit().createSection(composite,
				Section.EXPANDED | Section.TITLE_BAR);
		messageSection.setLayout(new GridLayout(1, false));

		Font font = new Font(composite.getDisplay(), new FontData(
				"Lucida Grande", 10, SWT.BOLD));
		messageSection.setFont(font);
		if (subTitle != null) {
			messageSection.setText(subTitle);
		}
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		messageSection.setData(data);

		GridLayout layout = new GridLayout(1, false);
		Composite sectionClient = getFormToolkit().createComposite(
				messageSection);
		sectionClient.setLayout(layout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		createInputPart(sectionClient);
		getFormToolkit().paintBordersFor(sectionClient);
		messageSection.setClient(sectionClient);
		return composite;
	}

	protected abstract void createInputPart(Composite composite);

	/**
	 * Gets a button in this dialog's button bar.
	 * 
	 * @param index
	 *            the index of the button in the dialog's button bar
	 * @return a button in the dialog's button bar
	 */
	protected Button getButton(int index) {
		return buttons[index];
	}

	/**
	 * An accessor for the labels to use on the buttons.
	 * 
	 * @return The button labels to used; never <code>null</code>.
	 */
	protected String[] getButtonLabels() {
		return buttonLabels;
	}

	/**
	 * An accessor for the index of the default button in the button array.
	 * 
	 * @return The default button index.
	 */
	protected int getDefaultButtonIndex() {
		return defaultButtonIndex;
	}

	/**
	 * Returns the minimum message area width in pixels This determines the
	 * minimum width of the dialog.
	 * <p>
	 * Subclasses may override.
	 * </p>
	 * 
	 * @return the minimum message area width (in pixels)
	 */
	protected int getMinimumMessageWidth() {
		return convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
	}

	/**
	 * Handle the shell close. Set the return code to <code>SWT.DEFAULT</code>
	 * as there has been no explicit close by the user.
	 * 
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent() {
		// Sets a return code of SWT.DEFAULT since none of the dialog buttons
		// were pressed to close the dialog.
		super.handleShellCloseEvent();
		setReturnCode(SWT.DEFAULT);
	}

	// Workaround. SWT does not seem to set rigth the default button if
	// there is not control with focus. Bug: 14668
	public int open() {
		create();
		Button b = getButton(defaultButtonIndex);
		b.setFocus();
		b.getShell().setDefaultButton(b);
		return super.open();
	}

	/**
	 * A mutator for the button labels.
	 * 
	 * @param buttonLabels
	 *            The button labels to use; must not be <code>null</code>.
	 */
	protected void setButtonLabels(String[] buttonLabels) {
		if (buttonLabels == null) {
			throw new NullPointerException(
					"The array of button labels cannot be null.");} //$NON-NLS-1$
		this.buttonLabels = buttonLabels;
	}

	/**
	 * A mutator for the array of buttons in the button bar.
	 * 
	 * @param buttons
	 *            The buttons in the button bar; must not be <code>null</code>.
	 */
	protected void setButtons(Button[] buttons) {
		if (buttons == null) {
			throw new NullPointerException(
					"The array of buttons cannot be null.");} //$NON-NLS-1$
		this.buttons = buttons;
	}

	protected void setDefaultButtonIndex(int defaultButtonIndex) {
		this.defaultButtonIndex = defaultButtonIndex;
	}
}
