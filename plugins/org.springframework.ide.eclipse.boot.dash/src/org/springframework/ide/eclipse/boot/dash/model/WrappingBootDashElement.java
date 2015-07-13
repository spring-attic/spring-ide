/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.TypeLookup;

public abstract class WrappingBootDashElement<T> implements BootDashElement {

	public static final String TAGS_KEY = "tags";

	protected final T delegate;

	private BootDashModel parent;
	private TypeLookup typeLookup;

	public WrappingBootDashElement(BootDashModel parent, T delegate) {
		this.parent = parent;
		this.delegate = delegate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((delegate == null) ? 0 : delegate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		WrappingBootDashElement other = (WrappingBootDashElement) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return delegate.toString();
	}

	protected TypeLookup getTypeLookup() {
		if (typeLookup==null) {
			typeLookup = new TypeLookup() {
				public IType findType(String fqName) {
					try {
						IJavaProject jp = getJavaProject();
						if (jp!=null) {
							return jp.findType(fqName, new NullProgressMonitor());
						}
					} catch (Exception e) {
						BootDashActivator.log(e);
					}
					return null;
				}
			};
		}
		return typeLookup;
	}

	public abstract PropertyStoreApi getPersistentProperties();

	@Override
	public LinkedHashSet<String> getTags() {
		try {
			String[] tags = getPersistentProperties().get(TAGS_KEY, (String[])null);
			if (tags!=null) {
				return new LinkedHashSet<String>(Arrays.asList(tags));
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return new LinkedHashSet<String>();
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {
		try {
			if (newTags==null || newTags.isEmpty()) {
				getPersistentProperties().put(TAGS_KEY, (String[])null);
			} else {
				getPersistentProperties().put(TAGS_KEY, newTags.toArray(new String[newTags.size()]));
			}
			parent.notifyElementChanged(this);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	public BootDashModel getParent() {
		return parent;
	}

}
