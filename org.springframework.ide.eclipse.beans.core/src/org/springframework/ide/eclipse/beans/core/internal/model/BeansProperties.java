/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
 * @author Torsten Juergeleit
 * @author Christian Dupuis
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
