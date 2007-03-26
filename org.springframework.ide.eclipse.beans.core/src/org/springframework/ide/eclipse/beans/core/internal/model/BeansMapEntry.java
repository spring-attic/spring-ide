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

	public BeansMapEntry(IBeansMap map, Map.Entry<?, ?> entry) {
		super(map, "(map entry)", null, null);
		setLocation(entry);
	}

	public int getElementType() {
		return IBeansModelElementTypes.MAP_ENTRY_TYPE;
	}

	@Override
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
		return children.toArray(new IModelElement[children.
		                                                            size()]);
	}

	@Override
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

	@Override
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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(key);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(", key=");
		text.append(key);
		return text.toString();
	}

	private void setLocation(Map.Entry<?, ?> entry) {
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
