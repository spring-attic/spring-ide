/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;

public class ContentTypeEnablerDisabler extends AbstractHandler implements IExecutableExtension {

	private String contentTypeId = "org.springframework.ide.eclipse.applicationProperties";
	private boolean enable;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource rsrc = getResource(event);
		if (rsrc!=null) {
			execute(rsrc);
		}
		return null;
	}

	protected void execute(IResource rsrc) throws ExecutionException {
		try {
			IContentType ctype = Platform.getContentTypeManager().getContentType(contentTypeId);
			if (enable) {
				ctype.addFileSpec(rsrc.getName(), IContentType.FILE_NAME_SPEC);
			} else {
				ctype.removeFileSpec(rsrc.getName(), IContentType.FILE_NAME_SPEC);
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}

	private IResource getResource(ExecutionEvent event) {
		List<IResource> rs = getResources(event);
		if (rs!=null && !rs.isEmpty()) {
			return rs.get(0);
		}
		return null;
	}

	private List<IResource> getResources(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection!=null) {
			List<IResource> selected = new ArrayList<IResource>();
			if (selection instanceof IStructuredSelection) {
				Iterator<?> iter = ((IStructuredSelection) selection).iterator();
				while (iter.hasNext()) {
					Object obj = iter.next();
					IResource resource = null;
					if (obj instanceof IResource) {
						resource = (IResource) obj;
					} else if (obj instanceof IAdaptable) {
						resource = (IResource) ((IAdaptable) obj)
						.getAdapter(IResource.class);
					}
					if (resource != null) {
						selected.add(resource);
					}
				}
			} else {
				Object editorInput = HandlerUtil.getActiveEditorInput(event);
				if (editorInput != null) {
					if (editorInput instanceof IFileEditorInput) {
						selected.add(((IFileEditorInput) editorInput).getFile());
					}
				}
			}
			return selected;
		}
		return Collections.emptyList();
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		String[] parts = ((String)data).split(",");
		enable = parts[0].equals("enable");
		contentTypeId = parts[1];
	}

}
