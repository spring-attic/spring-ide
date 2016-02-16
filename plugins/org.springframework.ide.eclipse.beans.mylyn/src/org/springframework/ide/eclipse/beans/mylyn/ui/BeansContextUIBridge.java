/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylyn.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.AbstractContextUiBridge;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * {@link AbstractContextUiBridge} that integrates the {@link IBeansModel} with
 * Mylyn in the UI.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansContextUIBridge extends AbstractContextUiBridge {

	private static final String CONTENT_TYPE = "spring_beans";

	public BeansContextUIBridge() {
		new BeansUserInteractionMonitor();
	}

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof XMLMultiPageEditorPart;
	}

	@Override
	public void close(IInteractionElement element) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				IEditorReference[] references = page.getEditorReferences();
				for (int i = 0; i < references.length; i++) {
					IEditorPart part = references[i].getEditor(false);
					if (part != null && part instanceof XMLMultiPageEditorPart) {
						XMLMultiPageEditorPart editor = (XMLMultiPageEditorPart) part;
						IEditorInput editorInput = editor.getEditorInput();
						if (editorInput != null
								&& editorInput instanceof IFileEditorInput) {
							IResource resource = ((IFileEditorInput) editorInput)
									.getFile();
							IModelElement modelElement = BeansModelUtils
									.getResourceModelElement(resource);
							if (modelElement != null
									&& element.getHandleIdentifier().equals(
											modelElement.getElementID())) {
								editor.dispose();
							}
						}
					}
				}
			}
		}
		catch (Throwable t) {
		}
	}

	@Override
	public List<TreeViewer> getContentOutlineViewers(IEditorPart editorPart) {
		if (editorPart == null)
			return null;
		List<TreeViewer> viewers = new ArrayList<TreeViewer>();
		return viewers;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public IInteractionElement getElement(IEditorInput editorInput) {
		if (editorInput != null && editorInput instanceof IFileEditorInput) {
			IResource resource = ((IFileEditorInput) editorInput).getFile();
			IModelElement modelElement = BeansModelUtils
					.getResourceModelElement(resource);
			String handle = ContextCore.getStructureBridge(
					modelElement).getHandleIdentifier(modelElement);
			return ContextCore.getContextManager().getElement(handle);
		}
		return null;
	}

	@Override
	public Object getObjectForTextSelection(TextSelection textSelection,
			IEditorPart editorPart) {
		if (textSelection != null) {

		}
		return null;
	}

	@Override
	public void open(IInteractionElement element) {
		String handle = element.getHandleIdentifier();
		IModelElement modelElement = BeansCorePlugin.getModel().getElement(
				handle);
		if (modelElement != null
				&& modelElement instanceof IResourceModelElement) {
			BeansUIUtils.openInEditor((IResourceModelElement) modelElement);
		}
	}
}
