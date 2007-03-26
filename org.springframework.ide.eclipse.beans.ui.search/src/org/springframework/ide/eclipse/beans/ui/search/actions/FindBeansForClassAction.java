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

	@Override
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
