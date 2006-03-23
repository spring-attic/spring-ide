/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIException;

/**
 * This class is a helper for dealing with plugin preferences.
 * 
 * @author Pierre-Antoine Grégoire
 */
public class PreferencesFacade {
	private static Map defaultValues = null;

	public static void setDefaultValues(AbstractUIPlugin plugin, HashMap map) {
		defaultValues = map;
		initDefaultValues(plugin, true);
	}

	private static boolean defaultsSet = false;

	public static Object getPreference(AbstractUIPlugin plugin, String propertyName, Class type) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		Object result = null;
		if (type.equals(String.class)) {
			result = plugin.getPreferenceStore().getString(propertyName);
		}
		if (type.equals(Boolean.class)) {
			result = new Boolean(plugin.getPreferenceStore().getBoolean(propertyName));
		}
		if (type.equals(Integer.class)) {
			result = new Integer(plugin.getPreferenceStore().getInt(propertyName));
		}
		if (type.equals(Double.class)) {
			result = new Double(plugin.getPreferenceStore().getDouble(propertyName));
		}
		if (type.equals(Float.class)) {
			result = new Float(plugin.getPreferenceStore().getFloat(propertyName));
		}
		if (type.equals(Long.class)) {
			result = new Long(plugin.getPreferenceStore().getLong(propertyName));
		}
		return result;
	}

	public static void setPreference(AbstractUIPlugin plugin, String propertyName, Object value) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		Class type = value.getClass();
		if (type.equals(String.class)) {
			plugin.getPreferenceStore().setValue(propertyName, (String) value);
		}
		if (type.equals(Boolean.class)) {
			plugin.getPreferenceStore().setValue(propertyName, ((Boolean) value).booleanValue());
		}
		if (type.equals(Integer.class)) {
			plugin.getPreferenceStore().setValue(propertyName, ((Integer) value).intValue());
		}
		if (type.equals(Double.class)) {
			plugin.getPreferenceStore().setValue(propertyName, ((Double) value).doubleValue());
		}
		if (type.equals(Float.class)) {
			plugin.getPreferenceStore().setValue(propertyName, ((Float) value).floatValue());
		}
		if (type.equals(Long.class)) {
			plugin.getPreferenceStore().setValue(propertyName, ((Long) value).longValue());
		}
	}

	public static Object getDefaultPreferenceValue(String propertyName) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		return defaultValues.get(propertyName);
	}

	public static void setToDefaultValues(AbstractUIPlugin plugin) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		initDefaultValues(plugin, false);
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		for (Iterator it = defaultValues.keySet().iterator(); it.hasNext();) {
			preferenceStore.setToDefault((String) it.next());
		}
	}

	public static void setToDefaultValue(AbstractUIPlugin plugin, String propertyName) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		initDefaultValues(plugin, false);
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		preferenceStore.setToDefault(propertyName);
	}

	public static void initDefaultValues(AbstractUIPlugin plugin, boolean force) {
		if (defaultValues == null) {
			throw new SpringCoreUIException("Default values' map should have been set at least one time");
		}
		if ((!defaultsSet) || force) {
			IPreferenceStore preferenceStore = plugin.getPreferenceStore();
			String key = null;
			Object value = null;
			for (Iterator it = defaultValues.keySet().iterator(); it.hasNext();) {
				key = (String) it.next();
				value = defaultValues.get(key);
				if (value.getClass().equals(String.class)) {
					preferenceStore.setDefault(key, (String) value);
				}
				if (value.getClass().equals(Boolean.class)) {
					preferenceStore.setDefault(key, ((Boolean) value).booleanValue());
				}
				if (value.getClass().equals(Integer.class)) {
					preferenceStore.setDefault(key, ((Integer) value).intValue());
				}
				if (value.getClass().equals(Double.class)) {
					preferenceStore.setDefault(key, ((Double) value).doubleValue());
				}
				if (value.getClass().equals(Float.class)) {
					preferenceStore.setDefault(key, ((Float) value).floatValue());
				}
				if (value.getClass().equals(Long.class)) {
					preferenceStore.setDefault(key, ((Long) value).longValue());
				}
			}
			defaultsSet = true;
		}
	}
}