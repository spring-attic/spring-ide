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
package org.springframework.ide.eclipse.beans.ui.graph.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Factory for saving and restoring a <code>GraphEditorInput</code>. The
 * stored representation of a <code>GraphEditorInput</code> remembers the the
 * IDs oth the element and the context.
 * <p>
 * The workbench will automatically create instances of this class as required.
 * It is not intended to be instantiated or subclassed by the client.
 * </p>
 * 
 * @see org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput
 * @author Torsten Juergeleit
 */
public class GraphEditorInputFactory implements IElementFactory {

	/**
	 * Factory id. The workbench plug-in registers a factory by this name with
	 * the "org.eclipse.ui.elementFactories" extension point.
	 */
	private static final String ID_FACTORY = BeansGraphPlugin.PLUGIN_ID
			+ ".editor.inputfactory";
	/**
	 * Tag for the ID of the element.
	 */
	private static final String TAG_ELEMENT = "element";

	/**
	 * Tag for the ID of the context.
	 */
	private static final String TAG_CONTEXT = "context";

	public IAdaptable createElement(IMemento memento) {
		String elementID = memento.getString(TAG_ELEMENT);
		String contextID = memento.getString(TAG_CONTEXT);
		if (elementID != null && contextID != null) {
			IBeansModel model = BeansCorePlugin.getModel();
			IModelElement element = model.getElement(elementID);
			if (element != null) {
				IModelElement context = model.getElement(contextID);
				if (context != null) {
					return new GraphEditorInput(element, context);
				} else {
					return new GraphEditorInput(element);
				}
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
	 * Saves the state of the given graph editor input into the given memento.
	 * 
	 * @param memento  the storage area for element state
	 * @param input  the graph editor input
	 */
	public static void saveState(IMemento memento, GraphEditorInput input) {
		memento.putString(TAG_ELEMENT, input.getElement().getElementID());
		memento.putString(TAG_CONTEXT, input.getContext().getElementID());
	}
}
