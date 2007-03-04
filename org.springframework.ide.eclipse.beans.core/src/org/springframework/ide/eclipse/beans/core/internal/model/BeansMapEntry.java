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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProperties;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Holds a {@link Map.Entry}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansMapEntry extends AbstractBeansValueHolder implements
		IBeansMapEntry {

	private Object key;

	public BeansMapEntry(IBeansMap map, Map.Entry entry) {
		super(map, "(map entry)", null, null);
		setLocation(entry);
	}

	public int getElementType() {
		return IBeansModelElementTypes.MAP_ENTRY_TYPE;
	}

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		if (!(getElementParent() instanceof IBeansProperties)
				&& key instanceof IModelElement) {
			children.add((IModelElement) key);
		}
		Object value = getValue();
		if (value instanceof IModelElement) {
			children.add((IModelElement) value);
		}
		return (IModelElement[]) children.toArray(new IModelElement[children.
		                                                            size()]);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this entry's key
			if (key instanceof IModelElement) {
				((IModelElement) key).accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this entry's value
			super.accept(visitor, monitor);
		}
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getKey() {
		return key;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansMapEntry)) {
			return false;
		}
		BeansMapEntry that = (BeansMapEntry) other;
		if (!ObjectUtils.nullSafeEquals(this.key, that.key)) return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(key);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(", key=");
		text.append(key);
		return text.toString();
	}

	private void setLocation(Map.Entry entry) {
		XmlSourceLocation location = null;
		Object key = entry.getKey();
		if (key instanceof BeanMetadataElement) {
			Object source = ((BeanMetadataElement) key).getSource();
			if (source instanceof XmlSourceLocation) {
				location = new XmlSourceLocation((XmlSourceLocation) source);
			}
		}

		Object value = entry.getValue();
		if (value instanceof BeanMetadataElement) {
			Object source = ((BeanMetadataElement) value).getSource();
			if (source instanceof XmlSourceLocation) {
				if (location == null) {
					location = new XmlSourceLocation((XmlSourceLocation)
							source);
				} else {
					location.setEndLine(((XmlSourceLocation) source)
							.getEndLine());
				}
			}
		}
		if (location != null) {
			location.setLocalName("<entry");
			setElementSourceLocation(location);
		}
	}
}
