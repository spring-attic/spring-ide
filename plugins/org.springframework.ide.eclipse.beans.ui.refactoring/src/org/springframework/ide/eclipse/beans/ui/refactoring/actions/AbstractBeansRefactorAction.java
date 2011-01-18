/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.springframework.ide.eclipse.beans.ui.actions.AbstractBeansConfigEditorAction;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract base class for XML related refactor triggers 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @since 2.0
 */
@SuppressWarnings( { "restriction" })
public abstract class AbstractBeansRefactorAction extends
		AbstractBeansConfigEditorAction {

	protected String getSelectedAttributeName(ITextSelection textSelection) {
		String attributeName = null;
		if (textSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();
			if (obj instanceof Element) {
				int offset = textSelection.getOffset()
						- ((IDOMNode) obj).getStartOffset();
				IStructuredDocumentRegion open = ((IDOMNode) obj)
						.getFirstStructuredDocumentRegion();
				ITextRegionList openRegions = open.getRegions();
				ITextRegion nameRegion = null;

				for (int i = open.getNumberOfRegions() - 1; i >= 0; i--) {
					ITextRegion region = openRegions.get(i);
					if (region.getStart() <= offset
							&& region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
						nameRegion = region;
						break;
					}
				}

				// the name region is REQUIRED to do anything useful
				if (nameRegion != null) {
					attributeName = open.getText(nameRegion);
				}
			}
		}
		return attributeName;
	}

	protected void processAction(IDocument document, ITextSelection textSelection) {
		if (textSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();
			
			Element node = null;
			String attributeName = null;
			
			if (obj instanceof Attr) {
				Attr attribute = (Attr) obj;
				node = attribute.getOwnerElement();
				attributeName = attribute.getName();
			}
			else if (obj instanceof Element) {
				node = (Element) obj;
				attributeName = getSelectedAttributeName(textSelection);
			}
			
			if (node != null && attributeName != null) {
				// check if bean class is selected
				String className = BeansEditorUtils.getAttribute(node, "class");
				String propertyName = BeansEditorUtils.getAttribute(node,
						"name");
				IJavaElement je = null;
				if ("bean".equals(node.getLocalName())
						&& StringUtils.hasText(className)
						&& "class".equals(attributeName)) {
					je = JdtUtils.getJavaType(BeansEditorUtils
							.getProject(document), className);
				}
				else if ("property".equals(node.getLocalName())
						&& StringUtils.hasText(propertyName)
						&& "name".equals(attributeName)) {
					Node beanNode = node.getParentNode();
					List<IType> types = BeansEditorUtils.getClassNamesOfBean(
							getConfigFile(), beanNode);

					if (types != null && types.size() > 0) {
						je = types.get(0).getField(propertyName);
						if (je == null || !je.exists()) {
							List<String> path = new ArrayList<String>();
							path.add(propertyName);
							je = BeansEditorUtils
									.extractMethodFromPropertyPathElements(
											path, types, getConfigFile(), 0);
						}
					}
				}

				try {
					if (je != null) {
						run(je);
					}
				}
				catch (CoreException e) {
				}
			}
		}
	}

	protected abstract void run(IJavaElement element) throws CoreException;

	@Override
	public void run(IAction action) {
		IDocument document = getTextEditor().getDocumentProvider().getDocument(
				getTextEditor().getEditorInput());
		if (document != null) {
			// get current text selection
			ITextSelection textSelection = getCurrentSelection();
			if (textSelection.isEmpty())
				return;
			processAction(document, textSelection);
		}
	}

	protected ITextSelection getCurrentSelection() {
		ISelectionProvider provider = getTextEditor().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}
		return TextSelection.emptySelection();
	}

	// *********** remove the methods below after upgrading to 3.3 ***********
	public static boolean isProcessable(JavaEditor editor) {
		if (editor == null)
			return true;
		Shell shell = editor.getSite().getShell();
		IJavaElement input = SelectionConverter.getInput(editor);
		// if a Java editor doesn't have an input of type Java element
		// then it is for sure not on the build path
		if (input == null) {
			MessageDialog.openInformation(shell,
					ActionMessages.ActionUtil_notOnBuildPath_title,
					ActionMessages.ActionUtil_notOnBuildPath_message);
			return false;
		}
		return ActionUtil.isProcessable(shell, input);
	}

	/**
	 * Check whether <code>editor</code> and <code>element</code> are
	 * processable and editable. If the editor edits the element, the validation
	 * is only performed once. If necessary, ask the user whether the file(s)
	 * should be edited.
	 * 
	 * @param editor an editor, or <code>null</code> iff the action was not
	 * executed from an editor
	 * @param shell a shell to serve as parent for a dialog
	 * @param element the element to check, cannot be <code>null</code>
	 * @return <code>true</code> if the element can be edited,
	 * <code>false</code> otherwise
	 */
	public static boolean isEditable(JavaEditor editor, Shell shell,
			IJavaElement element) {
		if (editor != null) {
			IJavaElement input = SelectionConverter.getInput(editor);
			if (input != null
					&& input.equals(element
							.getAncestor(IJavaElement.COMPILATION_UNIT))) {
				return isEditable(editor);
			}
			else {
				return isEditable(editor) && isEditable(shell, element);
			}
		}
		return isEditable(shell, element);
	}

	public static boolean isEditable(JavaEditor editor) {
		if (!isProcessable(editor)) {
			return false;
		}

		return editor.validateEditorInputState();
	}

	public static boolean isEditable(Shell shell, IJavaElement element) {
		if (!ActionUtil.isProcessable(shell, element)) {
			return false;
		}
		return true;
	}

}
