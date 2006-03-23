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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A dialog that has a title area for displaying a title and an image as well as a common area for displaying a description, a message, or an error message.
 * <p>
 * This dialog class may be subclassed.
 */
public class TitleAreaFormsDialog extends org.springframework.ide.eclipse.core.ui.dialogs.Dialog {

	// Space between an image and a label
	private static final int H_GAP_IMAGE = 5;

	// Minimum dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 350;

	// Minimum dialog height (in dialog units)
	private static final int MIN_DIALOG_HEIGHT = 150;

	private Label titleLabel;

	private Label titleImage;

	private Label bottomFillerLabel;

	private Label leftFillerLabel;

	// private RGB titleAreaRGB;

	Color titleAreaColor;

	private String message = ""; //$NON-NLS-1$

	private String errorMessage;

	private Text messageLabel;

	private Composite workArea;

	private Label messageImageLabel;

	private Image messageImage;

	private Color normalMsgAreaBackground;

	private Color errorMsgAreaBackground;

	private Image errorMsgImage;

	private boolean showingError = false;

	private boolean titleImageLargest = true;

	private int messageLabelHeight;

	/**
	 * Instantiate a new title area dialog.
	 * 
	 * @param parentShell
	 *            the parent SWT shell
	 */
	public TitleAreaFormsDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * @see Dialog.createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		setFormToolkit(new FormToolkit(parent.getDisplay()));
		// initialize the dialog units
		initializeDialogUnits(parent);
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		parent.setLayoutData(data);
		// Now create a work area for the rest of the dialog
		workArea = getFormToolkit().createComposite(parent);
		GridLayout childLayout = new GridLayout();
		childLayout.marginHeight = 0;
		childLayout.marginWidth = 0;
		childLayout.verticalSpacing = 0;
		workArea.setLayout(childLayout);
		Control top = createTitleArea(parent);
		resetWorkAreaAttachments(top);
		workArea.setFont(JFaceResources.getDialogFont());
		workArea.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// initialize the dialog units
		initializeDialogUnits(workArea);
		// create the dialog area and button bar
		dialogArea = createDialogArea(workArea);
		buttonBar = createButtonBar(workArea);
		return parent;
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above the button bar).
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method creates and returns a new <code>Composite</code> with no margins and spacing. Subclasses should override.
	 * </p>
	 * 
	 * @param parent
	 *            The parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		// create the top level composite for the dialog area
		Composite composite =getFormToolkit().createComposite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		// Build the separator line
		Label titleBarSeparator = getFormToolkit().createLabel(composite,"", SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	/**
	 * Creates the dialog's title area.
	 * 
	 * @param parent
	 *            the SWT parent for the title area widgets
	 * @return Control with the highest x axis value.
	 */
	private Control createTitleArea(Composite parent) {
		// add a dispose listener
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (titleAreaColor != null)
					titleAreaColor.dispose();
			}
		});
		int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		// Dialog image @ right
		titleImage = getFormToolkit().createLabel(parent, "", SWT.CENTER);
		titleImage.setImage(JFaceResources.getImage(TitleAreaDialog.DLG_IMG_TITLE_BANNER));
		FormData imageData = new FormData();
		imageData.top = new FormAttachment(0, 0);
		// Note: do not use horizontalSpacing on the right as that would be a
		// regression from
		// the R2.x treeStyle where there was no margin on the right and images are
		// flush to the right
		// hand side. see reopened comments in 41172
		imageData.right = new FormAttachment(100, 0); // horizontalSpacing
		titleImage.setLayoutData(imageData);
		// Title label @ top, left
		titleLabel = getFormToolkit().createLabel(parent, "", SWT.LEFT);
		// JFaceColors.setColors(titleLabel, foreground, background);
		titleLabel.setFont(JFaceResources.getBannerFont());
		titleLabel.setText(" ");//$NON-NLS-1$
		titleLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		FormData titleData = new FormData();
		titleData.top = new FormAttachment(0, verticalSpacing);
		titleData.right = new FormAttachment(titleImage);
		titleLabel.setLayoutData(titleData);
		// Message image @ bottom, left
		messageImageLabel = getFormToolkit().createLabel(parent, "", SWT.CENTER);
		messageImageLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		// Message label @ bottom, center
		messageLabel = getFormToolkit().createText(parent, "", SWT.WRAP | SWT.READ_ONLY);
		messageLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		messageLabel.setText(" \n "); // two lines//$NON-NLS-1$
		messageLabel.setFont(JFaceResources.getDialogFont());
		messageLabelHeight = messageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		// Filler labels
		leftFillerLabel = getFormToolkit().createLabel(parent, "", SWT.CENTER);
		leftFillerLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		bottomFillerLabel = getFormToolkit().createLabel(parent, "", SWT.CENTER);
		bottomFillerLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
		determineTitleImageLargest();
		parent.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		if (titleImageLargest)
			return titleImage;
		return messageLabel;
	}

	/**
	 * Determine if the title image is larger than the title message and message area. This is used for layout decisions.
	 */
	private void determineTitleImageLargest() {
		int titleY = titleImage.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		int labelY = titleLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		labelY += verticalSpacing;
		labelY += messageLabelHeight;
		labelY += verticalSpacing;
		titleImageLargest = titleY > labelY;
	}

	/**
	 * Set the layout values for the messageLabel, messageImageLabel and fillerLabel for the case where there is a normal message.
	 * 
	 * @param verticalSpacing
	 *            int The spacing between widgets on the vertical axis.
	 * @param horizontalSpacing
	 *            int The spacing between widgets on the horizontal axis.
	 */
	private void setLayoutsForNormalMessage(int verticalSpacing, int horizontalSpacing) {
		FormData messageImageData = new FormData();
		messageImageData.top = new FormAttachment(titleLabel, verticalSpacing);
		messageImageData.left = new FormAttachment(0, H_GAP_IMAGE);
		messageImageLabel.setLayoutData(messageImageData);
		FormData messageLabelData = new FormData();
		messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
		messageLabelData.right = new FormAttachment(titleImage);
		messageLabelData.left = new FormAttachment(messageImageLabel, horizontalSpacing);
		messageLabelData.height = messageLabelHeight;
		if (titleImageLargest)
			messageLabelData.bottom = new FormAttachment(titleImage, 0, SWT.BOTTOM);
		messageLabel.setLayoutData(messageLabelData);
		FormData fillerData = new FormData();
		fillerData.left = new FormAttachment(0, horizontalSpacing);
		fillerData.top = new FormAttachment(messageImageLabel, 0);
		fillerData.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
		bottomFillerLabel.setLayoutData(fillerData);
		FormData data = new FormData();
		data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
		data.left = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
		data.right = new FormAttachment(messageImageLabel, 0);
		leftFillerLabel.setLayoutData(data);
	}

	/**
	 * The <code>TitleAreaDialog</code> implementation of this <code>Window</code> methods returns an initial size which is at least some reasonable minimum.
	 * 
	 * @return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x), Math.max(convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT), shellSize.y));
	}

	/**
	 * Retained for backward compatibility.
	 * 
	 * Returns the title area composite. There is no composite in this implementation so the shell is returned.
	 * 
	 * @return Composite
	 * @deprecated
	 */
	protected Composite getTitleArea() {
		return getShell();
	}

	/**
	 * Returns the title image label.
	 * 
	 * @return the title image label
	 */
	protected Label getTitleImageLabel() {
		return titleImage;
	}

	/**
	 * Display the given error message. The currently displayed message is saved and will be redisplayed when the error message is set to <code>null</code>.
	 * 
	 * @param newErrorMessage
	 *            the newErrorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String newErrorMessage) {
		// Any change?
		if (errorMessage == null ? newErrorMessage == null : errorMessage.equals(newErrorMessage))
			return;
		errorMessage = newErrorMessage;
		if (errorMessage == null) {
			if (showingError) {
				// we were previously showing an error
				showingError = false;
				setMessageBackgrounds(false);
			}
			// show the message
			// avoid calling setMessage in case it is overridden to call
			// setErrorMessage,
			// which would result in a recursive infinite loop
			if (message == null) // this should probably never happen since
				// setMessage does this conversion....
				message = ""; //$NON-NLS-1$
			updateMessage(message);
			messageImageLabel.setImage(messageImage);
			setImageLabelVisible(messageImage != null);
		} else {
			// Add in a space for layout purposes but do not
			// change the instance variable
			String displayedErrorMessage = " " + errorMessage; //$NON-NLS-1$
			updateMessage(displayedErrorMessage);
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;
				// lazy initialize the error background color and image
				if (errorMsgAreaBackground == null) {
					errorMsgAreaBackground = JFaceColors.getBannerBackground(messageLabel.getDisplay());
					errorMsgImage = JFaceResources.getImage(TitleAreaDialog.DLG_IMG_TITLE_ERROR);
				}
				// show the error
				normalMsgAreaBackground = messageLabel.getBackground();
				setMessageBackgrounds(true);
				messageImageLabel.setImage(errorMsgImage);
				setImageLabelVisible(true);
			}
		}
		layoutForNewMessage();
	}

	/**
	 * Re-layout the labels for the new message.
	 */
	private void layoutForNewMessage() {
		int verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		int horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		// If there are no images then layout as normal
		if (errorMessage == null && messageImage == null) {
			setImageLabelVisible(false);
			setLayoutsForNormalMessage(verticalSpacing, horizontalSpacing);
		} else {
			messageImageLabel.setVisible(true);
			bottomFillerLabel.setVisible(true);
			leftFillerLabel.setVisible(true);
			/**
			 * Note that we do not use horizontalSpacing here as when the background of the messages changes there will be gaps between the icon label and the message that are the background color of the shell. We add a leading space elsewhere to compendate for this.
			 */
			FormData data = new FormData();
			data.left = new FormAttachment(0, H_GAP_IMAGE);
			data.top = new FormAttachment(titleLabel, verticalSpacing);
			messageImageLabel.setLayoutData(data);
			data = new FormData();
			data.top = new FormAttachment(messageImageLabel, 0);
			data.left = new FormAttachment(0, 0);
			data.bottom = new FormAttachment(messageLabel, 0, SWT.BOTTOM);
			data.right = new FormAttachment(messageImageLabel, 0, SWT.RIGHT);
			bottomFillerLabel.setLayoutData(data);
			data = new FormData();
			data.top = new FormAttachment(messageImageLabel, 0, SWT.TOP);
			data.left = new FormAttachment(0, 0);
			data.bottom = new FormAttachment(messageImageLabel, 0, SWT.BOTTOM);
			data.right = new FormAttachment(messageImageLabel, 0);
			leftFillerLabel.setLayoutData(data);
			FormData messageLabelData = new FormData();
			messageLabelData.top = new FormAttachment(titleLabel, verticalSpacing);
			messageLabelData.right = new FormAttachment(titleImage);
			messageLabelData.left = new FormAttachment(messageImageLabel, 0);
			messageLabelData.height = messageLabelHeight;
			if (titleImageLargest)
				messageLabelData.bottom = new FormAttachment(titleImage, 0, SWT.BOTTOM);
			messageLabel.setLayoutData(messageLabelData);
		}
		// Do not layout before the dialog area has been created
		// to avoid incomplete calculations.
		if (dialogArea != null)
			getShell().layout(true);
	}

	/**
	 * Set the message text. If the message line currently displays an error, the message is saved and will be redisplayed when the error message is set to <code>null</code>.
	 * <p>
	 * Shortcut for <code>setMessage(newMessage, IMessageProvider.NONE)</code>
	 * </p>
	 * This method should be called after the dialog has been opened as it updates the message label immediately.
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 */
	public void setMessage(String newMessage) {
		setMessage(newMessage, IMessageProvider.NONE);
	}

	/**
	 * Sets the message for this dialog with an indication of what type of message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>, <code>INFORMATION</code>,<code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 * <p>
	 * Note that for backward compatibility, a message of type <code>ERROR</code> is different than an error message (set using <code>setErrorMessage</code>). An error message overrides the current message until the error message is cleared. This method replaces the current message and does not affect the error message.
	 * </p>
	 * 
	 * @param newMessage
	 *            the message, or <code>null</code> to clear the message
	 * @param newType
	 *            the message type
	 * @since 2.0
	 */
	public void setMessage(String newMessage, int newType) {
		Image newImage = null;
		if (newMessage != null) {
			switch (newType) {
			case IMessageProvider.NONE:
				break;
			case IMessageProvider.INFORMATION:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
				break;
			case IMessageProvider.WARNING:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
				break;
			case IMessageProvider.ERROR:
				newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
				break;
			}
		}
		showMessage(newMessage, newImage);
	}

	/**
	 * Show the new message and image.
	 * 
	 * @param newMessage
	 * @param newImage
	 */
	private void showMessage(String newMessage, Image newImage) {
		// Any change?
		if (message.equals(newMessage) && messageImage == newImage)
			return;
		message = newMessage;
		if (message == null)
			message = "";//$NON-NLS-1$
		// Message string to be shown - if there is an image then add in
		// a space to the message for layout purposes
		String shownMessage = (newImage == null) ? message : " " + message; //$NON-NLS-1$  
		messageImage = newImage;
		if (!showingError) {
			// we are not showing an error
			updateMessage(shownMessage);
			messageImageLabel.setImage(messageImage);
			setImageLabelVisible(messageImage != null);
			layoutForNewMessage();
		}
	}

	/**
	 * Update the contents of the messageLabel.
	 * 
	 * @param newMessage
	 *            the message to use
	 */
	private void updateMessage(String newMessage) {
		messageLabel.setText(newMessage);
	}

	/**
	 * Sets the title to be shown in the title area of this dialog.
	 * 
	 * @param newTitle
	 *            the title show
	 */
	public void setTitle(String newTitle) {
		if (titleLabel == null)
			return;
		String title = newTitle;
		if (title == null)
			title = "";//$NON-NLS-1$
		titleLabel.setText(title);
	}

	/**
	 * Sets the title image to be shown in the title area of this dialog.
	 * 
	 * @param newTitleImage
	 *            the title image show
	 */
	public void setTitleImage(Image newTitleImage) {
		titleImage.setImage(newTitleImage);
		titleImage.setVisible(newTitleImage != null);
		if (newTitleImage != null) {
			determineTitleImageLargest();
			Control top;
			if (titleImageLargest)
				top = titleImage;
			else
				top = messageLabel;
			resetWorkAreaAttachments(top);
		}
	}

	/**
	 * Make the label used for displaying error images visible depending on boolean.
	 * 
	 * @param visible.
	 *            If <code>true</code> make the image visible, if not then make it not visible.
	 */
	private void setImageLabelVisible(boolean visible) {
		messageImageLabel.setVisible(visible);
		bottomFillerLabel.setVisible(visible);
		leftFillerLabel.setVisible(visible);
	}

	/**
	 * Set the message backgrounds to be the error or normal color depending on whether or not showingError is true.
	 * 
	 * @param showingError
	 *            If <code>true</code> use a different Color to indicate the error.
	 */
	private void setMessageBackgrounds(boolean showingError) {
		Color color;
		if (showingError)
			color = errorMsgAreaBackground;
		else
			color = normalMsgAreaBackground;
		messageLabel.setBackground(color);
		messageImageLabel.setBackground(color);
		bottomFillerLabel.setBackground(color);
		leftFillerLabel.setBackground(color);
	}

	/**
	 * Reset the attachment of the workArea to now attach to top as the top control.
	 * 
	 * @param top
	 */
	private void resetWorkAreaAttachments(Control top) {
		FormData childData = new FormData();
		childData.top = new FormAttachment(top);
		childData.right = new FormAttachment(100, 0);
		childData.left = new FormAttachment(0, 0);
		childData.bottom = new FormAttachment(100, 0);
		workArea.setLayoutData(childData);
	}
}
