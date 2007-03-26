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
package org.springframework.ide.eclipse.webflow.ui.graph;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * @author Christian Dupuis
 * @since 2.0
 *
 */
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
