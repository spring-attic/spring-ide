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
import java.util.Iterator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

public class BeansPackageRenameRefactoringParticipant extends
		BeansTypeRenameRefactoringParticipant {

	@Override
	protected boolean initialize(Object element) {
		try {
			if (element instanceof IPackageFragment) {
				IPackageFragment fragment = (IPackageFragment) element;
				if (!fragment.containsJavaResources())
					return false;
				IJavaProject javaProject = (IJavaProject) fragment
						.getAncestor(IJavaElement.JAVA_PROJECT);
				project = javaProject.getProject();
				if (SpringCoreUtils.isSpringProject(project)) {
					elements = new HashMap<Object, Object>();
					elements.put(fragment, getArguments().getNewName());
					return true;
				}
			}
		}
		catch (JavaModelException e) {
		}
		return false;
	}

	@Override
	protected String[] getNewNames() {
		String[] result = new String[elements.size()];
		Iterator<Object> iter = elements.values().iterator();
		for (int i = 0; i < elements.size(); i++)
			result[i] = iter.next().toString();
		return result;
	}
}
