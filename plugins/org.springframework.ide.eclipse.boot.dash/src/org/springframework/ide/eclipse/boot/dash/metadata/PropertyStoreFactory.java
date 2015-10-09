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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.DefaultSecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.SecuredCredentialsStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;

/**
 * Provides static helper methods to create IPropertyStore instances,
 */
public class PropertyStoreFactory {

	private static final String QUALIFIER = "microservice-metadata";

	public static IScopedPropertyStore<IProject> createForProjects() {
		return new PreferenceBasedStore<IProject>() {
			protected IEclipsePreferences createPrefs(IProject p) {
				IEclipsePreferences prefs = new ProjectScope(p).getNode(QUALIFIER);
				return prefs;
			}
		};
	}

	public static IScopedPropertyStore<RunTargetType> createForRunTargets() {
		return new PreferenceBasedStore<RunTargetType>() {
			protected IEclipsePreferences createPrefs(RunTargetType runTargetType) {
				return InstanceScope.INSTANCE.getNode(BootDashActivator.PLUGIN_ID + ':' + runTargetType.getName());
			}
		};
	}

	public static SecuredCredentialsStore createSecuredCredentialsStore() {
		return new DefaultSecuredCredentialsStore();
	}

	public static <S> IPropertyStore createForScope(final S scope, final IScopedPropertyStore<S> scopedStore) {
		return new IPropertyStore() {

			@Override
			public void put(String key, String value) throws Exception {
				scopedStore.put(scope, key, value);
			}

			@Override
			public String get(String key) {
				return scopedStore.get(scope, key);
			}
		};
	}

	public static PropertyStoreApi createApi(IPropertyStore backingStore) {
		return new PropertyStoreApi(backingStore);
	}

	public static IPropertyStore createSubStore(final String subStoreId, final IPropertyStore backingStore) {
		return new IPropertyStore() {

			private String subkey(String key) {
				return subStoreId+":"+key;
			}

			@Override
			public void put(String key, String value) throws Exception {
				backingStore.put(subkey(key), value);
			}

			@Override
			public String get(String key) {
				return backingStore.get(subkey(key));
			}
		};
	}

}
