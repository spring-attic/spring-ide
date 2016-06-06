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

import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class RefactoringStatuses {

	public static RefactoringStatus error(Exception e) {
		return RefactoringStatus.create(ExceptionUtil.status(e));
	}

	public static RefactoringStatus error(String string) {
		return RefactoringStatus.createErrorStatus(string);
	}

	public static final RefactoringStatus OK = RefactoringStatus.create(Status.OK_STATUS);

	public static RefactoringStatus fatal(Throwable e) {
		return RefactoringStatus.createFatalErrorStatus(ExceptionUtil.getMessage(e));
	}

	public static RefactoringStatus warn(String msg) {
		return RefactoringStatus.createWarningStatus(msg);
	}

	public static RefactoringStatus fatal(String msg) {
		return RefactoringStatus.createFatalErrorStatus(msg);
	}


}
