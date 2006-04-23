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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * The AbstractIconAndMessageDialog is the abstract superclass of dialogs that have an icon and a message as the first two widgets.<br>
 * In this dialog the icon and message are direct children of the shell in order that they can be read by accessibility tools more easily.
 * @author Pierre-Antoine Grégoire
 */
public abstract class AbstractMessageAndButtonDialog extends Dialog {
	private Image image;

	/**
	 * Return the label for the image.
	 */
	protected Label imageLabel;

	/**
	 * Message (a localized string).
	 */
	protected String message;

	/**
	 * Message label is the label the message is shown on.
	 */
	protected Label messageLabel;

	/**
	 * Message (a localized string).
	 */
	protected String subTitle;

	/**
	 * Constructor for AbstractIconAndMessageDialog.
	 * 
	 * @param parentShell
	 *            the parent shell, or <code>null</code> to create a top-level shell
	 */
	public AbstractMessageAndButtonDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * @see Dialog.createButtonBar()
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = getFormToolkit().createComposite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);

		return composite;
	}

	/*
	 * @see Dialog.createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		// initialize the dialog units
		initializeDialogUnits(parent);
		setFormToolkit(new FormToolkit(parent.getDisplay()));
		// create the top level composite for the dialog
		Form form = getFormToolkit().createForm(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN) * 3 / 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING) * 2;
		layout.makeColumnsEqualWidth = false;
		form.getBody().setLayout(layout);
		form.getBody().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		createDialogAndButtonArea(form.getBody());
		return parent;
	}

	/**
	 * Create the dialog area and the button bar for the receiver.
	 * 
	 * @param parent
	 */
	protected void createDialogAndButtonArea(Composite parent) {
		// create the dialog area and button bar
		dialogArea = createDialogArea(parent);
		buttonBar = createButtonBar(parent);
		// Apply to the parent so that the message gets it too.
	}

	/**
	 * Create the area the message will be shown in.
	 * 
	 * @param composite
	 *            The composite to parent from.
	 * @return Control
	 */
	protected Control createMessageArea(Composite parent) {

		// create composite
		Section messageSection = getFormToolkit().createSection(parent, Section.EXPANDED | Section.TITLE_BAR);
		messageSection.setLayout(new GridLayout(2, false));
		if (subTitle != null) {
			Font font = new Font(parent.getDisplay(), new FontData("Lucida Grande", 12, SWT.BOLD));
			messageSection.setFont(font);
			messageSection.setText(subTitle);
		}
		GridLayout layout = new GridLayout(2, false);
		Composite sectionClient = getFormToolkit().createComposite(messageSection);
		sectionClient.setLayout(layout);
		sectionClient.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		// create image
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(sectionClient, SWT.BORDER | SWT.FLAT);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));
		}
		// create message
		if (message != null) {
			messageLabel = new Label(sectionClient, getMessageTextStyle());
			messageLabel.setText(message);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			messageLabel.setLayoutData(data);
			messageLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		}
		getFormToolkit().paintBordersFor(sectionClient);
		messageSection.setClient(sectionClient);
		return parent;
	}

	protected Image getImage() {
		return this.image;
	}

	/**
	 * Returns the treeStyle for the message label.
	 * 
	 * @return the treeStyle for the message label
	 * 
	 * @since 3.0
	 */
	protected int getMessageTextStyle() {
		return SWT.READ_ONLY | SWT.WRAP;
	}

	protected void setImage(Image image) {
		this.image = image;
	}

}