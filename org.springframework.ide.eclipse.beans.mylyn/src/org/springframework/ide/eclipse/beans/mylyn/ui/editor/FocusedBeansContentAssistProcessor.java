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
package org.springframework.ide.eclipse.beans.mylyn.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean.BeansContentAssistProcessor;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.mylyn.ui.editor.FocusedStructuredTextViewerContentAssistProcessor;

/**
 * {@link IContentAssistProcessor} that delegates to a nested
 * {@link BeansContentAssistProcessor}.
 * <p>
 * This implementation extends
 * {@link FocusedStructuredTextViewerContentAssistProcessor} to reorder
 * proposals based on the current interest level. Therefore it overrides the
 * {@link #determineHandleForProposedElement(Object)} to provide the
 * {@link IBeansModel} specific element handles.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class FocusedBeansContentAssistProcessor extends
		FocusedStructuredTextViewerContentAssistProcessor implements
		IContentAssistProcessor {

	public FocusedBeansContentAssistProcessor(IContentAssistProcessor processor) {
		super(processor);
	}

	protected String determineHandleForProposedElement(Object element) {
		String handle = super.determineHandleForProposedElement(element);
		if (handle == null) {
			if (element != null && element instanceof IBeansModelElement) {
				return ((IBeansModelElement) element).getElementID();
			}
			else if (element != null && element instanceof ElementImpl) {
				ElementImpl node = (ElementImpl) element;
				IStructuredDocument document = node.getStructuredDocument();
				IFile resource = getResource(document);
				if (document != null) {
					int startLine = document.getLineOfOffset(node
							.getStartOffset()) + 1;
					int endLine = document.getLineOfOffset(node.getEndOffset()) + 1;
					IModelElement mostspecificElement = BeansModelUtils
							.getMostSpecificModelElement(startLine, endLine,
									resource, null);
					if (mostspecificElement != null
							&& mostspecificElement instanceof IBeansModelElement) {
						return ((IBeansModelElement) mostspecificElement)
								.getElementID();
					}
				}
			}
		}
		return handle;
	}

	private IFile getResource(IStructuredDocument document) {
		if (document != null) {
			IStructuredModel model = StructuredModelManager.getModelManager()
					.getModelForRead(document);
			IFile resource = null;
			try {
				String baselocation = model.getBaseLocation();
				if (baselocation != null) {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					IPath filePath = new Path(baselocation);
					if (filePath.segmentCount() > 0) {
						resource = root.getFile(filePath);
					}
				}
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
			return resource;
		}
		return null;
	}
}