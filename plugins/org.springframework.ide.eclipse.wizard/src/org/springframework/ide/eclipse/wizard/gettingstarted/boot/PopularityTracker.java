/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Dependency;

/**
 * Component that keeps track of 'popular' dependencies used in the NewSpringBootWizard.
 * It keeps a persistent count of how many times a given dependency has been used.
 *
 * @author Kris De Volder
 */
public class PopularityTracker {

	public static final String PREFIX = PopularityTracker.class.getName()+".";

	private IPreferenceStore store;

	public PopularityTracker(IPreferenceStore store) {
		this.store = store;
	}

	protected String key(String id) {
		String key = PREFIX+id;
		return key;
	}

	public void incrementUsageCount(Dependency d) {
		incrementUsageCount(d.getId());
	}

	public int getUsageCount(Dependency d) {
		return getUsageCount(d.getId());
	}

	private int getUsageCount(String id) {
		return store.getInt(key(id));
	}

	private void incrementUsageCount(String id) {
		String key = key(id);
		store.setValue(key, store.getInt(key));
	}


}
