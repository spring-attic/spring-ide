/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.jdt;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Abstract class for implementing {@link MoveParticipant}
 * 
 * @author Christian Dupuis
 */
public abstract class AbstractMoveRefactoringParticipant extends
		MoveParticipant implements ISharableParticipant {

	protected IProject project;

	protected List<Object> elements;

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	public void addElement(Object element, RefactoringArguments arguments) {
		elements.add(element);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		if (!getArguments().getUpdateReferences())
			return null;
		CompositeChange result = new CompositeChange(getName());
		Set<IBeansProject> projects = BeansCorePlugin.getModel().getProjects();
		for (IBeansProject beansProject : projects) {
			Set<IBeansConfig> beansConfigs = beansProject.getConfigs();
			for (IBeansConfig beansConfig : beansConfigs) {
				addChange(result, beansConfig.getElementResource(), pm);
			}
		}
		return (result.getChildren().length == 0) ? null : result;
	}

	protected abstract void addChange(CompositeChange result,
			IResource resource, IProgressMonitor pm) throws CoreException;
}
