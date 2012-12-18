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

/**
 * @author Leo Dos Santos
 */
public class LiveBeansContext extends LiveBeansGroup {

	public static final String ATTR_CONTEXT = "context";

	public static final String ATTR_PARENT = "parent";

	public static final String ATTR_BEANS = "beans";

	private String displayName;

	private LiveBeansContext parent;

	public LiveBeansContext(String label) {
		super(label);
		attributes.put(ATTR_CONTEXT, label);
	}

	@Override
	public String getDisplayName() {
		// compute the display name the first time it's needed
		if (displayName == null) {
			String label = getLabel();
			int indexStart = label.lastIndexOf(":");
			if (indexStart > -1 && indexStart < label.length()) {
				displayName = label.substring(indexStart + 1, label.length());
			}
			if (displayName == null) {
				displayName = label;
			}
		}
		return displayName;
	}

	public LiveBeansContext getParent() {
		return parent;
	}

	public void setParent(LiveBeansContext parent) {
		this.parent = parent;
		attributes.put(ATTR_PARENT, parent.getLabel());
	}

}
