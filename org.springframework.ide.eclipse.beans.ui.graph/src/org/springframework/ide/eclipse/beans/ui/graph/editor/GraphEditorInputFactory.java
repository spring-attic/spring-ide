/*
 * Copyright 2002-2006 the original author or authors.
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
