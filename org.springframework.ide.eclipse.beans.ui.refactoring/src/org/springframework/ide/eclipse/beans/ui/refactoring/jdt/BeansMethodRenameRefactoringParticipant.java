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

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansMethodRenameRefactoringParticipant extends AbstractRenameRefactoringParticipant {

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IMethod) {
			IMethod method = (IMethod) element;
			IJavaProject javaProject = (IJavaProject) method.getAncestor(IJavaElement.JAVA_PROJECT);
			project = javaProject.getProject();
			if (SpringCoreUtils.isSpringProject(project) && method.getElementName().startsWith("set")) {
				elements = new HashMap<Object, Object>();
				elements.put(method, getArguments().getNewName());
				return true;
			}
		}
		return false;
	}

	@Override
	protected void addChange(CompositeChange result, IResource resource, IProgressMonitor pm) throws CoreException {
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils.createMethodRenameChange((IFile) resource,
					getAffectedElements(), getNewNames(), pm);
			if (change != null)
				result.add(change);
		}
	}

	@Override
	protected String[] getNewNames() {
		String[] result = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			String newName = getArguments().getNewName();
			if (newName.startsWith("set")) {
				newName = StringUtils.uncapitalize(newName.substring(3));
			}
			result[i] = newName;
		}
		return result;
	}

	@Override
	public String getName() {
		return "Rename properties referenced in Spring Bean definitions";
	}
}
