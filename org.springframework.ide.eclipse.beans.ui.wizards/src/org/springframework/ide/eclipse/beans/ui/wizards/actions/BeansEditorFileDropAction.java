/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.beans.ui.wizards.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.internal.AbstractDropAction;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.SpringBeanWizardResourcesAware;
import org.springframework.ide.eclipse.core.ui.dialogs.message.ErrorDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.wizards.WizardFormsDialog;

public class BeansEditorFileDropAction extends AbstractDropAction {

	public boolean run(DropTargetEvent event, IEditorPart targetEditor) {
		dropResource((IResource[]) event.data, targetEditor);
		return true;
	}

	public void dropResource(IResource resource[], IEditorPart editorPart) {
		try {
			if (resource != null && resource.length == 1) {
				IType typeDropped = getTypeInFile(resource[0]);
				if (typeDropped != null) {
					IFile file = (IFile) editorPart.getEditorInput()
							.getAdapter(IResource.class);
					IJavaProject project = JavaCore.create(file.getProject());
					if (project != null) {
						IType type = project.findType(typeDropped
								.getFullyQualifiedName());
						if (type == null) {
							return;
						}
						try {
							IBeansProject beansProject = new BeansProject(
									project.getProject());
							IBeansConfig beansConfig = new BeansConfig(
									beansProject, file.getName());
							SpringBeanWizardResourcesAware springBeansDeclarationWizard = new SpringBeanWizardResourcesAware();
							springBeansDeclarationWizard.init(beansProject,
									beansConfig,type);
							new WizardFormsDialog(editorPart.getEditorSite()
									.getShell(), springBeansDeclarationWizard)
									.open();
						} catch (Throwable e) {
							ErrorDialog errorDialog = new ErrorDialog(
									"Spring IDE Error",
									"Error while launching wizard.", e);
							errorDialog.open();
						}
					}
				}
			}
		} catch (JavaModelException e) {

		}
	}

	private static IType getTypeInFile(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (file.getProjectRelativePath().getFileExtension().equals("java")) {
				ICompilationUnit compilationUnit = JavaCore
						.createCompilationUnitFrom(file);
				if (compilationUnit != null)
					return compilationUnit.findPrimaryType();
			} else if (file.getProjectRelativePath().getFileExtension().equals(
					"class")) {
				IClassFile classFile = JavaCore.createClassFileFrom(file);
				if (classFile != null) {
					try {
						return classFile.getType();
					} catch (JavaModelException e) {
					}
				}
			}
		}
		return null;
	}
}
