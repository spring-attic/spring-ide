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
package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.runtime.ListenerList;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * Default implementation of the common protocol for a model.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractModel extends AbstractModelElement implements
		IModel {

	private ListenerList listeners;

	public AbstractModel(IModelElement parent, String name) {
		super(parent, name);
		listeners = new ListenerList();
	}

	public int getElementType() {
		return IModelElementTypes.MODEL_TYPE;
	}

	public final void addChangeListener(IModelChangeListener listener) {
		listeners.add(listener);
	}

	public final void removeChangeListener(IModelChangeListener listener) {
		listeners.remove(listener);
	}

	public final void notifyListeners(IModelElement element, Type type) {
		ModelChangeEvent event = new ModelChangeEvent(element, type);
		for (Object listener : listeners.getListeners()) {
			((IModelChangeListener) listener).elementChanged(event);
		}
	}
}
