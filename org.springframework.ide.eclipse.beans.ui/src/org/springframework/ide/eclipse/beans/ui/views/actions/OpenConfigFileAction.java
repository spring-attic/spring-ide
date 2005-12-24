/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.views.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.views.BeansView;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class OpenConfigFileAction extends Action {

	private static final String PREFIX = "View.OpenConfigFileAction.";

    private BeansView view;

	public OpenConfigFileAction(BeansView view) {
		super(BeansUIPlugin.getResourceString(PREFIX + "label"));
		setToolTipText(BeansUIPlugin.getResourceString(PREFIX + "tooltip"));
		this.view = view;
    }

	public boolean isEnabled() {
		IResource resource = view.getResourceFromSelectedNode();
		return (resource instanceof IFile && resource.exists());
	}

	public void run() {
		IResource resource = view.getResourceFromSelectedNode();
		if (resource instanceof IFile && resource.exists()) {
			int line = view.getStartLineFromSelectedNode();
			SpringUIUtils.openInEditor((IFile) resource, line);
		}
	}
}
