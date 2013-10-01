/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;

/**
 * The following code is copied from:
 * 
 * org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne
 * 
 * as these were originally private methods, yet perform general working sets
 * handling from a structured selection and package explorer part that can be
 * reused for other project wizards.
 * 
 * Visibility of the methods has been increased from private to protected or
 * public. Otherwise, the code remains unchanged.
 * 
 */
public class ProjectNameLocationArea extends WizardPageArea {

	private final NameGroup nameGroup;

	private final LocationGroup locationGroup;

	private final Validator validator;

	private final Shell shell;

	private final NewSpringProjectWizardModel model;

	public ProjectNameLocationArea(NewSpringProjectWizardModel model, IWizardPageStatusHandler statusHandler,
			Shell shell) {
		super(statusHandler);
		this.shell = shell;
		this.model = model;
		validator = new Validator();
		nameGroup = new NameGroup();
		locationGroup = new LocationGroup();

	}

	@Override
	public Control createArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

		nameGroup.createControl(composite);
		locationGroup.createControl(composite);

		// establish connections
		nameGroup.addObserver(locationGroup);

		// initialize all elements
		nameGroup.notifyObservers();

		nameGroup.addObserver(validator);
		locationGroup.addObserver(validator);

		// Fire the first event for validation
		setProjectName(""); //$NON-NLS-1$

