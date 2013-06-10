/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.guides.wizard.GuideImportWizardModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

/**
 * A 'Download' button. Can be clicked to perform a download operation.
 * It is enabled or disabled (i.e. greyed out) based on whether the
 * content is already downloaded.
 * 
 * @author Kris De Volder
 */
public class DownloadButtonSection extends WizardPageSection {

	private GuideImportWizardModel model;
	private Button button;

	public DownloadButtonSection(
			WizardPageWithSections owner,
			GuideImportWizardModel model) {
		super(owner);
		this.model = model;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

	@Override
	public void createContents(Composite page) {
		button = new Button(page, SWT.PUSH);
		button.setText("Download");
		enableDisable();
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(button);
		model.isDownloaded.addListener(new ValueListener<Boolean>() {
			@Override
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				enableDisable();
			}
		});
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				try {
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							try {
								model.performDownload(monitor);
							} catch (Exception e) {
								throw new InvocationTargetException(e);
							}
						}
					});
				} catch (InvocationTargetException e) {
					MessageDialog.openError(button.getShell(), "Error downloading", ExceptionUtil.getMessage(e));
					GettingStartedActivator.log(e);
				} catch (InterruptedException e) {
				}

			}
		});
	}

	/** 
	 * Update enablement state of the button. Button should be disabled if guide is
	 * already downloaded.
	 */
	private void enableDisable() {
		final GettingStartedGuide g = model.getGuide();
		if (g!=null) {
			Display dis = Display.getDefault();
			if (dis!=null) {
				dis.asyncExec(new Runnable() {
					public void run() {
						button.setEnabled(!g.isDownloaded());
					}
				});
			}
		}
	}

}
