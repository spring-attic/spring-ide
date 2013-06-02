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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Nieraj Singh
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
		mainPage.setDescription("Create a Spring project by selecting a template or simple project type.");
	}

	public NewSpringProjectWizardMainPage getMainPage() {
		return mainPage;
	}

	@Override
	public void addPages() {

		addPage(mainPage);

		// Also preload any section pages that require their controls to be
		// created
		// before a user clicks "Next", as some sections may need access to the
		// controls
		// if the user clicks "Finish" from the first page without ever going to
		// the next page
		List<SpringProjectWizardSection> sections = sectionFactory.loadSections();
		for (SpringProjectWizardSection section : sections) {
			List<IWizardPage> pages = section.loadPages();
			if (pages != null) {
				for (IWizardPage page : pages) {
					addPage(page);
				}
			}
		}
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

				CoreException coreException = null;
				try {
					ProjectConfiguration configuration = section.getProjectConfiguration();

					// Creating and configuring projects are separate steps for
					// now
					// as creating projects may need to be run in the UI thread
					// to access
					// wizard page widgets
					// while configuring the project does not. This is only
					// required as the wizard
					// was merged from two prior separate wizards, and the UI
					// coupling of the project
					// creation wasn't fully refactored out.
					IProject project = createProject(configuration);

					configureProject(project, configuration);
				}
				catch (InterruptedException e) {
					return false;
				}
				catch (InvocationTargetException error) {

					Throwable t = error.getTargetException();
					if (t instanceof CoreException) {
						coreException = (CoreException) t;
					}
					else {
						// CoreExceptions are handled separately, but unexpected
						// runtime
						// exceptions and errors may still occur.
						BeansUIPlugin.getDefault().getLog()
								.log(new Status(IStatus.ERROR, BeansUIPlugin.PLUGIN_ID, 0, t.toString(), t));
						MessageDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage,
								NLS.bind(NewSpringProjectWizardMessages.NewProject_internalError, t.getMessage()));
					}
				}
				catch (CoreException ce) {
					coreException = ce;
				}

				if (coreException != null) {
					handleError(coreException);
				}

			}

		}
		return finish;
	}

	protected IProject createProject(final ProjectConfiguration projectConfiguration) throws InterruptedException,
			InvocationTargetException, CoreException {

		// Prevent workspace builds when first creating project to avoid error
		// markers
		// from appearing, as the project will later be configured and built
		// again.
		enableWorkspaceBuild(false);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				project = projectConfiguration.createProject(monitor);

			}
		};

		// Do not fork. Run in UI thread as project creation may
		// require access to widgets in wizard pages
		getContainer().run(false, true, op);
		return project;
	}

	protected void configureProject(final IProject project, final ProjectConfiguration projectConfiguration)
			throws InterruptedException, InvocationTargetException, CoreException {

		enableWorkspaceBuild(true);

		final IWorkingSet[] workingSets = getWorkingSets();

		final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {

				projectConfiguration.configureProject(monitor);

				// The project was created , even if there are
				// errors, still
				// select and reveal it
				selectAndReveal(project);

				if (workingSets != null && workingSets.length > 0) {
					PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, workingSets);
				}
			}
		};

		// Run in UI thread as some dialogues may open during configuration
		getContainer().run(false, false, op);

	}

	protected void enableWorkspaceBuild(boolean enable) throws CoreException {
		IWorkspaceDescription wsd = ResourcesPlugin.getWorkspace().getDescription();
		if (!wsd.isAutoBuilding() == enable) {
			wsd.setAutoBuilding(enable);
			ResourcesPlugin.getWorkspace().setDescription(wsd);
		}
	}

	protected void handleError(CoreException coreException) {
		if (coreException != null) {
			if (coreException.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
				String projectName = project != null ? project.getName() : "";
				MessageDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage,
						NLS.bind(NewSpringProjectWizardMessages.NewProject_caseVariantExistsError, projectName));
			}
			else {
				// no special message
				ErrorDialog.openError(getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage, null,
						coreException.getStatus());
			}
		}
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

	public void selectAndReveal(final IProject project) {
		super.selectAndReveal(project);
		// TODO: Alternative to using the NewElementWizard select and reveal
		// once NewElementWizard dependency is removed
		// getContainer().getShell().getDisplay().syncExec(new Runnable() {
		//
		// public void run() {
		// BasicNewResourceWizard.selectAndReveal(project,
		// getWorkbench().getActiveWorkbenchWindow());
		// }
		// });
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
