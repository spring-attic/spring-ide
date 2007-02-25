/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.refactoring;

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

	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	public void addElement(Object element, RefactoringArguments arguments) {
		elements.add(element);
	}

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
