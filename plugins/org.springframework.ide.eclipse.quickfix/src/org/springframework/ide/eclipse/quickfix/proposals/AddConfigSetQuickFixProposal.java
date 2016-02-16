/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.proposals;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.internal.model.update.BeansModelUpdater;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * Quick fix proposal for creating a new config set
 * @author Terry Denney
 * @since 2.1
 */
public class AddConfigSetQuickFixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private class ConfigSetNameWizard extends Wizard {

		private String configSetName;

		private ConfigSetNameWizardPage page;

		public ConfigSetNameWizard() {
			setWindowTitle("Bean Reference Quick Fix");
		}

		@Override
		public void addPages() {
			page = new ConfigSetNameWizardPage();
			addPage(page);
		}

		public String getConfigSetName() {
			return configSetName;
		}

		@Override
		public boolean performFinish() {
			configSetName = page.getConfigSetName();
			return true;
		}
	}

	private class ConfigSetNameWizardPage extends WizardPage {

		private Text text;

		private String configSetName;

		private boolean canFinish;

		protected ConfigSetNameWizardPage() {
			super("configSetNameWizardPage");
		}

		@Override
		public boolean canFlipToNextPage() {
			return canFinish;
		}

		public void createControl(Composite parent) {
			setTitle("Create New Config Set");
			setMessage("Enter the name for the new config set.");

			parent.setLayout(new GridLayout());

			Composite control = new Composite(parent, SWT.NONE);
			control.setLayout(new GridLayout(2, false));
			control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Label label = new Label(control, SWT.NONE);
			label.setText("Name: ");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			text = new Text(control, SWT.BORDER);
			text.setEditable(true);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			text.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					configSetName = text.getText();
					update();
					getWizard().getContainer().updateButtons();
				}
			});

			setControl(control);
		}

		public String getConfigSetName() {
			return configSetName;
		}

		private void update() {
			String name = text.getText();
			if (name != null && name.length() > 0) {
				if (!project.hasConfigSet(name)) {
					canFinish = true;
					setErrorMessage(null);
				}
				else {
					canFinish = false;
					setErrorMessage("A config set with name " + name + " already exists.");
				}
			}
			else {
				canFinish = false;
				setErrorMessage(null);
			}
		}
	}

	private final String configName;

	private final String referencedConfigName;

	private final BeansProject project;

	private String configSetName;

	public AddConfigSetQuickFixProposal(int offset, int length, boolean missingEndQuote, IBean importBean, IFile file) {
		super(offset, length, missingEndQuote);

		this.configName = QuickfixUtils.getConfigName(file);
		this.referencedConfigName = QuickfixUtils.getConfigName(importBean.getElementResource());

		IBeansModel model = BeansCorePlugin.getModel();
		this.project = (BeansProject) model.getProject(file.getProject());
	}

	public AddConfigSetQuickFixProposal(int offset, int length, boolean missingEndQuote, IBean importBean, IFile file,
			String configSetName) {
		this(offset, length, missingEndQuote, importBean, file);
		this.configSetName = configSetName;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		ConfigSetNameWizard wizard = new ConfigSetNameWizard();

		if (configSetName == null) {
			WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
			if (dialog.open() == Dialog.OK) {
				configSetName = wizard.getConfigSetName();
			}
		}

		if (configSetName != null) {
			Set<String> configNames = new HashSet<String>();
			configNames.add(configName);
			configNames.add(referencedConfigName);

			IBeansConfigSet configSet = new BeansConfigSet(project, configSetName, configNames,
					IBeansConfigSet.Type.MANUAL);
			project.addConfigSet(configSet);
			project.saveDescription();
			BeansModelUpdater.updateProject(project);
		}

	}

	public String getDisplayString() {
		return "Create new config set with " + configName + " and " + referencedConfigName;
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
	}

}
