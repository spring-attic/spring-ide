/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
// TODO: remove dependency to NewElementWizard? Finish page functionality that
// is launched in a workspace job is the only useful dependency to the
// NewElementWizard
/**
 * Creates a new spring project based on either a template, or a simple project that is either Java-based or Maven.
 * <p/>
 * The different types of projects that this wizard handles is provided by a Wizard section via a wizard section factory.
 * 
 *
 */
public class NewSpringProjectWizard extends NewElementWizard implements INewWizard {

	private final NewSpringProjectWizardMainPage mainPage;

	private final SpringWizardSectionFactory sectionFactory;

	private IProject project;

	public NewSpringProjectWizard() {
		setWindowTitle("New Spring Project");
		setDefaultPageImageDescriptor(WizardImages.TEMPLATE_WIZARD_ICON);
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);

		sectionFactory = new SpringWizardSectionFactory(this);

		mainPage = new NewSpringProjectWizardMainPage();
		mainPage.setTitle(NewSpringProjectWizardMessages.NewProject_title);
		mainPage.setDescription(NewSpringProjectWizardMessages.NewProject_description);

	}

	public NewSpringProjectWizardMainPage getMainPage() {
		return mainPage;
	}

	@Override
	public void addPages() {
		// Only has one page. Additional pages can be contributed and managed by
		// wizard sections
		addPage(mainPage);
	}

	/**
	 * 
	 * @param page current page
	 * @return true if the template section contributes more pages to the wizard
	 * given a current page. False otherwise
	 */
	public boolean hasPages(IWizardPage page) {
		SpringProjectWizardSection section = getSection();

		if (section != null) {
			return section.hasNextPage(page);
		}
		return false;
	}

	// @Override
	@Override
	public boolean canFinish() {
		boolean canFinish = mainPage.isPageComplete();
		if (canFinish) {
			SpringProjectWizardSection section = getSection();

			if (section != null) {
				canFinish = section.canFinish();
			}
			else {
				// At least one section must be selected
				canFinish = false;
			}
		}

		return canFinish;
	}

	protected SpringProjectWizardSection getSection() {
		return sectionFactory.getSection(mainPage.getDescriptor());
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		SpringProjectWizardSection section = getSection();

		if (section != null) {
			return section.getNextPage(page);
		}

		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		// This will indirectly launch the project creation in a workspace job
		boolean finish = super.performFinish();

		// Once done, see if there is further configuration for the project, in
		// addition to handling
		// revealing the project in the workspace and adding it to working sets
		if (finish) {

			// Once the project creation job is done, launch any
			// configuration on the project, which
			// may involve workspace modification operations, if the section
			// implementation specifies it
			SpringProjectWizardSection section = getSection();

			if (section != null) {

				try {
					project = section.createProject(new NullProgressMonitor());
					final ProjectConfiguration configuration = section.getProjectConfiguration();

					if (configuration != null) {
						WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
							@Override
							protected void execute(IProgressMonitor monitor) throws CoreException,
									InvocationTargetException, InterruptedException {

								configuration.configureProject(project, monitor);
							}
						};
						getContainer().run(true, true, op);

					}

				}
				catch (InterruptedException e) {
					return false;
				}
				catch (InvocationTargetException error) {
					// FIXNS: Different ways of displaying error are
					// retained from older implementations (STS 3.2.0 and
					// older). Check if only
					// one version can be used

					Throwable t = error.getTargetException();
					if (t instanceof CoreException) {
						if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
							MessageDialog.openError(
									getShell(),
									NewSpringProjectWizardMessages.NewProject_errorMessage,
									NLS.bind(NewSpringProjectWizardMessages.NewProject_caseVariantExistsError,
											project.getName()));
						}
						else {
							// no special message
							ErrorDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage,
									null, ((CoreException) t).getStatus());
						}
					}
					else {
						// CoreExceptions are handled above, but unexpected
						// runtime
						// exceptions and errors may still occur.
						BeansUIPlugin.getDefault().getLog()
								.log(new Status(IStatus.ERROR, BeansUIPlugin.PLUGIN_ID, 0, t.toString(), t));
						MessageDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage,
								NLS.bind(NewSpringProjectWizardMessages.NewProject_internalError, t.getMessage()));
					}
				}
				catch (CoreException ce) {
					UiStatusHandler.logAndDisplay(ce.getStatus());
				}

				// Add to working sets even if there were configuration
				// errors
				IWorkingSet[] workingSets = getWorkingSets();
				if (workingSets.length > 0) {
					PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);
				}
			}

			// The project was created , even if there are errors, still
			// select and reveal it
			selectAndReveal(project);

		}
		return finish;
	}

	public IWorkingSet[] getWorkingSets() {
		return mainPage.getWorkingSets();
	}

	@Override
	protected void handleFinishException(Shell shell, InvocationTargetException e) {
		String title = NewWizardMessages.JavaProjectWizard_op_error_title;
		String message = NewWizardMessages.JavaProjectWizard_op_error_create_message;
		ExceptionHandler.handle(e, getShell(), title, message);
	}

	public void selectAndReveal(IProject project) {
		super.selectAndReveal(project);
	}

	@Override
	public IJavaElement getCreatedElement() {
		// Doesnt seem to be needed.
		return null;
	}

	// @Override
	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {

	}

	@Override
	public boolean performCancel() {
		SpringProjectWizardSection section = getSection();
		if (section != null) {
			section.cancel();
		}
		return true;
	}
}
