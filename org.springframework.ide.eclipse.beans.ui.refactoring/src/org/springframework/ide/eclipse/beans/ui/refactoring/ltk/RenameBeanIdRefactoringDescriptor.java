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

package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RenameBeanIdRefactoringDescriptor extends RefactoringDescriptor {

	public static final String REFACTORING_ID = "org.springframework.ide.eclipse.beans.ui.refactoring.ltk.renameBeanIdRefactoring";

	private final Map<String, String> arguments;

	public RenameBeanIdRefactoringDescriptor(String project,
			String description, String comment, Map<String, String> arguments) {
		super(REFACTORING_ID, project, description, comment,
				RefactoringDescriptor.STRUCTURAL_CHANGE
						| RefactoringDescriptor.MULTI_CHANGE);
		this.arguments = arguments;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status)
			throws CoreException {
		RenameBeanIdRefactoring refactoring = new RenameBeanIdRefactoring();
		status.merge(refactoring.initialize(arguments));
		return refactoring;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}
}