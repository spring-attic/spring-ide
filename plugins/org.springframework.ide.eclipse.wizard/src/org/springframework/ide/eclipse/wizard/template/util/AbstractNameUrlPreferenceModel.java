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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.wizard.WizardPlugin;

public abstract class AbstractNameUrlPreferenceModel {

	private final IEclipsePreferences store;

	private final IEclipsePreferences defaultStore;

	private String currentString;

	private boolean didChangeFlag = false;

	protected abstract IEclipsePreferences getStore();

	protected abstract String getStoreKey();

	protected abstract String getDefaultFilename();

	protected abstract IEclipsePreferences getDefaultStore();

	public AbstractNameUrlPreferenceModel() {
		store = getStore();
		defaultStore = getDefaultStore();

		if (store == null) {
			String title = NLS.bind("Serious error", null);
			String message = NLS
					.bind("This is a serious error: we didn't think this could happen.  Please tell the STS team that there was no preferences store for the Help bundle."
							+ "The issue tracker is at https://issuetracker.springsource.com/browse/STS", null);
			MessageDialog.openError(null, title, message);
		}
		setDefaults();
		revert();
	}

	public void revert() {
		currentString = store.get(getStoreKey(), null);
		if (currentString == null) {
			currentString = defaultStore.get(getStoreKey(), "");
		}
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

	// sets didChange as a side effect: we need to get that information
	// back up to the preferences page
	public boolean persist() {
		didChangeFlag = false;
		if (!currentString.equals(store.get(getStoreKey(), null))) {
			Assert.isNotNull(currentString, "INTERNAL ERROR: current string should not be null in " + this.getClass());
			store.put(getStoreKey(), currentString);
			didChangeFlag = true;
		}
		return false;
	}

	// We need a way to get information back to the templates preference page so
	// it can kick off updating the descriptors. You can perhaps imagine
	// that this flag would be a little scary, that it could get stuck "on" or
	// "off". However, the only method that does anything with the flag is
	// TemplatesPreferencePage.performOk(), so there's not a danger of a race.
	// (ExamplesPreferencePage ignores it.) IF YOU CHANGE SOMETHING SO THAT
	// MORE THAN ONE METHOD LOOKS AT/MODIFIES didChangeFlag, you might need
	// to use a different method to kick off updating the descriptors.
	public boolean getAndClearChangedFlag() {
		boolean oldValue = didChangeFlag;
		didChangeFlag = false;
		return oldValue;
	}

	protected void clearNonDefaults() {
		currentString = defaultStore.get(getStoreKey(), null);
	}

}
