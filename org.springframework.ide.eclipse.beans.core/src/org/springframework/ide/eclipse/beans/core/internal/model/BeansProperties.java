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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProperties;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * Holds a managed {@link Properties}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansProperties extends AbstractBeansModelElement implements
		IBeansProperties {

	private Properties properties;
	private LinkedList<IBeansMapEntry> entries;

	public BeansProperties(IModelElement parent, ManagedProperties properties) {
		super(parent, "(properties)", properties);

		// Create new properties with values from given properties
		this.properties = new Properties();
		this.entries = new LinkedList<IBeansMapEntry>();
		for (Object entry : properties.entrySet()) {
			Map.Entry<?, ?> mEntry = (Map.Entry<?, ?>) entry;
			this.properties.put(mEntry.getKey(), mEntry.getValue());

			BeansMapEntry bmEntry = new BeansMapEntry(this, mEntry);
			bmEntry.setKey(BeansModelUtils.resolveValueIfNecessary(bmEntry,
					mEntry.getKey()));
			bmEntry.setValue(BeansModelUtils
					.resolveValueIfNecessary(bmEntry, mEntry.getValue()));
			entries.add(bmEntry);
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROPERTIES_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		List<IBeansMapEntry> children = new ArrayList<IBeansMapEntry>();
		for (IBeansMapEntry entry : entries) {
			children.add(entry);
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this bean
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this props' entries
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

	public Map<?, ?> getMap() {
		return properties;
	}

	public Properties getProperties() {
		return properties;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansProperties)) {
			return false;
		}
		BeansProperties that = (BeansProperties) other;
		if (!ObjectUtils.nullSafeEquals(this.properties, that.properties))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(properties);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": properties=");
		text.append(properties);
		return text.toString();
	}
}
