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

import java.lang.reflect.Constructor;
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
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.reorg.CreateTargetQueries;
import org.eclipse.jdt.internal.ui.refactoring.reorg.ReorgMoveWizard;
import org.eclipse.jdt.internal.ui.refactoring.reorg.ReorgQueries;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.util.ClassUtils;

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
		startRefactoring(resources, javaElements, BeansUIPlugin.getActiveWorkbenchShell());
	}

	protected void startRefactoring(IResource[] resources, IJavaElement[] javaElements, Shell shell)
			throws CoreException {
		if (RefactoringAvailabilityTester.isMoveAvailable(resources, javaElements)) {
			IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(resources, javaElements);
			if (policy.canEnable()) {
				JavaMoveProcessor processor = new JavaMoveProcessor(policy);
				Refactoring refactoring = new MoveRefactoring(processor);
				
				RefactoringWizard wizard = null;
				
				// Hack to allow usage of this refactoring on Eclipse < 3.4 and 3.4
				if (SpringCoreUtils.isEclipseSameOrNewer(3, 4)) {
					// RefactoringWizard wizard = new ReorgMoveWizard(processor,refactoring);
					Constructor cons = ClassUtils.getConstructorIfAvailable(ReorgMoveWizard.class, new Class[] {
							JavaMoveProcessor.class, Refactoring.class });
					if (cons != null) {
						try {
							wizard = (RefactoringWizard) cons.newInstance(processor, refactoring);
						}
						catch (Exception e) {
							BeansUIPlugin.log(e);
						}
					}
				}
				else {
					// RefactoringWizard wizard = new ReorgMoveWizard(refactoring);
					Constructor cons = ClassUtils.getConstructorIfAvailable(ReorgMoveWizard.class, new Class[] {
						Refactoring.class });
					if (cons != null) {
						try {
							wizard = (RefactoringWizard) cons.newInstance(refactoring);
						}
						catch (Exception e) {
							BeansUIPlugin.log(e);
						}
					}
				}
				if (wizard != null) {
					processor.setCreateTargetQueries(new CreateTargetQueries(wizard));
					processor.setReorgQueries(new ReorgQueries(wizard));
					new RefactoringStarter().activate(refactoring, wizard, shell,
							RefactoringMessages.OpenRefactoringWizardAction_refactoring,
							RefactoringSaveHelper.SAVE_ALL_ALWAYS_ASK);
				}
			}
		}
	}

}
