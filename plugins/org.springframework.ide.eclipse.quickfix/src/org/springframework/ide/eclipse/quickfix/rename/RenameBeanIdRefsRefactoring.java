/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.rename;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;

/**
 * Refactoring for renaming bean reference for an @bean method (referenced
 * org.springframework
 * .ide.eclipse.beans.ui.refactoring.ltk.RenameBeanIdRefactoring)
 * 
 * @author Terry Denney
 * @author Martin Lippert
 */
public class RenameBeanIdRefsRefactoring extends Refactoring {

	private final IMethod method;

	private final String newBeanId;

	private final String oldBeanId;

	public RenameBeanIdRefsRefactoring(IMethod method, String newBeanId) {
		this.method = method;
		this.newBeanId = newBeanId;
		this.oldBeanId = method.getElementName();
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("Creating change...", 1);
			CompositeChange compositeChange = new CompositeChange("Renaming Spring Bean id");

			IBeansProject beanProject = BeansCorePlugin.getModel().getProject(method.getResource().getProject());
			Set<IBeansConfig> configs = beanProject.getConfigs();

			// TODO: make sure I should be including all config files
			for (IBeansConfig config : configs) {
				IResource resource = config.getElementResource();
				if (resource.isAccessible() && resource instanceof IFile) {
					Change refsChange = BeansRefactoringChangeUtils.createRenameBeanRefsChange((IFile) resource,
							oldBeanId, newBeanId, monitor);
					compositeChange.add(refsChange);
				}
			}
			return compositeChange;
		}
		finally {
			monitor.done();
		}
	}

	@Override
	public String getName() {
		return "Rename Bean";
	}

}
