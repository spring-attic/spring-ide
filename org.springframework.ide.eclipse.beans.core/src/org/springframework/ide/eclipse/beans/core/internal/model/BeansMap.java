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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * Holds a managed {@link Map}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansMap extends AbstractBeansModelElement implements IBeansMap {

	private LinkedHashMap<Object, Object> map;
	private LinkedList<IBeansMapEntry> entries;

	public BeansMap(IModelElement parent, ManagedMap map) {
		super(parent, "(map)", map);

		// Create new list with values from given list
		this.map = new LinkedHashMap<Object, Object>();
		this.entries = new LinkedList<IBeansMapEntry>();
		for (Object entry : map.entrySet()) {
			Map.Entry mEntry = (Map.Entry) entry;
			this.map.put(mEntry.getKey(), mEntry.getValue());

			BeansMapEntry bmEntry = new BeansMapEntry(this, mEntry);
			bmEntry.setKey(BeansModelUtils.resolveValueIfNecessary(bmEntry,
					mEntry.getKey()));
			bmEntry.setValue(BeansModelUtils
					.resolveValueIfNecessary(bmEntry, mEntry.getValue()));
			entries.add(bmEntry);
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.MAP_TYPE;
	}

	public IModelElement[] getElementChildren() {
		List<IBeansMapEntry> children = new ArrayList<IBeansMapEntry>();
		for (IBeansMapEntry entry : entries) {
			children.add(entry);
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this map's entries
			for (Object value : entries) {
				if (value instanceof IModelElement) {
					((IModelElement) value).accept(visitor, monitor);
					if (monitor.isCanceled()) {
						return;
					}
				}
			}
		}
	}

	public Map getMap() {
		return map;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansMap)) {
			return false;
		}
		BeansMap that = (BeansMap) other;
		if (!ObjectUtils.nullSafeEquals(this.map, that.map)) return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(map);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": map=");
		text.append(map);
		return text.toString();
	}
}
