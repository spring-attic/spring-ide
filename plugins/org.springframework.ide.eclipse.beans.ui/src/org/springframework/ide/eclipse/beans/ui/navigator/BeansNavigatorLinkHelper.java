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
package org.springframework.ide.eclipse.beans.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.navigator.actions.ILinkHelperExtension;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * {@link ILinkHelper} implementation for resolving links to {@link IBeansModelElement}s.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansNavigatorLinkHelper implements ILinkHelper, ILinkHelperExtension {

	/**
	 * {@inheritDoc}
	 */
	public void activateEditor(IWorkbenchPage page, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	public IStructuredSelection findSelection(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();

			// Ensure that if the project is not loaded we skip this in the UI
			IBeansProject project = BeansCorePlugin.getModel().getProject(file.getProject());
			if (project instanceof ILazyInitializedModelElement
					&& !((ILazyInitializedModelElement) project).isInitialized()) {
				return StructuredSelection.EMPTY;
			}

			IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
			if (config != null) {
				IEditorPart editor = SpringUIUtils.getActiveEditor();
				if (editor.getEditorInput() == input && editor.getSite() != null
						&& editor.getSite().getSelectionProvider() != null) {
					ISelection selection = editor.getSite().getSelectionProvider().getSelection();
					IModelElement element = BeansUIUtils.getSelectedElement(selection, config);
					if (element != null) {
						return new TreeSelection(BeansUIUtils.createTreePath(element));
					}
				}
			}
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * {@inheritDoc}
	 */
	public IStructuredSelection findSelection(Object object) {
		if (object instanceof ElementImpl) {
			ElementImpl element = (ElementImpl) object;
			IStructuredDocument document = element.getStructuredDocument();
			IFile resource = SpringUIUtils.getFile(document);

			// Ensure that if the project is not loaded we skip this in the UI
			IBeansProject project = BeansCorePlugin.getModel().getProject(resource.getProject());
			if (project instanceof ILazyInitializedModelElement
					&& !((ILazyInitializedModelElement) project).isInitialized()) {
				return StructuredSelection.EMPTY;
			}
			
			// Make sure that the file is actually a beans config
			if (!BeansCoreUtils.isBeansConfig(resource)) {
				return null;
			}

			// The line number-based approach is the best approximation that we can currently do
			int startLine = document.getLineOfOffset(element.getStartOffset()) + 1;
			int endLine = document.getLineOfOffset(element.getEndOffset()) + 1;

			IModelElement modelElement = BeansModelUtils.getMostSpecificModelElement(startLine, endLine, resource,
					new NullProgressMonitor());
			if (modelElement != null) {
				return new TreeSelection(BeansUIUtils.createTreePath(modelElement));
			}
			return new StructuredSelection(resource);
		}

		return null;
	}

}
