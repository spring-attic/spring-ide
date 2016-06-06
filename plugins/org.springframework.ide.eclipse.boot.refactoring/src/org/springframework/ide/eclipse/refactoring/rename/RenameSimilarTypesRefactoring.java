/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.refactoring.rename;

import static org.springframework.ide.eclipse.refactoring.rename.RefactoringStatuses.OK;
import static org.springframework.ide.eclipse.refactoring.rename.RefactoringStatuses.*;
import static org.springframework.ide.eclipse.refactoring.rename.RefactoringStatuses.fatal;

import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableSet;

public class RenameSimilarTypesRefactoring extends Refactoring {

	public static final String REFACTORING_NAME = "Rename Similar Types";

	public static final Pattern CLASS_NAME = Pattern.compile(
			"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
	);

	private IJavaProject project;

	/**
	 * Name fragment to look for.
	 */
	private String oldName;

	/**
	 * What to replace the fragment with.
	 */
	private String newName;

	/**
	 * All types selected for renaming.
	 */
	private Set<IType> targets;

	private ImmutableSet<IType> foundTypes;



	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}
	@Override
	public String getName() {
		return REFACTORING_NAME;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		try {
			if (!StringUtils.hasText(oldName)) {
				return error("Old name is not defined");
			}
			if (!StringUtils.hasText(newName)) {
				return fatal("New name is not defined");
			}
			if (newName.equals(oldName)) {
				return warn("New name '"+newName+"' is the same as the old name");
			}
			if (foundTypes.isEmpty()) {
				return error("No types matching '"+oldName+"' found");
			}
			if (targets.isEmpty()) {
				return warn("No types are selected for renaming");
			}
			if (!CLASS_NAME.matcher(newName).matches()) {
				return fatal("'"+newName+"' is not a valid type name");
			}
			return OK;
		} catch (Exception e) {
			return fatal(e);
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.createErrorStatus("The refactoring has not been implemented yet!");
	}
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		throw ExceptionUtil.coreException("The refactoring has not been implemented yet!");
	}

	public void setSelectedTypes(Set<IType> value) {
		this.targets = value;
	}

	public void setFoundTypes(ImmutableSet<IType> value) {
		this.foundTypes = value;
	}

}
