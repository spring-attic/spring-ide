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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.springframework.ide.eclipse.wizard.WizardImages;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
// TODO: remove dependency to NewElementWizard? Finish page functionality that
// is launched in a job is the only dependency to the NewElementWizard
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

	// @Override
	@Override
	public boolean canFinish() {
		boolean canFinish = mainPage.isPageComplete();
		if (canFinish) {
			SpringProjectWizardSection section = getSection();

			if (section != null) {
				canFinish = section.canFinish();
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
		if (finish) {
			if (project == null) {
				finish = false;
			}
			else {
				// Once the project creation job is done, launch any
				// configuration on the project, which
				// may involve workspace modification operations, if the section
				// implementation specifies it
				SpringProjectWizardSection section = getSection();
				if (section != null) {
					finish = section.configureProject(project);
				}
			}
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
		return null;
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		// For the moment, creation of a project occurs in finishPage(..) from
		// NewElementWizard, as the wizard inherits ability to launch workspace
		// jobs.
		SpringProjectWizardSection section = getSection();
		if (section != null) {
			project = section.createProject(monitor);
		}
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
