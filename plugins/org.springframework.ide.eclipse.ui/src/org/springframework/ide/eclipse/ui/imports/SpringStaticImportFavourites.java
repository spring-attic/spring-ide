/*******************************************************************************
 * Copyright (c) 2007, 2016 Spring IDE Developers, IBM Corporation, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial public API
 *     IBM Corporation and others - original implementation
 *     
 * Original License for code derived from: org.eclipse.jdt.internal.ui.preferences.CodeAssistFavoritesConfigurationBlock:
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.imports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock.Key;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Loads Types containing statics into Eclipse Static Import favourites
 * preferences.
 * <p/>
 * NOTE: Code derived from:
 * <p/>
 * org.eclipse.jdt.internal.ui.preferences.CodeAssistFavoritesConfigurationBlock
 * <p/>
 *
 */
public class SpringStaticImportFavourites {
	private final Key PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS = getJDTUIKey(
			PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);

	private static final String WILDCARD = ".*";

	private static IScopeContext[] DEFAULT_ORDER = new IScopeContext[] { InstanceScope.INSTANCE,
			DefaultScope.INSTANCE };

	private IScopeContext[] lookUpOrder = DEFAULT_ORDER;

	private WorkingCopyManager manager;

	private final StaticImportCatalogue catalogue;

	public SpringStaticImportFavourites(StaticImportCatalogue catalogue) {
		this.catalogue = catalogue;
		manager = new WorkingCopyManager();
	}

	/**
	 * load the static imports into Eclipse favourites preferences
	 * asynchronously
	 */
	public void asynchLoad() {

		Job job = new Job("Loading Spring static imports into Eclipse code assist favourites") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				load();
				return Status.OK_STATUS;
			}
		};

		job.schedule();
	}

	public void load() {
		List<String> customFavourites = loadAndValidateFromCatalogue();
		setValue(customFavourites);
		applyChanges();
	}

	protected Key getKey(String plugin, String key) {
		return new Key(plugin, key);
	}

	protected final void setValue(List<String> favorites) {
		setValue(PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS, serializeFavorites(favorites));

	}

	protected String getValue(Key key) {
		return key.getStoredValue(getLookupOrder(), false, manager);
	}

	protected IScopeContext[] getLookupOrder() {
		return this.lookUpOrder;
	}

	protected String setValue(Key key, String value) {
		String oldValue = getValue(key);
		key.setStoredValue(getLookupOrder()[0], value, manager);
		return oldValue;
	}

	public String[] getFromPreferences() {
		String str = getValue(PREF_CODEASSIST_FAVORITE_STATIC_MEMBERS);
		if (str != null && str.length() > 0) {
			return deserializeFavorites(str);
		}
		return new String[0];
	}

	private String[] deserializeFavorites(String str) {
		return str.split(";");
	}

	protected void applyChanges() {
		try {
			manager.applyChanges();
		} catch (BackingStoreException e) {
			SpringUIPlugin.log(e);
		}
	}

	private static String serializeFavorites(List<String> favorites) {
		int size = favorites.size();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < size; i++) {
			buf.append(favorites.get(i));
			if (i < size - 1) {
				buf.append(';');
			}
		}
		return buf.toString();
	}

	protected final Key getJDTUIKey(String key) {
		return getKey(JavaUI.ID_PLUGIN, key);
	}

	protected List<String> mergeWithExisting(List<String> favorites) {

		String[] existing = getFromPreferences();
		List<String> merged = new ArrayList<String>(Arrays.asList(existing));
		for (String fav : favorites) {
			if (!merged.contains(fav)) {
				merged.add(fav);
			}
		}
		return merged;
	}

	protected List<String> loadAndValidateFromCatalogue() {
		List<String> validated = getValidated(catalogue.getCatalogue());
		return mergeWithExisting(validated);
	}

	protected List<String> getValidated(String[] original) {

		List<String> validated = new ArrayList<String>();
		for (String val : original) {
			IStatus status = JavaConventions.validateJavaTypeName(val, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
			if (status.isOK()) {
				validated.add(asWildCard(val));
			} else {
				SpringUIPlugin.log(status);
			}
		}

		return validated;
	}

	protected String asWildCard(String val) {
		return val + WILDCARD;
	}

}
