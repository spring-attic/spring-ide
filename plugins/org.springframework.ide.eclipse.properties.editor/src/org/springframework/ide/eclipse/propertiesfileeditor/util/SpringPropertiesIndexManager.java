/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap;
import org.springframework.ide.eclipse.propertiesfileeditor.PropertyInfo;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertyIndex;
import org.springframework.ide.eclipse.propertiesfileeditor.util.ClasspathListenerManager.ClasspathListener;

/**
 * Support for Reconciling, Content Assist and Hover Text in spring properties
 * file all make use of a per-project index of spring properties metadata extracted
 * from project's classpath. This Index manager is responsible for keeping at most
 * one index per-project and to keep the index up-to-date.
 *  
 * @author Kris De Volder
 */
public class SpringPropertiesIndexManager implements ClasspathListener {
	
	private Map<String, FuzzyMap<PropertyInfo>> indexes = null;
	
	public SpringPropertiesIndexManager() {
		SpringPropertiesEditorPlugin.getClasspathListeners().add(this);
	}

	public synchronized FuzzyMap<PropertyInfo> get(IJavaProject jp) {
		String key = jp.getElementName();
		if (indexes==null) {
			indexes = new HashMap<String, FuzzyMap<PropertyInfo>>();
		}
		FuzzyMap<PropertyInfo> index = indexes.get(key);
		if (index==null) {
			index = new SpringPropertyIndex(jp);
			indexes.put(key, index);
		}
		return index;
	}
	
	@Override
	public synchronized void classpathChanged(IJavaProject jp) {
		indexes.remove(jp.getElementName());
	}

}