		return composite;
	}

	/**
	 * Gets a project name for the new project.
	 * 
	 * @return the new project resource handle
	 */
	protected String getProjectName() {
		return nameGroup.getName();
	}

	/**
	 * Sets the name of the new project
	 * 
	 * @param name the new name
	 */
	protected void setProjectName(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}

		nameGroup.setName(name);
	}

	/**
	 * Request a project name. Fires an event whenever the text field is
	 * changed, regardless of its content.
	 */
	protected final class NameGroup extends Observable implements IDialogFieldListener {

		protected final StringDialogField fNameField;

		public NameGroup() {
			// text field for project name
			fNameField = new StringDialogField();
			fNameField.setLabelText(NewWizardMessages.NewJavaProjectWizardPageOne_NameGroup_label_text);
			fNameField.setDialogFieldListener(this);
		}

		public Control createControl(Composite composite) {
			Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(new GridLayout(2, false));

			fNameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fNameField.getTextControl(null));

			GridDataFactory.fillDefaults().grab(true, false).applyTo(nameComposite);
			return nameComposite;
		}

		protected void fireEvent() {
			setChanged();
			notifyObservers();
		}

		public String getName() {
			return fNameField.getText().trim();
		}

		public void postSetFocus() {
			fNameField.postSetFocusOnDialogField(shell.getDisplay());
		}

		public void setName(String name) {
			fNameField.setText(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener
		 * #dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.
		 * DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
			fireEvent();
		}
	}

	/**
	 * Request a location. Fires an event whenever the checkbox or the location
	 * field is changed, regardless of whether the change originates from the
	 * user or has been invoked programmatically.
	 */
	private final class LocationGroup extends Observable implements Observer, IStringButtonAdapter,
			IDialogFieldListener {

		protected final SelectionButtonDialogField fUseDefaults;

		protected final StringButtonDialogField fLocation;

		private String fPreviousExternalLocation;

		private static final String DIALOGSTORE_LAST_EXTERNAL_LOC = JavaUI.ID_PLUGIN + ".last.external.project"; //$NON-NLS-1$

		public LocationGroup() {
			fUseDefaults = new SelectionButtonDialogField(SWT.CHECK);
			fUseDefaults.setDialogFieldListener(this);
			fUseDefaults.setLabelText(NewWizardMessages.NewJavaProjectWizardPageOne_LocationGroup_location_desc);

			fLocation = new StringButtonDialogField(this);
			fLocation.setDialogFieldListener(this);
			fLocation.setLabelText(NewWizardMessages.NewJavaProjectWizardPageOne_LocationGroup_locationLabel_desc);
			fLocation.setButtonLabel(NewWizardMessages.NewJavaProjectWizardPageOne_LocationGroup_browseButton_desc);

			fUseDefaults.setSelection(true);

			fPreviousExternalLocation = ""; //$NON-NLS-1$
		}

		public Control createControl(Composite composite) {
			final int numColumns = 4;

			final Composite locationComposite = new Composite(composite, SWT.NONE);
			locationComposite.setLayout(new GridLayout(numColumns, false));

			fUseDefaults.doFillIntoGrid(locationComposite, numColumns);
			fLocation.doFillIntoGrid(locationComposite, numColumns);
			LayoutUtil.setHorizontalGrabbing(fLocation.getTextControl(null));

			GridDataFactory.fillDefaults().grab(true, false).applyTo(locationComposite);

			return locationComposite;
		}

		protected void fireEvent() {
			setChanged();
			notifyObservers();
		}

		protected String getDefaultPath(String name) {
			final IPath path = Platform.getLocation().append(name);
			return path.toOSString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable,
		 * java.lang.Object)
		 */
		public void update(Observable o, Object arg) {
			if (isUseDefaultSelected()) {
				fLocation.setText(getDefaultPath(nameGroup.getName()));
			}
			fireEvent();
		}

		public IPath getLocation() {
			if (isUseDefaultSelected()) {
				return Platform.getLocation();
			}
			return Path.fromOSString(fLocation.getText().trim());
		}

		public boolean isUseDefaultSelected() {
			return fUseDefaults.isSelected();
		}

		public void setLocation(IPath path) {
			fUseDefaults.setSelection(path == null);
			if (path != null) {
				fLocation.setText(path.toOSString());
			}
			else {
				fLocation.setText(getDefaultPath(nameGroup.getName()));
			}
			fireEvent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter
		 * #
		 * changeControlPressed(org.eclipse.jdt.internal.ui.wizards.dialogfields
		 * .DialogField)
		 */
		public void changeControlPressed(DialogField field) {
			final DirectoryDialog dialog = new DirectoryDialog(shell);
			dialog.setMessage(NewWizardMessages.NewJavaProjectWizardPageOne_directory_message);
			String directoryName = fLocation.getText().trim();
			if (directoryName.length() == 0) {
				String prevLocation = JavaPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LAST_EXTERNAL_LOC);
				if (prevLocation != null) {
					directoryName = prevLocation;
				}
			}

			if (directoryName.length() > 0) {
				final File path = new File(directoryName);
				if (path.exists()) {
					dialog.setFilterPath(directoryName);
				}
			}
			final String selectedDirectory = dialog.open();
			if (selectedDirectory != null) {
				String oldDirectory = new Path(fLocation.getText().trim()).lastSegment();
				fLocation.setText(selectedDirectory);
				String lastSegment = new Path(selectedDirectory).lastSegment();
				if (lastSegment != null
						&& (nameGroup.getName().length() == 0 || nameGroup.getName().equals(oldDirectory))) {
					nameGroup.setName(lastSegment);
				}
				JavaPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LAST_EXTERNAL_LOC, selectedDirectory);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener
		 * #dialogFieldChanged(org.eclipse.jdt.internal.ui.wizards.dialogfields.
		 * DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
			if (field == fUseDefaults) {
				final boolean checked = fUseDefaults.isSelected();
				if (checked) {
					fPreviousExternalLocation = fLocation.getText();
					fLocation.setText(getDefaultPath(nameGroup.getName()));
					fLocation.setEnabled(false);
				}
				else {
					fLocation.setText(fPreviousExternalLocation);
					fLocation.setEnabled(true);
				}
			}
			fireEvent();
		}
	}

	protected final class Validator implements Observer {

		public void update(Observable o, Object arg) {
			IStatus status = validateArea();
			notifyStatusChange(status);
		}

	}

	@Override
	protected IStatus validateArea() {

		// First time the area is validated, treat missing data as OK status as
		// not to show an error when the UI is first displayed.
		// Subsequent validations should treat missing data as ERROR.
		int missingDataStatus = IStatus.INFO;

		final String name = getProjectName();
		final String location = locationGroup.getLocation().toOSString();

		model.projectName.setValue(name);

		// Only set the location if default is not used. Otherwise set null
		if (locationGroup.isUseDefaultSelected()) {
			model.projectLocation.setValue(null);
		}
		else {
			model.projectLocation.setValue(location);
		}

		final IWorkspace workspace = JavaPlugin.getWorkspace();

		// check whether the project name field is empty
		if (name.length() == 0) {
			setAreaComplete(false);
			return createStatus(NewWizardMessages.NewJavaProjectWizardPageOne_Message_enterProjectName,
					missingDataStatus);
		}

		// check whether the project name is valid
		final IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setAreaComplete(false);
			return createStatus(nameStatus.getMessage(), IStatus.ERROR);
		}

		// check whether project already exists
		final IProject handle = workspace.getRoot().getProject(name);
		if (handle.exists()) {
			setAreaComplete(false);
			return createStatus(NewWizardMessages.NewJavaProjectWizardPageOne_Message_projectAlreadyExists,
					IStatus.ERROR);
		}

		IPath projectLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(name);
		if (projectLocation.toFile().exists()) {
			try {
				// correct casing
				String canonicalPath = projectLocation.toFile().getCanonicalPath();
				projectLocation = new Path(canonicalPath);
			}
			catch (IOException e) {
				// Do not set as a wizard status, as it may sill be continue
				// with further checks
				WizardPlugin
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
								"I/O Error when correcting project path casing: " + e.getMessage(), e));

			}

			String existingName = projectLocation.lastSegment();
			if (!existingName.equals(nameGroup.getName())) {
				setAreaComplete(false);
				return createStatus(Messages.format(
						NewWizardMessages.NewJavaProjectWizardPageOne_Message_invalidProjectNameForWorkspaceRoot,
						BasicElementLabels.getResourceName(existingName)), IStatus.ERROR);
			}
		}

		// check whether location is empty
		if (location.length() == 0) {
			setAreaComplete(false);
			return createStatus(NewWizardMessages.NewJavaProjectWizardPageOne_Message_enterLocation, missingDataStatus);
		}

		// check whether the location is a syntactically correct path
		if (!Path.EMPTY.isValidPath(location)) {
			setAreaComplete(false);
			return createStatus(NewWizardMessages.NewJavaProjectWizardPageOne_Message_invalidDirectory, IStatus.ERROR);
		}

		IPath projectPath = null;
		if (!locationGroup.isUseDefaultSelected()) {
			projectPath = Path.fromOSString(location);
			if (!projectPath.toFile().exists()) {
				// check non-existing external location
				if (!canCreate(projectPath.toFile())) {

					setAreaComplete(false);
					return createStatus(
							NewWizardMessages.NewJavaProjectWizardPageOne_Message_cannotCreateAtExternalLocation,
							IStatus.ERROR);

				}
			}
		}

		// validate the location
		final IStatus locationStatus = workspace.validateProjectLocation(handle, projectPath);
		if (!locationStatus.isOK()) {
			setAreaComplete(false);
			return createStatus(locationStatus.getMessage(), IStatus.ERROR);
		}

		setAreaComplete(true);
		return Status.OK_STATUS;
	}

	private boolean canCreate(File file) {
		while (!file.exists()) {
			file = file.getParentFile();
			if (file == null) {
				return false;
			}
		}

		return file.canWrite();
	}
}
