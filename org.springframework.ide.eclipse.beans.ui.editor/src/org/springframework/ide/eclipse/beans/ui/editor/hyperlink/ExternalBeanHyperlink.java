/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.editors.ZipEntryEditorInput;

public class ExternalBeanHyperlink implements IHyperlink {

	private final IRegion region;

	private final ISourceModelElement modelElement;

	/**
	 * Creates a new Java element hyperlink.
	 */
	public ExternalBeanHyperlink(IBean bean, IRegion region) {
		this.region = region;
		this.modelElement = bean;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return null;
	}

	public void open() {
		IResource resource = modelElement.getElementResource();
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (modelElement.isElementArchived()) {
				try {
					ZipEntryStorage storage = new ZipEntryStorage(resource
							.getProject(), modelElement.getElementParent().getElementName());
					IEditorInput input = new ZipEntryEditorInput(storage);
					IEditorDescriptor desc = IDE.getEditorDescriptor(storage
							.getName());
					IEditorPart editor = SpringUIUtils.openInEditor(input, desc
							.getId());
					IMarker marker = file.createMarker(IMarker.TEXT);
					marker.setAttribute(IMarker.LINE_NUMBER, modelElement
							.getElementStartLine());
					IDE.gotoMarker(editor, marker);
				} catch (CoreException e) {
					BeansCorePlugin.log(e);
				}
			} else {
				SpringUIUtils.openInEditor((IFile) modelElement
						.getElementResource(), modelElement
						.getElementStartLine());
			}
		}
	}

}
