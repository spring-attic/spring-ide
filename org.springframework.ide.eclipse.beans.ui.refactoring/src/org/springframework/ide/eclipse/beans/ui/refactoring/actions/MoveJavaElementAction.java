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

package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringExecutionStarter;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgUtils;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.widgets.Shell;

/**
 * Starts move refactoring actions for Java Elements like class
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class MoveJavaElementAction extends AbstractRefactorJavaElementAction {

	protected void run(JavaEditor fEditor, IJavaElement element,
			boolean lightweight) throws CoreException {
		Shell shell = fEditor.getSite().getShell();

		if (!(element instanceof IType)) {
			return;
		}

		// Work around for http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
		if (!ActionUtil.isEditable(fEditor, shell, element)) {
			return;
		}

		List<IJavaElement> elements = new ArrayList<IJavaElement>();
		elements.add(element);
		IResource[] resources = ReorgUtils.getResources(elements);
		IJavaElement[] javaElements = ReorgUtils.getJavaElements(elements);
		if (RefactoringAvailabilityTester.isMoveAvailable(resources,
				javaElements)) {
			RefactoringExecutionStarter.startMoveRefactoring(resources,
					javaElements, shell);
		}
	}
}
