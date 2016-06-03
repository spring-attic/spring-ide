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

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;

public class RenameSimilarTypesRefactoring extends Refactoring {

	public static final String REFACTORING_NAME = "Rename Similar Types";
	
	public static final Pattern CLASS_NAME = Pattern.compile(
			"\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
	);

	/**
	 * The 'main' type being renamed.
	 */
	private IType fTarget;
	private String newName;
	
	private List<IType> similarTypes = ImmutableList.of();

	public void setNewName(String newName) {
		this.newName = newName;
	}
	public void setTarget(IType target) {
		this.fTarget = target;
	}
	@Override
	public String getName() {
		return REFACTORING_NAME;
	}
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			if (fTarget==null) {
				return error("No target type to rename selected");
			}
			if (fTarget.isAnonymous()) {
				return error("Anonymous types can't be renamed");
			}
			if (!fTarget.exists()) {
				return error("Type '"+fTarget.getFullyQualifiedName()+"' doesn't exist");
			}
			if (!StringUtils.hasText(newName)) {
				return error("New name is not defined");
			}
			if (newName.equals(fTarget.getElementName())) {
				return error("New name '"+newName+"' is the same as the old name");
			}
			if (!CLASS_NAME.matcher(newName).matches()) {
				return error("'"+newName+"' is not a valid class name");
			}
			String newFqName = getNewFqName();
			IType newType = getJavaProject().findType(newFqName, new NullProgressMonitor());
			if (newType!=null && newType.exists()) {
				return error("A type with name '"+newFqName+"' already exists");
			}
			return OK;
		} catch (Exception e) {
			return error(e);
		}
	}
	
	private IJavaProject getJavaProject() {
		if (fTarget!=null) {
			return fTarget.getJavaProject();
		}
		return null;
	}
	/**
	 * Determines the new fqName for the target type
	 */
	private String getNewFqName() {
		String fqName = fTarget.getFullyQualifiedName();
		String replaceName = fTarget.getElementName();
		fqName = fqName.substring(0, fqName.length()-replaceName.length()) 
				+ newName;
		return fqName;
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

	/////////////////////////////////////////////////////////

	private RefactoringStatus error(Exception e) {
		return RefactoringStatus.create(ExceptionUtil.status(e));
	}

	private RefactoringStatus error(String string) {
		return RefactoringStatus.createErrorStatus(string);
	}
	
	private static final RefactoringStatus OK = RefactoringStatus.create(Status.OK_STATUS);



}
