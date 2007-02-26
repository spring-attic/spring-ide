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

package org.springframework.ide.eclipse.beans.ui.refactoring.jdt;

import java.util.Iterator;
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
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Abstract super class for implemeting {@link RenameParticipant}
 * 
 * @author Christian Dupuis
 */
public abstract class AbstractRenameRefactoringParticipant extends
		RenameParticipant {

	protected IProject project;

	protected Map<Object, Object> elements;

	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	public void addElement(Object element, RefactoringArguments arguments) {
		elements.put(element, ((RenameArguments) arguments).getNewName());
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

	protected IJavaElement[] getAffectedElements() {
		Set<Object> objects = elements.keySet();
		return (IJavaElement[]) objects
				.toArray(new IJavaElement[objects.size()]);
	}

	protected String[] getNewNames() {
		String[] result = new String[elements.size()];
		Iterator<Object> iter = elements.values().iterator();
		for (int i = 0; i < elements.size(); i++)
			result[i] = iter.next().toString();
		return result;
	}

	public String getName() {
		return "Rename classes referenced in Spring Bean definitions";
	}
}
