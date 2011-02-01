/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
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
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Abstract super class for implementing {@link RenameParticipant}
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 */
public abstract class AbstractRenameRefactoringParticipant extends RenameParticipant {

	protected IProject project;

	protected Map<Object, Object> elements;

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (!getArguments().getUpdateReferences()) {
			return null;
		}
		Set<IResource> processedResources = new HashSet<IResource>();

		CompositeChange result = new CompositeChange(getName());
		Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
		for (IBeansProject beansProject : projects) {
			Set<IBeansConfig> beansConfigs = beansProject.getConfigs();
			for (IBeansConfig beansConfig : beansConfigs) {
				if (!processedResources.contains(beansConfig.getElementResource())) {
					addChange(result, beansConfig.getElementResource(), pm);
					processedResources.add(beansConfig.getElementResource());
				}
				for (IBeansImport import_ : beansConfig.getImports()) {
					for (IBeansConfig config : import_.getImportedBeansConfigs()) {
						if (!processedResources.contains(config.getElementResource())) {
							addChange(result, config.getElementResource(), pm);
							processedResources.add(config.getElementResource());
						}
					}
				}
			}
		}
		return (result.getChildren().length == 0) ? null : result;
	}

	protected abstract void addChange(CompositeChange result, IResource resource, IProgressMonitor pm)
			throws CoreException;

	protected IJavaElement[] getAffectedElements() {
		Set<Object> objects = elements.keySet();
		return objects.toArray(new IJavaElement[objects.size()]);
	}

	@Override
	public String getName() {
		return "Rename classes referenced in Spring Bean definitions";
	}
}
