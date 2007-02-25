package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
 * @author cd
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
							.getStartLine());
					try {
						if (javaEditorPart != null && je != null) {
							JavaEditor editor = (JavaEditor) javaEditorPart;
							IPreferenceStore store = JavaPlugin.getDefault()
									.getPreferenceStore();
							run(
									editor,
									je,
									store
											.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT));
						}
					}
					catch (CoreException e) {
					}
				}
			}
		}
	}

	protected abstract void run(JavaEditor fEditor, IJavaElement element,
			boolean lightweight) throws CoreException;

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

}
