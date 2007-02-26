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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

public class BeansTypeMoveRefactoringParticipant extends
		AbstractMoveRefactoringParticipant {

	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			IJavaProject javaProject = (IJavaProject) type
					.getAncestor(IJavaElement.JAVA_PROJECT);
			project = javaProject.getProject();
			if (SpringCoreUtils.isSpringProject(project)) {
				elements = new ArrayList<Object>();
				elements.add(element);
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return "Rename classes referenced in Spring Bean definitions";
	}

	protected void addChange(CompositeChange result, IResource resource,
			IProgressMonitor pm) throws CoreException {
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils.createRenameChange(
					(IFile) resource, getAffectedElements(), getNewNames(), pm);
			if (change != null)
				result.add(change);
		}
	}

	protected IJavaElement[] getAffectedElements() {
		return (IJavaElement[]) elements.toArray(new IJavaElement[elements
				.size()]);
	}

	private String[] getNewNames() {
		Object destination = getArguments().getDestination();
		StringBuffer buffer = new StringBuffer();
		if (destination instanceof IPackageFragment) {
			buffer.append(((IPackageFragment) destination).getElementName());
			if (buffer.length() > 0)
				buffer.append("."); //$NON-NLS-1$
		}
		String[] result = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			result[i] = buffer.toString()
					+ ((IJavaElement) elements.get(i)).getElementName();
		}
		return result;
	}
}
