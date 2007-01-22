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
		return MODEL_TYPE;
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
