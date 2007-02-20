/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;

public class CompilationProblemRequestor implements IProblemRequestor {

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