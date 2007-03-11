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
