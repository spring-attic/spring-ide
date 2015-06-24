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

import java.util.ArrayList;
import java.util.List;

public abstract class WrappingBootDashElement<T> implements TaggableBootDashElement {

	protected final T delegate;
	
	private List<TagsChangedListener> tagsListeners;

	public WrappingBootDashElement(T delegate) {
		this.delegate = delegate;
		this.tagsListeners = new ArrayList<TagsChangedListener>();
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

	@Override
	public void addListener(TagsChangedListener listener) {
		tagsListeners.add(listener);
	}

	@Override
	public void removeListener(TagsChangedListener listener) {
		tagsListeners.remove(listener);
	}
	
	protected void notifyTagsChanged(String[] newTags, String[] oldTags) {
		for (TagsChangedListener listener : tagsListeners) {
			listener.tagsChanged(this, newTags, oldTags);
		}
	}

}
