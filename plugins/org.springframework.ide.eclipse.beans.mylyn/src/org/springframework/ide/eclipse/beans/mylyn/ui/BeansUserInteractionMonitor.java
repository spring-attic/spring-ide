/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylyn.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.mylyn.core.BeansContextStructureBridge;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.w3c.dom.Text;

/**
 * {@link AbstractUserInteractionMonitor} extension that tracks current selections in the open
 * editor and sends back feedback to Mylyn that a element has been selected and the interest level
 * should be increased.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansUserInteractionMonitor extends AbstractUserInteractionMonitor {

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection,
			boolean contributeToContext) {

		if ((part instanceof XMLMultiPageEditorPart || part instanceof IBeansXmlEditor)
				&& selection instanceof ITextSelection) {
			ITextEditor textEditor = (ITextEditor) part.getAdapter(ITextEditor.class);
			IEditorInput editorInput = textEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) editorInput).getFile();
				if (BeansContextStructureBridge.isBeansConfig(file)) {

					int startLine = ((ITextSelection) selection).getStartLine() + 1;
					int endLine = ((ITextSelection) selection).getEndLine() + 1;

					IStructuredSelection sel = (IStructuredSelection) selection;
					for (Object obj : sel.toArray()) {
						// Try to resolve to a better node instead of working with text
						if (obj instanceof Text && ((IDOMNode) obj).getOwnerDocument() != null) {
							IDOMNode sibling = (IDOMNode) ((Text) obj).getNextSibling();
							if (sibling != null) {
								int siblingStartLine = ((IDOMDocument) sibling.getOwnerDocument())
										.getStructuredDocument().getLineOfOffset(
												sibling.getStartOffset()) + 1;
								int siblingEndLine = ((IDOMDocument) sibling.getOwnerDocument())
										.getStructuredDocument().getLineOfOffset(
												sibling.getEndOffset()) + 1;
								if (startLine == siblingStartLine) {
									startLine = siblingStartLine;
									endLine = siblingEndLine;
								}
							}
						}
						else if (obj instanceof IDOMNode
								&& ((IDOMNode) obj).getOwnerDocument() != null
								&& !((IDOMNode) obj).getOwnerDocument().getDocumentElement()
										.equals(obj)) {
							IDOMNode node = (IDOMNode) obj;
							startLine = ((IDOMDocument) node.getOwnerDocument())
									.getStructuredDocument().getLineOfOffset(node.getStartOffset()) + 1;
							endLine = ((IDOMDocument) node.getOwnerDocument())
									.getStructuredDocument().getLineOfOffset(node.getEndOffset()) + 1;
						}

						IModelElement mostspecificElement = BeansModelUtils
								.getMostSpecificModelElement(startLine, endLine, file, null);
						if (mostspecificElement != null) {
							super.handleElementSelection(part, mostspecificElement,
									contributeToContext);
						}
					}

				}
			}
		}
	}
}
