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
package org.springframework.ide.eclipse.config.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;

//Copied from JDT's NewClassCreationWizard and PDE's JavaAttributeWizardPage
@SuppressWarnings("restriction")
public class ExtendedNewClassCreationWizard extends NewElementWizard {

	private ExtendedNewClassWizardPage mainPage;

	private final IProject project;

	private final String className;

	private final boolean openEditor;

	public ExtendedNewClassCreationWizard(IProject project, String className, boolean openEditorOnFinish) {
		super();
		this.project = project;
		this.className = className;
		this.openEditor = openEditorOnFinish;

		setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewWizardMessages.NewClassCreationWizard_title);
	}

	@Override
	public void addPages() {
		if (mainPage == null) {
			mainPage = new ExtendedNewClassWizardPage(project, className);
			mainPage.init();
		}
		addPage(mainPage);
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		mainPage.createType(monitor);
	}

	@Override
	public IJavaElement getCreatedElement() {
		return mainPage.getCreatedType();
	}

	public String getQualifiedName() {
		if (mainPage.getCreatedType() == null) {
			return null;
		}
		return mainPage.getCreatedType().getFullyQualifiedName('$');
	}

	@Override
	public boolean performFinish() {
		warnAboutTypeCommentDeprecation();
		boolean res = super.performFinish();
		if (res) {
			IResource resource = mainPage.getModifiedResource();
			if (resource != null) {
				selectAndReveal(resource);
				if (openEditor) {
					openResource((IFile) resource);
				}
			}
		}
		return res;
	}

}
