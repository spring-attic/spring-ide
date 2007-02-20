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

import org.eclipse.jdt.core.WorkingCopyOwner;

public class CompilationUnitHelper {

	private CompilationProblemRequestor fProblemRequestor = null;

	private WorkingCopyOwner fWorkingCopyOwner = null;

	private static CompilationUnitHelper instance;

	private CompilationUnitHelper() {
		// force use of instance
	}

	public synchronized static final CompilationUnitHelper getInstance() {

		if (instance == null)
			instance = new CompilationUnitHelper();
		return instance;
	}

	public CompilationProblemRequestor getProblemRequestor() {

		if (fProblemRequestor == null)
			fProblemRequestor = new CompilationProblemRequestor();
		return fProblemRequestor;
	}

	public WorkingCopyOwner getWorkingCopyOwner() {

		if (fWorkingCopyOwner == null) {
			fWorkingCopyOwner = new WorkingCopyOwner() {
				public String toString() {
					return "Spring Beans IDE Working copy owner"; //$NON-NLS-1$
				}
			};
		}
		return fWorkingCopyOwner;
	}
}
