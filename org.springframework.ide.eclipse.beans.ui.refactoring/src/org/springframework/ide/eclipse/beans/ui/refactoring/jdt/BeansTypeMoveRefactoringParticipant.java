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

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansTypeMoveRefactoringParticipant extends AbstractMoveRefactoringParticipant {

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			IType type = (IType) element;
			IJavaProject javaProject = (IJavaProject) type.getAncestor(IJavaElement.JAVA_PROJECT);
			project = javaProject.getProject();
			if (SpringCoreUtils.isSpringProject(project)) {
				elements = new ArrayList<Object>();
				elements.add(element);
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Rename classes referenced in Spring Bean definitions";
	}

	@Override
	protected void addChange(CompositeChange result, IResource resource, IProgressMonitor pm) throws CoreException {
		if (resource.exists()) {
			Change change = BeansRefactoringChangeUtils.createRenameChange((IFile) resource, getAffectedElements(),
					getNewNames(), pm);
			if (change != null)
				result.add(change);
		}
	}

	protected IJavaElement[] getAffectedElements() {
		return elements.toArray(new IJavaElement[elements.size()]);
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
			result[i] = buffer.toString() + ((IJavaElement) elements.get(i)).getElementName();
		}
		return result;
	}
}
