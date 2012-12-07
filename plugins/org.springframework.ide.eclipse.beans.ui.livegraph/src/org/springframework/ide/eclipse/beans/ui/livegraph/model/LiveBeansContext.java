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

	private LiveBeansContext parent;

	public LiveBeansContext(String label) {
		super(label);
	}

	public LiveBeansContext getParent() {
		return parent;
	}

	public void setParent(LiveBeansContext parent) {
		this.parent = parent;
	}

}
