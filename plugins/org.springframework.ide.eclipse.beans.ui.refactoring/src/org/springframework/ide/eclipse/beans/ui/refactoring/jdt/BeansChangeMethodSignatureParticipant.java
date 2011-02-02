/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.jdt;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.refactoring.participants.ChangeMethodSignatureParticipant;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.util.StringUtils;

/**
 * @author Martin Lippert
 * @since 2.6.0
 */
public class BeansChangeMethodSignatureParticipant extends
		ChangeMethodSignatureParticipant {

	private IMethod refactoredMethod;
	private String newName;

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IMethod) {
			IMethod method = (IMethod) element;
			IJavaProject javaProject = (IJavaProject) method
					.getAncestor(IJavaElement.JAVA_PROJECT);
			IProject project = javaProject.getProject();
			if (SpringCoreUtils.isSpringProject(project)) {
				refactoredMethod = method;
				newName = getArguments().getNewName();
				if (newName.startsWith("set")) {
					newName = StringUtils.uncapitalize(newName.substring(3));
				}
				
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Rename properties referenced in Spring Bean definitions";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		Set<IResource> processedResources = new HashSet<IResource>();

		CompositeChange result = new CompositeChange(getName());
		Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
		for (IBeansProject beansProject : projects) {
			Set<IBeansConfig> beansConfigs = beansProject.getConfigs();
			for (IBeansConfig beansConfig : beansConfigs) {
				if (!processedResources.contains(beansConfig
						.getElementResource())) {
					addChange(result, beansConfig.getElementResource(), pm);
					processedResources.add(beansConfig.getElementResource());
				}
				for (IBeansImport import_ : beansConfig.getImports()) {
					for (IBeansConfig config : import_
							.getImportedBeansConfigs()) {
						if (!processedResources.contains(config
								.getElementResource())) {
							addChange(result, config.getElementResource(), pm);
							processedResources.add(config.getElementResource());
						}
					}
				}
			}
		}
		return (result.getChildren().length == 0) ? null : result;
	}

	protected void addChange(CompositeChange result, IResource resource,
			IProgressMonitor pm) throws CoreException {
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils
					.createMethodRenameChange((IFile) resource,
							new IJavaElement[] {refactoredMethod}, new String[] {newName}, pm);
			if (change != null)
				result.add(change);
		}
	}
}
