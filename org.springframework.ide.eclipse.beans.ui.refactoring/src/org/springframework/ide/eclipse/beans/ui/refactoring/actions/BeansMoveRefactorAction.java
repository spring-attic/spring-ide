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
package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgUtils;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.internal.corext.refactoring.structure.JavaMoveRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.reorg.CreateTargetQueries;
import org.eclipse.jdt.internal.ui.refactoring.reorg.ReorgMoveWizard;
import org.eclipse.jdt.internal.ui.refactoring.reorg.ReorgQueries;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * Starts move refactoring actions for Java Elements like class
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansMoveRefactorAction extends AbstractBeansRefactorAction {

	@Override
	protected void run(IJavaElement element) throws CoreException {
		if (!(element instanceof IType)) {
			return;
		}

		List<IJavaElement> elements = new ArrayList<IJavaElement>();
		elements.add(element);
		IResource[] resources = ReorgUtils.getResources(elements);
		IJavaElement[] javaElements = ReorgUtils.getJavaElements(elements);
		startRefactoring(resources, javaElements, BeansUIPlugin
				.getActiveWorkbenchShell());
	}

	protected void startRefactoring(IResource[] resources,
			IJavaElement[] javaElements, Shell shell) throws CoreException {
		if (RefactoringAvailabilityTester.isMoveAvailable(resources,
				javaElements)) {
			IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(resources,
					javaElements);
			if (policy.canEnable()) {
				final JavaMoveProcessor processor = new JavaMoveProcessor(
						policy);
				final JavaMoveRefactoring refactoring = new JavaMoveRefactoring(
						processor);
				final RefactoringWizard wizard = new ReorgMoveWizard(
						refactoring);
				processor
						.setCreateTargetQueries(new CreateTargetQueries(wizard));
				processor.setReorgQueries(new ReorgQueries(wizard));
				new RefactoringStarter()
						.activate(
								refactoring,
								wizard,
								shell,
								RefactoringMessages.OpenRefactoringWizardAction_refactoring,
								true);
			}
		}
	}
}
