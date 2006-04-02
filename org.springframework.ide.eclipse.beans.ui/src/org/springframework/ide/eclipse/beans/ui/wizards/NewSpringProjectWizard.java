/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewSpringProjectWizard extends Wizard implements INewWizard {

	private NewSpringProjectCreationPage mainPage;

	private NewJavaProjectWizardPage javaPage;

	public NewSpringProjectWizard() {
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
	}

	/**
	 * Returns the newly created project.
	 * 
	 * @return the created project, or <code>null</code> if project not
	 *         created
	 */
	public IProject getNewProject() {
		return mainPage.getProjectHandle();
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(BeansWizardsMessages.NewProject_windowTitle);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_PROJECT);
	}

	public void addPages() {
		super.addPages();
		mainPage = new NewSpringProjectCreationPage("springNewProjectPage");
		mainPage.setTitle(BeansWizardsMessages.NewProject_title);
		mainPage.setDescription(BeansWizardsMessages.NewProject_description);
		addPage(mainPage);

		javaPage = new NewJavaProjectWizardPage(null, mainPage);
		addPage(javaPage);
	}

	public boolean performFinish() {

		// Run the operations to create a new Spring project
		Display display = getShell().getDisplay();
		if (display != null) {
			final IProgressMonitor monitor =
								  SpringUIUtils.getStatusLineProgressMonitor();
			display.syncExec(new Runnable() {
				public void run() {
					try {
						mainPage.getRunnable().run(monitor);
						if (mainPage.isJavaProject()) {
							javaPage.getRunnable().run(monitor);
						}
					} catch (InterruptedException e) {
					} catch (InvocationTargetException e) {
						// Unexpected runtime exceptions and errors may still occur
						Throwable t = e.getTargetException();
						BeansUIPlugin.getDefault().getLog().log(
								new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
										   0, t.toString(), t));
						MessageDialog.openError(getShell(),
							BeansWizardsMessages.NewProject_errorMessage,
							NLS.bind(
								 BeansWizardsMessages.NewProject_internalError,
								 t.getMessage()));
						}
					}
				});
		}
		return true;
	}
}
