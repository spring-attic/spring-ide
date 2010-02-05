/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class NewWebflowConfigWizard extends Wizard implements INewWizard {

	private NewWebflowConfigFilePage mainPage;

	private LinkToBeansConfigWizardPage linkPage;

	private IStructuredSelection selection;

	private IWebflowConfig newConfig;

	public NewWebflowConfigWizard() {
		setDialogSettings(Activator.getDefault().getDialogSettings());
	}

	/**
	 * Returns the newly created beans config.
	 * @return the created beans config, or <code>null</code> if config not created
	 */
	public IWebflowConfig getNewConfig() {
		return newConfig;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
		setWindowTitle("Create a new Spring Web Flow Definition file");
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_CONFIG);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new NewWebflowConfigFilePage("webflowNewConfigPage", selection);
		linkPage = new LinkToBeansConfigWizardPage("webflowLinkConfigPage");
		// mainPage.setTitle(BeansWizardsMessages.NewConfig_title);
		// mainPage.setDescription(BeansWizardsMessages.NewConfig_description);
		addPage(mainPage);
		addPage(linkPage);
	}

	@Override
	public boolean performFinish() {
		createNewConfig();
		if (newConfig == null) {
			return false;
		}

		BasicNewResourceWizard.selectAndReveal(newConfig.getResource(), BeansUIPlugin.getActiveWorkbenchWindow());
		SpringUIUtils.openInEditor(newConfig.getResource(), -1);
		return true;
	}

	private void createNewConfig() {

		// append file extension
		if (!StringUtils.hasText(mainPage.getFileExtension())) {
			mainPage.setFileExtension("xml");
		}
		
		// create the new Spring project operation
		final IFile file = mainPage.createNewFile();
		newConfig = new WebflowConfig(getProject(file));
		newConfig.setResource(file);
		String name = linkPage.getName();
		if (name != null && !"".equals(name)) {
			newConfig.setName(name);
		}
		newConfig.setBeansConfigs(linkPage.getBeansConfigs());

		IWebflowProject wfp = getProject(file);
		List<IWebflowConfig> configs = wfp.getConfigs();
		configs.add(newConfig);

		wfp.setConfigs(configs);
	}

	private IWebflowProject getProject(final IFile file) {
		if (!SpringCoreUtils.isSpringProject(file)) {
			WorkspaceModifyOperation oper = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					SpringCoreUtils.addProjectNature(file.getProject(), SpringCore.NATURE_ID, monitor);
				}
			};
			try {
				getContainer().run(true, true, oper);
			}
			catch (InvocationTargetException e) {
				SpringCore.log(e);
			}
			catch (InterruptedException e) {
				SpringCore.log(e);
			}
		}

		IWebflowProject webflowProject = org.springframework.ide.eclipse.webflow.core.Activator.getModel().getProject(
				file.getProject());

		if (webflowProject != null) {
			return webflowProject;
		}
		return new WebflowProject(file.getProject(), org.springframework.ide.eclipse.webflow.core.Activator.getModel());
	}
}
