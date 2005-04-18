/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractModel extends AbstractModelElement
															implements IModel {
	private List listeners;

	public AbstractModel(IModelElement parent, String name) {
		super(parent, name);
		this.listeners = new ArrayList();
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

	protected final void notifyListeners(IModelElement element, int type) {
		ModelChangeEvent event = new ModelChangeEvent(element, type);
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			IModelChangeListener listener = (IModelChangeListener) iter.next();
			listener.elementChanged(event);
		}
	}
}
