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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringExecutionStarter;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.widgets.Shell;

/**
 * Starts refactoring actions for Java Elements like class names and properties
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class RenameJavaElementAction extends AbstractRefactorJavaElementAction {

	protected void run(JavaEditor fEditor, IJavaElement element) throws CoreException {
		Shell shell = fEditor.getSite().getShell();

		if (!isRenameAvailable(element)) {
			return;
		}

		// XXX workaround bug 31998
		if (ActionUtil.mustDisableJavaModelAction(shell, element)) {
			return;
		}
		RefactoringExecutionStarter.startRenameRefactoring(element, shell);
	}

	private boolean isRenameAvailable(IJavaElement element)
			throws CoreException {
		switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
			return RefactoringAvailabilityTester
					.isRenameAvailable((IJavaProject) element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return RefactoringAvailabilityTester
					.isRenameAvailable((IPackageFragmentRoot) element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return RefactoringAvailabilityTester
					.isRenameAvailable((IPackageFragment) element);
		case IJavaElement.COMPILATION_UNIT:
			return RefactoringAvailabilityTester
					.isRenameAvailable((ICompilationUnit) element);
		case IJavaElement.TYPE:
			return RefactoringAvailabilityTester
					.isRenameAvailable((IType) element);
		case IJavaElement.METHOD:
			final IMethod method = (IMethod) element;
			if (method.isConstructor())
				return RefactoringAvailabilityTester.isRenameAvailable(method
						.getDeclaringType());
			else
				return RefactoringAvailabilityTester.isRenameAvailable(method);
		case IJavaElement.FIELD:
			final IField field = (IField) element;
			if (Flags.isEnum(field.getFlags()))
				return RefactoringAvailabilityTester
						.isRenameEnumConstAvailable(field);
			else
				return RefactoringAvailabilityTester
						.isRenameFieldAvailable(field);
		case IJavaElement.TYPE_PARAMETER:
			return RefactoringAvailabilityTester
					.isRenameAvailable((ITypeParameter) element);
		case IJavaElement.LOCAL_VARIABLE:
			return RefactoringAvailabilityTester
					.isRenameAvailable((ILocalVariable) element);
		}
		return false;
	}
}
