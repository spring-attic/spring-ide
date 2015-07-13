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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Comparator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Table section column model backed by plug-in preferences.
 * Might need a super abstract class or interface in the near future.
 *
 * @author Alex Boyko
 *
 */
public class BootDashColumnModel {

	public static final Comparator<BootDashColumnModel> INDEX_COMPARATOR = new Comparator<BootDashColumnModel>() {
		@Override
		public int compare(BootDashColumnModel o1, BootDashColumnModel o2) {
			return o1.getIndex() - o2.getIndex();
		}
	};

	public static final Comparator<BootDashColumnModel> DEFAULT_INDEX_COMPARATOR = new Comparator<BootDashColumnModel>() {
		@Override
		public int compare(BootDashColumnModel o1, BootDashColumnModel o2) {
			return o1.getDefaultIndex() - o2.getDefaultIndex();
		}
	};

	private static final String PREF_KEY_SEPARATOR = "___";
	private static final String PREF_KEY_WIDTH_SUFFIX = "width";
	private static final String PREF_KEY_VISIBILITY_SUFFIX = "visibility";
	private static final String PREF_KEY_ORDER_SUFFIX = "index";

	private final BootDashColumn feature;
	private final int allignment;
	private final EditingSupportFactory editingSupport;
	private final BootDashActionFactory singleClickAction;

	private final IPreferenceStore prefStore;
	private final String prefKeysPrefix;

	public BootDashColumnModel(BootDashColumn feature,
			int defaultWidth,
			boolean defaultVisible,
			int defaultIndex,
			IPreferenceStore prefStore,
			String prefKeysPrefix,
			EditingSupportFactory editingSupport,
			BootDashActionFactory singleClickAction,
			int alignment
			) {
		super();
		this.feature = feature;
		this.allignment = alignment;
		this.editingSupport = editingSupport;
		this.singleClickAction = singleClickAction;
		this.prefStore = prefStore;
		this.prefKeysPrefix = prefKeysPrefix == null ? "" : prefKeysPrefix;
		if (prefStore != null) {
			prefStore.setDefault(getWidthPrefKey(), defaultWidth);
			prefStore.setDefault(getVisibilityPrefKey(), defaultVisible);
			prefStore.setDefault(getIndexKey(), defaultIndex);
		}
	}

	public BootDashColumnModel(BootDashColumn feature,
			int defaultWidth,
			boolean defaultVisible,
			int defaultIndex,
			IPreferenceStore prefStore,
			String prefKeysPrefix,
			EditingSupportFactory editingSupport,
			BootDashActionFactory singleClickAction
			) {
		this(feature, defaultWidth, defaultVisible, defaultIndex, prefStore, prefKeysPrefix, editingSupport, singleClickAction, SWT.LEFT);
	}

	public BootDashColumnModel(BootDashColumn feature,
			int defaultWidth,
			boolean defaultVisible,
			int defaultIndex,
			String prefKeysPrefix
			) {
		this(feature, defaultWidth, defaultVisible, defaultIndex, prefKeysPrefix, feature.getEditingSupport(), feature.getSingleClickAction());
	}

	public BootDashColumnModel(BootDashColumn column, boolean defaultVisible, int defaultIndex, String prefKeysPrefix) {
		this(column, column.getDefaultWidth(), defaultVisible, defaultIndex, prefKeysPrefix);
	}

	public BootDashColumnModel(BootDashColumn feature,
			int defaultWidth,
			boolean defaultVisible,
			int defaultIndex,
			String prefKeysPrefix,
			EditingSupportFactory editingSupport,
			BootDashActionFactory singleClickAction
			) {
		this(feature, defaultWidth, defaultVisible, defaultIndex, BootDashActivator.getDefault().getPreferenceStore(), prefKeysPrefix, editingSupport, singleClickAction);
	}

	public String getWidthPrefKey() {
		return getKey(prefKeysPrefix, feature.toString(), PREF_KEY_WIDTH_SUFFIX);
	}

	public String getVisibilityPrefKey() {
		return getKey(prefKeysPrefix, feature.toString(), PREF_KEY_VISIBILITY_SUFFIX);
	}

	public String getIndexKey() {
		return getKey(prefKeysPrefix, feature.toString(), PREF_KEY_ORDER_SUFFIX);
	}

	private static String getKey(String... tokens) {
		StringBuilder sb = new StringBuilder();
		if (tokens.length > 0) {
			sb.append(tokens[0]);
			for (int i = 1; i < tokens.length; i++) {
				sb.append(PREF_KEY_SEPARATOR);
				sb.append(tokens[i]);
			}
		}
		return sb.toString();
	}

	public int getWidth() {
		return prefStore.getInt(getWidthPrefKey());
	}

	public void setWidth(int width) {
		prefStore.setValue(getWidthPrefKey(), width);
	}

	public boolean getVisibility() {
		return prefStore.getBoolean(getVisibilityPrefKey());
	}

	public void setVisibility(boolean visibility) {
		prefStore.setValue(getVisibilityPrefKey(), visibility);
	}

	public String getLabel() {
		return feature.getLabel();
	}

	public int getDefaultWidth() {
		return prefStore.getDefaultInt(getWidthPrefKey());
	}

	public boolean getDefaultVisibility() {
		return prefStore.getDefaultBoolean(getVisibilityPrefKey());
	}

	public int getDefaultIndex() {
		return prefStore.getDefaultInt(getIndexKey());
	}

	public int getAllignment() {
		return allignment;
	}

	public EditingSupportFactory getEditingSupport() {
		return editingSupport;
	}

	public BootDashActionFactory getSingleClickActionFactory() {
		return singleClickAction;
	}

	public int getIndex() {
		return prefStore.getInt(getIndexKey());
	}

	public void setIndex(int index) {
		prefStore.setValue(getIndexKey(), index);
	}

	public BootDashColumn getType() {
		return feature;
	}

}
