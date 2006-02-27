/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.search.actions;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchScope;
import org.springframework.ide.eclipse.beans.ui.search.internal.queries.BeanClassQuery;

/**
 * @author Torsten Juergeleit
 */
public class FindBeansForClassAction extends Action
											   implements IViewActionDelegate {
	private ISelection selection;

	public void init(IViewPart view) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void run(IAction action) {
		run();
	}

	public void run() {
		String className = getSelectedClassName();
		if (className != null) {
			BeansSearchScope scope = BeansSearchScope.newSearchScope();
			ISearchQuery query = new BeanClassQuery(scope, className, true,
													false);
			NewSearchUI.activateSearchResultView();
			NewSearchUI.runQueryInBackground(query);
		}
	}

	private String getSelectedClassName() {
		if ((selection instanceof IStructuredSelection) &&
														!selection.isEmpty()) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof IType) {
				return ((IType) obj).getFullyQualifiedName();
			} else if (obj instanceof ICompilationUnit) {
				return ((ICompilationUnit)
								obj).findPrimaryType().getFullyQualifiedName();
			} else if (obj instanceof IClassFile) {
				try {
					return ((IClassFile) obj).getType().getFullyQualifiedName();
				} catch (JavaModelException e) {
					// Can't do nothing here
				}
			}
		}
		return null;
	}
}
