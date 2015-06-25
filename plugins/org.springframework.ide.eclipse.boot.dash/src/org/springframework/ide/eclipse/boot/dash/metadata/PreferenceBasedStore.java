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
package org.springframework.ide.eclipse.boot.dash.metadata;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * An abstract base class to create {@link IPropertyStore} that uses
 * {@link IEclipsePreferences} to persist properties.
 */
public abstract class PreferenceBasedStore<T> implements IPropertyStore<T> {

	protected abstract IEclipsePreferences createPrefs(T element);

	@Override
	public String get(T p, String k) {
		IEclipsePreferences prefs = createPrefs(p);
		return prefs.get(k, null);
	}

	@Override
	public void put(T element, String key, String value) throws Exception {
		IEclipsePreferences prefs = createPrefs(element);
		if (value==null) {
			prefs.remove(key);
		} else {
			prefs.put(key, value);
		}
		prefs.flush();
	}

}
