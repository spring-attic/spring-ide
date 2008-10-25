/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansTypeRenameRefactoringParticipant extends
		AbstractRenameRefactoringParticipant {
	
	@Override
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

	@Override
	protected void addChange(CompositeChange result, IResource resource,
			IProgressMonitor pm) throws CoreException {
		
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils.createRenameChange(
					(IFile) resource, getAffectedElements(), getNewNames(), pm);
			if (change != null) {
				result.add(change);
			}
		}
	}

	protected String[] getOldNames() {
		String[] result = new String[elements.size()];
		Iterator<Object> iter = elements.keySet().iterator();
		for (int i = 0; i < elements.size(); i++)
			result[i] = ((IType) iter.next()).getFullyQualifiedName('$');
		return result;
	}

	@Override
	protected String[] getNewNames() {
		String[] result = new String[elements.size()];
		Iterator<Object> iter = elements.keySet().iterator();
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
