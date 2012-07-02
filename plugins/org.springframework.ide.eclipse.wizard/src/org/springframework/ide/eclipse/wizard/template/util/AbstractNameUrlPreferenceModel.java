/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/

/*
 * @author Kaitlin Duck Sherwood
 */
package org.springframework.ide.eclipse.wizard.template.util;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;

public abstract class AbstractNameUrlPreferenceModel {

	private final IEclipsePreferences store;

	private final IEclipsePreferences defaultStore;

	private String currentString;

	protected boolean optionalFlagValue;

	private final List<PropertyChangeListener> listeners;

	protected abstract IEclipsePreferences getStore();

	protected abstract String getStoreKey();

	protected abstract String getDefaultFilename();

	protected abstract IEclipsePreferences getDefaultStore();

	public AbstractNameUrlPreferenceModel() {
		listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

		store = getStore();
		defaultStore = getDefaultStore();

		if (store == null) {
			String title = NLS.bind("Serious error", null);
			String message = NLS
					.bind("This is a serious error: we didn't think this could happen.  Please tell the STS team that there was no preferences store for the Help bundle."
							+ "The issue tracker is at https://issuetracker.springsource.com/browse/STS", null);
			MessageDialog.openError(null, title, message);
		}

		// You always want the defaults set;
		// you might not want to overwrite the store
		setDefaults();

		// side effect: if the instance store is empty, set the instance store
		// to the default values
		revert();

		// If don't persist it here, it isn't available to the ResourceProvider
		persist();
	}

	public void revert() {
		currentString = store.get(getStoreKey(), null);
		if (currentString == null) {
			currentString = defaultStore.get(getStoreKey(), "");
		}
		// don't need to be as careful with the optional flag because
		// its default is something reasonable; also it can't be set to
		// null to show that it has not been set
	}

	// This initializes the defaults; it is NOT ever called by the user
	// or even reached by something the user does.
	protected void setDefaults() {
		InputStream defaultsStream;
		try {
			defaultsStream = FileLocator.openStream(WizardPlugin.getDefault().getBundle(), new Path(
					getDefaultFilename()), false);
			Properties urlProperties = new Properties();
			urlProperties.load(defaultsStream);

			String encodedString = "";
			for (Object key : urlProperties.keySet()) {
				if (key instanceof String && urlProperties.get(key) instanceof String) {
					try {
						encodedString = encodedString
								+ (new NameUrlPair((String) key, urlProperties.getProperty((String) key)))
										.asCombinedString();
					}
					catch (URISyntaxException e) {
						String title = NLS.bind("Malformed URL", null);
						String message = NLS.bind("The {0} is not a legal URL; ignoring.", urlProperties.get(key));
						MessageDialog.openError(null, title, message);
					}
				}
			}
			defaultStore.put(getStoreKey(), encodedString);
		}
		catch (IOException e1) {
			String title = NLS.bind("Could not read defaults", null);
			String message = NLS.bind(
					"Could not read defaults from file {0}; check that the file exists and is readable",
					getDefaultFilename());
			MessageDialog.openError(null, title, message);
		}
	}

	public ArrayList<NameUrlPair> getElements() {
		if (currentString == null) {
			currentString = defaultStore.get(getStoreKey(), "");
		}
		return NameUrlPair.decodeMultipleNameUrlStrings(currentString);
	}

	public String replaceNameUrlPairInEncodedString(NameUrlPair oldNameUrl, NameUrlPair newNameUrl) {
		currentString = currentString.replaceFirst(oldNameUrl.asCombinedString(), newNameUrl.asCombinedString());
		return currentString;
	}

	public String removeNameUrlPairInEncodedString(NameUrlPair oldNameUrl) {
		String combinedString = oldNameUrl.asCombinedString();
		String adjustedString = Pattern.quote(combinedString);

		currentString = currentString.replaceFirst(adjustedString, "");
		return currentString;
	}

	public String addNameUrlPairInEncodedString(NameUrlPair newNameUrl) {
		currentString = currentString + newNameUrl.asCombinedString();
		return currentString;
	}

	public boolean persist() {
		boolean didChangeFlag = false;

		normalizeCurrentStringOrder();

		if (!currentString.equals(store.get(getStoreKey(), null))) {
			Assert.isNotNull(currentString, "INTERNAL ERROR: current string should not be null in " + this.getClass());
			store.put(getStoreKey(), currentString);
			didChangeFlag = true;
		}

		boolean oldValue = getStore().getBoolean(getStoreOptionalFlagKey(), optionalFlagDefault());
		boolean newValue = getOptionalFlagValue();
		if (oldValue != newValue) {
			didChangeFlag = true;
			getStore().putBoolean(getStoreOptionalFlagKey(), getOptionalFlagValue());
		}

		if (didChangeFlag) {
			ContentPlugin.getDefault().getManager().setDirty();
		}
		return didChangeFlag;
	}

	private void normalizeCurrentStringOrder() {
		List<NameUrlPair> sortedItems = NameUrlPair.decodeMultipleNameUrlStrings(currentString);
		Collections.sort(sortedItems, new Comparator<NameUrlPair>() {
			public int compare(NameUrlPair o1, NameUrlPair o2) {
				return o1.getUrlString().compareTo(o2.getUrlString());
			}
		});
		currentString = "";
		for (NameUrlPair nameUrlPair : sortedItems) {
			currentString += nameUrlPair.asCombinedString();
		}
	}

	protected void clearNonDefaults() {
		currentString = defaultStore.get(getStoreKey(), null);
	}

	protected abstract String getStoreOptionalFlagKey();

	protected boolean optionalFlagDefault() {
		return false;
	}

	// The templates need somewhere to keep a boolean of whether or
	// not to show self-hosted templates in the New Template Wizard.
	protected void setOptionalFlagValue(boolean flagValue) {
		optionalFlagValue = flagValue;
	}

	protected boolean getOptionalFlagValue() {
		return optionalFlagValue;
	}

	public void addListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

}
