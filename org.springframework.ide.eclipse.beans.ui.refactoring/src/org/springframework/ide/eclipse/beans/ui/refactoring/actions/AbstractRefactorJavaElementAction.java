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
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.ui.actions.AbstractBeansConfigEditorAction;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author Christian Dupuis
 * @since 2.0
 * 
 */
@SuppressWarnings( { "unchecked", "restriction" })
public abstract class AbstractRefactorJavaElementAction extends
		AbstractBeansConfigEditorAction {

	private void processAction(IDocument document, ITextSelection textSelection) {
		if (textSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();
			if (obj instanceof Element) {
				Element node = (Element) obj;
				// check if bean class is selected
				String className = BeansEditorUtils.getAttribute(node, "class");
				String propertyName = BeansEditorUtils.getAttribute(node,
						"name");
				IJavaElement je = null;
				if ("bean".equals(node.getLocalName())
						&& StringUtils.hasText(className)) {
					je = BeansModelUtils.getJavaType(BeansEditorUtils
							.getProject(document), className);
				}
				else if ("property".equals(node.getLocalName())
						&& StringUtils.hasText(propertyName)) {
					Node beanNode = node.getParentNode();
					List<IType> types = BeansEditorUtils.getClassNamesOfBean(
							getConfigFile(), beanNode);

					if (types != null && types.size() > 0) {
						je = types.get(0).getField(propertyName);
						if (je == null) {
							List<String> path = new ArrayList<String>();
							path.add(propertyName);
							je = BeansEditorUtils
									.extractMethodFromPropertyPathElements(
											path, types, getConfigFile(), 0);
						}
					}
				}

				if (je != null) {

					// bring up the Java Editor
					IEditorPart javaEditorPart = SpringUIUtils.openInEditor(je);
					SpringUIUtils.openInEditor(getConfigFile(), textSelection
							.getStartLine() + 1);
					try {
						if (javaEditorPart != null && je != null) {
							JavaEditor editor = (JavaEditor) javaEditorPart;
							// Work around for
							// http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
							if (!isEditable(editor,
									editor.getSite().getShell(), je)) {
								return;
							}

							run(editor, je);
						}
					}
					catch (CoreException e) {
					}
				}
			}
		}
	}

	protected abstract void run(JavaEditor fEditor, IJavaElement element)
			throws CoreException;

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

	private ITextSelection getCurrentSelection() {
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
