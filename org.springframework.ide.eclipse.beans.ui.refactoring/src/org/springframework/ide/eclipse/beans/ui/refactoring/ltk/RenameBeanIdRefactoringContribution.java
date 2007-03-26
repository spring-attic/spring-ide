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

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class RenameBeanIdRefactoringContribution extends
		RefactoringContribution {

	@SuppressWarnings("unchecked")
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project,
			String description, String comment, Map arguments, int flags) {
		return new RenameBeanIdRefactoringDescriptor(project, description,
				comment, arguments);
	}

	@Override
	public Map retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof RenameBeanIdRefactoringDescriptor)
			return ((RenameBeanIdRefactoringDescriptor) descriptor)
					.getArguments();
		return super.retrieveArgumentMap(descriptor);
	}
}
