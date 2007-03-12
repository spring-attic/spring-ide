/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph;

import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;

/**
 * 
 */
public abstract class WebflowUtils {

	public static WebflowEditor getActiveFlowEditor() {

		IEditorPart editorPart = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart instanceof WebflowEditor) {
			return (WebflowEditor) editorPart;
		}
		return null;
	}

	public static WebflowEditorInput getActiveFlowEditorInput() {

		WebflowEditor editor = getActiveFlowEditor();
		if (editor != null) {
			return (WebflowEditorInput) editor.getEditorInput();
		}
		return null;
	}

	public static IWebflowConfig getActoveWebflowConfig() {
		WebflowEditorInput editorInput = getActiveFlowEditorInput();
		if (editorInput != null && editorInput.getFile() != null) {
			IWebflowProject project = org.springframework.ide.eclipse.webflow.core.Activator
					.getModel().getProject(editorInput.getFile().getProject());
			if (project != null) {
				return project.getConfig(editorInput.getFile());
			}
		}
		return null;
	}

}