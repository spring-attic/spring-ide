/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.ValidatorSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Single page dialog with status bar and ok cancel buttons capable of hosting 
 * {@link PageSection}s.
 * 
 * @author Kris De Volder
 */
public abstract class DialogWithSections extends TitleAreaDialog implements ValueListener<ValidationResult>, IPageWithSections {

	private String title;

	public DialogWithSections(String title, Shell shell) {
		super(shell);
		this.title = title;
		this.settings = getDialogSettings(this.getClass().getName());
	}
	
	public void create() {
		super.create();
		setTitle(title);
	}

	protected Control createDialogArea(Composite parent) {
		readSettings();
		Composite page = (Composite) super.createDialogArea(parent);
		
//		GridDataFactory.fillDefaults().grab(true,true).applyTo(parent);
//		Composite page = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 12;
        layout.marginWidth = 12;
        page.setLayout(layout);
        validator = new CompositeValidator();
        for (PageSection section : getSections()) {
			section.createContents(page);
			validator.addChild(section.getValidator());
		}
        validator.addListener(this);
        return page;
	}

	/**
	 * A delay used for posting status messages to the dialog area after a status update happens.
	 * This is to get rid of spurious message that only appear for a fraction of a second as
	 * internal some auto updating states in models are inconsistent. E.g. in new boot project wizard 
	 * when project name is entered it is temporarily inconsistent with default project location until
	 * that project location itself is update in response to the change event from the project name.
	 * If the project location validator runs before the location update, a spurious validation error
	 * temporarily results.
	 * 
	 * Note: this is a hacky solution. It would be better if the LiveExp framework solved this by
	 * tracking and scheduling refreshes based on the depedency graph. Thus it might guarantee
	 * that the validator never sees the inconsistent state because it is refreshed last.
	 */
	private static final long MESSAGE_DELAY = 250;

	private List<WizardPageSection> sections = null;
	private CompositeValidator validator;
	private UIJob updateJob;
	
	protected synchronized List<WizardPageSection> getSections() {
		if (sections==null) {
			sections = safeCreateSections();
		}
		return sections;
	}
	
	private List<WizardPageSection> safeCreateSections() {
		try {
			return createSections();
		} catch (CoreException e) {
			BootActivator.log(e);
			return Arrays.asList(
					new CommentSection(this, "Dialog couldn't be created because of an unexpected error:+\n"+ExceptionUtil.getMessage(e)+"\n\n"
							+ "Check the error log for details"),
					new ValidatorSection(Validator.alwaysError(ExceptionUtil.getMessage(e)), this)
			);
		}
	}

	/**
	 * This method should be implemented to generate the contents of the page.
	 */
	protected List<WizardPageSection> createSections() throws CoreException {
		//This default implementation is meant to be overridden
		return Arrays.asList(
				(WizardPageSection)new CommentSection(this, "Override DialogWithSections.createSections() to provide real content."),
				new ValidatorSection(Validator.alwaysError("Subclass must implement validation logic"), this)
		);
	}
	
	/**
	 * Convert ValidationResult into an Eclipse IStatus object.
	 */
	private IStatus toStatus(ValidationResult value) {
		if (value==null || value.isOk()) {
			return Status.OK_STATUS;
		} else {
			return new Status(value.status, BootActivator.PLUGIN_ID , value.msg);
		}
	}
	
	public void gotValue(LiveExpression<ValidationResult> exp, final ValidationResult status) {
		scheduleUpdateJob();
	}
	
	private synchronized void scheduleUpdateJob() {
		Shell shell = getShell();
		if (shell!=null) {
			if (this.updateJob==null) {
				this.updateJob = new UIJob("Update Wizard message") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						updateStatus(validator.getValue());
//						IStatus status = toStatus(validator.getValue());
//						updateStatus(status);
						return Status.OK_STATUS;
					}
				};
				updateJob.setSystem(true);
			}
			updateJob.schedule(MESSAGE_DELAY);
		}
	}
	
	private void updateStatus(ValidationResult status) {
		boolean enableOk = true;
		if (status==null || status.isOk()) {
			setMessage("", IMessageProvider.NONE);
		} else {
			setMessage(status.msg, status.getMessageProviderStatus());
			enableOk = status.status<IStatus.ERROR;
		}
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton!=null) {
			okButton.setEnabled(enableOk);
		}
	}

	public void dispose() {
		for (WizardPageSection s : sections) {
			s.dispose();
		}
	}	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/// Dialog settings related cruft. Most of this code copied from AbstractMavenDialog.
	///
	/// Therefore the code below is copyrighted as follows: 
	/*******************************************************************************
	 * Copyright (c) 2008-2010 Sonatype, Inc.
	 * All rights reserved. This program and the accompanying materials
	 * are made available under the terms of the Eclipse Public License v1.0
	 * which accompanies this distribution, and is available at
	 * http://www.eclipse.org/legal/epl-v10.html
	 *
	 * Contributors:
	 *      Sonatype, Inc. - initial API and implementation
	 *******************************************************************************/

	protected static final String KEY_WIDTH = "width"; //$NON-NLS-1$
	protected static final String KEY_HEIGHT = "height"; //$NON-NLS-1$
	private static final String KEY_X = "x"; //$NON-NLS-1$
	private static final String KEY_Y = "y"; //$NON-NLS-1$

	protected final IDialogSettings settings;
	private Point location;
	private Point size;
	
	/**
	 * Initializes itself from the dialog settings with the same state as at the previous invocation.
	 */
	protected void readSettings() {
		try {
			int x = settings.getInt(KEY_X);
			int y = settings.getInt(KEY_Y);
			location = new Point(x, y);
		} catch(NumberFormatException e) {
			location = null;
		}
		try {
			int width = settings.getInt(KEY_WIDTH);
			int height = settings.getInt(KEY_HEIGHT);
			size = new Point(width, height);

		} catch(NumberFormatException e) {
			size = null;
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeSettings() {
		Point location = getShell().getLocation();
		settings.put(KEY_X, location.x);
		settings.put(KEY_Y, location.y);

		Point size = getShell().getSize();
		settings.put(KEY_WIDTH, size.x);
		settings.put(KEY_HEIGHT, size.y);
	}

	private static IDialogSettings getDialogSettings(String settingsSection) {
		// activator is null inside WindowBuilder design editor
		BootActivator activator = BootActivator.getDefault();
		IDialogSettings pluginSettings = activator != null ? activator.getDialogSettings() : null;
		IDialogSettings settings = pluginSettings != null ? pluginSettings.getSection(settingsSection) : null;
		if(settings == null) {
			settings = new DialogSettings(settingsSection);
			settings.put(KEY_WIDTH, 480);
			settings.put(KEY_HEIGHT, 450);
			if(pluginSettings != null) {
				pluginSettings.addSection(settings);
			}
		}
		return settings;
	}

}
