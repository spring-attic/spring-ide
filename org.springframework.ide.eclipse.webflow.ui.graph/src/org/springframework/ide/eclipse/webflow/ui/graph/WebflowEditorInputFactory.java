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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

public class WebflowEditorInputFactory implements IElementFactory {
	/**
	 * Factory id. The workbench plug-in registers a factory by this name with
	 * the "org.eclipse.ui.elementFactories" extension point.
	 */
	private static final String ID_FACTORY = "org.springframework.ide.eclipse.webflow.ui.graph.webFlowEditorInputFactory"; //$NON-NLS-1$

	/**
	 * Tag for the IFile.fullPath of the file resource.
	 */
	private static final String TAG_PATH = "path"; //$NON-NLS-1$

	/*
	 * (non-Javadoc) Method declared on IElementFactory.
	 */
	public IAdaptable createElement(IMemento memento) {
		// Get the file name.
		String fileName = memento.getString(TAG_PATH);
		if (fileName == null) {
			return null;
		}
		// Get a handle to the IFile...which can be a handle
		// to a resource that does not exist in workspace
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
				new Path(fileName));
		if (file != null) {

			IWebflowConfig config = Activator.getModel().getProject(
					file.getProject()).getConfig(file);
			if (config != null) {
				return new WebflowEditorInput(config);
			}
		}
		return null;
	}

	/**
	 * Returns the element factory id for this class.
	 * 
	 * @return the element factory id
	 */
	public static String getFactoryId() {
		return ID_FACTORY;
	}

	/**
	 * Saves the state of the given file editor input into the given memento.
	 * 
	 * @param memento the storage area for element state
	 * @param input the file editor input
	 */
	public static void saveState(IMemento memento, WebflowEditorInput input) {
		IFile file = input.getFile();
		memento.putString(TAG_PATH, file.getFullPath().toString());
	}
}