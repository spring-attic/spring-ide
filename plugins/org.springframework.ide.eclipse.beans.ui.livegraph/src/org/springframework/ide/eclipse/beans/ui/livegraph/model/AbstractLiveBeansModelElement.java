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
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Leo Dos Santos
 */
public abstract class AbstractLiveBeansModelElement implements IAdaptable {

	protected final Map<String, String> attributes;

	public AbstractLiveBeansModelElement() {
		this.attributes = new HashMap<String, String>();
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new LiveBeanPropertySource(this);
		}
		return null;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

}
