/*******************************************************************************
 * Copyright (c) 2006, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * {@link INewWizard} implementation that creates a new {@link IBeansConfig} instance.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class NewBeansConfigWizard extends Wizard implements INewWizard {

	private NewBeansConfigFilePage mainPage;

	private LinkToBeansConfigSetWizardPage linkPage;

	private NamespaceSelectionWizardPage xsdPage;

	private IStructuredSelection selection;

	private IBeansConfig newConfig;

	public NewBeansConfigWizard() {
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
	}

	/**
	 * Returns the newly created beans config.
	 * @return the created beans config, or <code>null</code> if config not created
	 */
	public IBeansConfig getNewConfig() {
		return newConfig;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
		setWindowTitle(BeansWizardsMessages.NewConfig_windowTitle);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_CONFIG);
	}

	@Override
	public void addPages() {
		super.addPages();
		xsdPage = new NamespaceSelectionWizardPage("xsdPage");
		mainPage = new NewBeansConfigFilePage("beansNewConfigPage", selection, xsdPage);
		linkPage = new LinkToBeansConfigSetWizardPage("beansLinkConfigPage");
		addPage(mainPage);
		addPage(xsdPage);
		addPage(linkPage);
	}

	@Override
	public boolean performFinish() {
		createNewConfig();
		if (newConfig == null) {
			return false;
		}

		BasicNewResourceWizard
				.selectAndReveal(newConfig.getElementResource(), BeansUIPlugin.getActiveWorkbenchWindow());
		SpringUIUtils.openInEditor((IFile) newConfig.getElementResource(), -1);
		return true;
	}

	private void createNewConfig() {
		// create the new Spring project operation
		mainPage.setXmlSchemaDefinitions(xsdPage.getXmlSchemaDefinitions());
		mainPage.setSchemaVersions(xsdPage.getSchemaVersions());

		// append file extension
		if (!StringUtils.hasText(mainPage.getFileExtension())) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(mainPage.getContainerFullPath());
			mainPage.setFileExtension("xml");
			BeansProject beansProject = getProject(resource);
			if (beansProject != null) {
				Set<String> suffixes = beansProject.getConfigSuffixes();
				if (!suffixes.isEmpty()) {
					mainPage.setFileExtension(suffixes.iterator().next());
				}
			}
		}

		final IFile file = mainPage.createNewFile();
		BeansProject beansProject = getProject(file);

		if (beansProject == null) {
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
			beansProject = new BeansProject(BeansCorePlugin.getModel(), file.getProject());
		}

		beansProject.addConfig(file, IBeansConfig.Type.MANUAL);
		newConfig = beansProject.getConfig(file);

		Set<IBeansConfigSet> configSets = linkPage.getBeansConfigSets();
		for (IBeansConfigSet bcs : configSets) {
			if (beansProject.equals(bcs.getElementParent())) {
				((BeansConfigSet) bcs).addConfig(newConfig.getId());
			}
		}

		// Now save modified project description
		beansProject.saveDescription();

		// Finally (after saving the modified project description!!!)
		// refresh the label decoration of all config files
		BeansModelLabelDecorator.update();
	}

	private BeansProject getProject(IResource file) {
		return (BeansProject) BeansCorePlugin.getModel().getProject(file.getProject());
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		return super.getNextPage(page);
	}
}
