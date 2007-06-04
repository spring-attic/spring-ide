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
package org.springframework.ide.eclipse.beans.mylar.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylar.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansUserInteractionMonitor extends AbstractUserInteractionMonitor {

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part,
			ISelection selection, boolean contributeToContext) {

		if (part instanceof XMLMultiPageEditorPart
				&& selection instanceof ITextSelection) {
			ITextEditor textEditor = (ITextEditor) part
					.getAdapter(ITextEditor.class);
			IEditorInput editorInput = textEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) editorInput).getFile();
				if (BeansCoreUtils.isBeansConfig(file)) {
					IBeansConfig beansConfig = BeansCorePlugin.getModel()
							.getConfig(file);
					int startLine = ((ITextSelection) selection).getStartLine() + 1;
					int endLine = ((ITextSelection) selection).getEndLine() + 1;

					ModelVisitor v = new ModelVisitor(startLine, endLine, file);
					beansConfig.accept(v, new NullProgressMonitor());
					IModelElement mostspecificElement = v.getElement();
					if (mostspecificElement != null) {
						super.handleElementSelection(part, mostspecificElement,
								contributeToContext);
					}
				}
			}
		}
	}

	private static class ModelVisitor implements IModelElementVisitor {

		private final int startLine;

		private final int endLine;

		private final IFile file;

		private IModelElement element;

		public IModelElement getElement() {
			return element;
		}

		public ModelVisitor(final int startLine, final int endLine,
				final IFile file) {
			this.startLine = startLine;
			this.endLine = endLine;
			this.file = file;
		}

		public boolean visit(IModelElement element, IProgressMonitor monitor) {
			if (element instanceof ISourceModelElement) {
				ISourceModelElement sourceElement = (ISourceModelElement) element;
				if (sourceElement.getElementResource().equals(file)
						&& sourceElement.getElementStartLine() <= startLine
						&& endLine <= sourceElement.getElementEndLine()) {
					this.element = element;

					if (sourceElement.getElementStartLine() == startLine
							&& endLine == sourceElement.getElementEndLine()) {
						return false;
					}
					else {
						return true;
					}
				}
				else {
					return false;
				}
			}
			else if (element instanceof IBeansConfig) {
				return true;
			}
			else {
				return false;
			}
		}
	}
}
