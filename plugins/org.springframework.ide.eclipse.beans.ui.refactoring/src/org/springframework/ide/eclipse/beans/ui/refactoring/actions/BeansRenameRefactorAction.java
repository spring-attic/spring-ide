/*******************************************************************************
 * Copyright (c) 2007, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.refactoring.ltk.RenameIdRefactoring;
import org.springframework.ide.eclipse.beans.ui.refactoring.ltk.RenameIdRefactoringWizard;
import org.springframework.ide.eclipse.beans.ui.refactoring.ltk.RenameIdType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Starts refactoring actions for Java Elements like class names and properties
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @author Tomasz Zarna
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansRenameRefactorAction extends AbstractBeansRefactorAction {

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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IDocument document = getTextEditor(event).getDocumentProvider().getDocument(
				getTextEditor(event).getEditorInput());
		if (document != null) {
			// get current text selection
			ITextSelection textSelection = getCurrentSelection(event);
			if (textSelection.isEmpty())
				return null;

			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();

			Element node = null;
			String attributeName = null;

			if (obj instanceof Attr) {
				Attr attribute = (Attr) obj;
				node = attribute.getOwnerElement();
				attributeName = attribute.getName();
			} else if (obj instanceof Element) {
				node = (Element) obj;
				attributeName = getSelectedAttributeName(textSelection);
			}

			if (node != null && attributeName != null) {
				// start Spring IDE's own bean id refactoring
				RenameIdType idType = null;

				if ("bean".equals(node.getLocalName())
						&& "id".equals(attributeName)) {
					idType = RenameIdType.BEAN;
				} else if ("tx:advice".equals(node.getNodeName())
						&& "id".equals(attributeName)) {
					idType = RenameIdType.ADVICE;
				} else if ("aop:pointcut".equals(node.getNodeName())
						&& "id".equals(attributeName)) {
					idType = RenameIdType.POINTCUT;
				}

				if (idType != null) {
					RenameIdRefactoring refactoring = new RenameIdRefactoring();
					refactoring.setType(idType);
					refactoring.setNode((IDOMNode) node);
					refactoring.setBeanId(BeansEditorUtils.getAttribute(node,
							"id"));
					refactoring.setFile(getConfigFile(event));
					refactoring.setOffset(textSelection.getOffset());
					RenameIdRefactoringWizard wizard = new RenameIdRefactoringWizard(
							refactoring, "Rename " + idType.getType() + " id");
					run(wizard, BeansUIPlugin.getActiveWorkbenchShell(),
							"Rename " + idType.getType() + " id");
				} else {
					processAction(event, document, textSelection);
				}
			}
		}
		return null;
	}

	@Override
	protected void run(IJavaElement element) throws CoreException {
		if (!isRenameAvailable(element)
				|| !ActionUtil.isProcessable(
						BeansUIPlugin.getActiveWorkbenchShell(), element)) {
			return;
		}
		// XXX workaround bug 31998
		if (ActionUtil.mustDisableJavaModelAction(
				BeansUIPlugin.getActiveWorkbenchShell(), element)) {
			return;
		}
		RefactoringExecutionStarter.startRenameRefactoring(element,
				BeansUIPlugin.getActiveWorkbenchShell());
	}

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(
					wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

}
