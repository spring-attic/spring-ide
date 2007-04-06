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
package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class RenameBeanIdRefactoringWizard extends RefactoringWizard {

	public RenameBeanIdRefactoringWizard(RenameBeanIdRefactoring refactoring,
			String pageTitle) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE
				| PREVIEW_EXPAND_FIRST_NODE);
		setDefaultPageTitle(pageTitle);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RenameBeanIdRefactoringInputPage(
				"IntroduceIndirectionInputPage"));
	}
}
