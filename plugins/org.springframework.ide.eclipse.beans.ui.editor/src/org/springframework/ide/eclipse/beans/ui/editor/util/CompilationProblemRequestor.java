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
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * @author Christian Dupuis
 */
class CompilationProblemRequestor implements IProblemRequestor {

	private boolean fIsActive = false;

	private boolean fIsRunning = false;

	private List<IProblem> fCollectedProblems;

	public void beginReporting() {

		fIsRunning = true;
		fCollectedProblems = new ArrayList<IProblem>();
	}

	public void acceptProblem(IProblem problem) {

		if (isActive())
			fCollectedProblems.add(problem);
	}

	public void endReporting() {

		fIsRunning = false;
	}

	public boolean isActive() {

		return fIsActive && fCollectedProblems != null;
	}

	/**
	 * Sets the active state of this problem requestor.
	 * 
	 * @param isActive the state of this problem requestor
	 */
	public void setIsActive(boolean isActive) {

		if (fIsActive != isActive) {
			fIsActive = isActive;
			if (fIsActive)
				startCollectingProblems();
			else
				stopCollectingProblems();
		}
	}

	/**
	 * Tells this annotation model to collect temporary problems from now on.
	 */
	private void startCollectingProblems() {

		fCollectedProblems = new ArrayList<IProblem>();
	}

	/**
	 * Tells this annotation model to no longer collect temporary problems.
	 */
	private void stopCollectingProblems() {

		// do nothing
	}

	/**
	 * @return the list of collected problems
	 */
	public List getCollectedProblems() {

		return fCollectedProblems;
	}

	public boolean isRunning() {

		return fIsRunning;
	}
}
