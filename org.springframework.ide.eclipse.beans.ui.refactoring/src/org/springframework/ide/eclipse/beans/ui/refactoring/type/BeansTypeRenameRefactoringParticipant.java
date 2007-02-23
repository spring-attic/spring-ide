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

package org.springframework.ide.eclipse.beans.ui.refactoring.type;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.springframework.ide.eclipse.beans.ui.refactoring.AbstractRenameRefactoringParticipant;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

public class BeansTypeRenameRefactoringParticipant extends
		AbstractRenameRefactoringParticipant {

	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			IJavaProject javaProject = (IJavaProject) type
					.getAncestor(IJavaElement.JAVA_PROJECT);
			project = javaProject.getProject();
			if (SpringCoreUtils.isSpringProject(project)) {
				elements = new HashMap<Object, Object>();
				elements.put(type, getArguments().getNewName());
				return true;
			}
		}
		return false;
	}
	
	protected void addChange(CompositeChange result, IResource resource,
			IProgressMonitor pm) throws CoreException {
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils.createRenameChange((IFile) resource,
					getAffectedElements(), getNewNames(), pm);
			if (change != null)
				result.add(change);
		}
	}

	protected String[] getOldNames() {
		String[] result = new String[elements.size()];
		Iterator iter = elements.keySet().iterator();
		for (int i = 0; i < elements.size(); i++)
			result[i] = ((IType) iter.next()).getFullyQualifiedName('$');
		return result;
	}

	protected String[] getNewNames() {
		String[] result = new String[elements.size()];
		Iterator iter = elements.keySet().iterator();
		for (int i = 0; i < elements.size(); i++) {
			IType type = (IType) iter.next();
			String oldName = type.getFullyQualifiedName('$');
			int index = oldName.lastIndexOf(type.getElementName());
			StringBuffer buffer = new StringBuffer(oldName.substring(0, index));
			buffer.append(elements.get(type));
			result[i] = buffer.toString();
		}
		return result;
	}
}
