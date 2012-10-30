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

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansModelCollection {

	private static LiveBeansModelCollection instance;

	public static LiveBeansModelCollection getInstance() {
		if (instance == null) {
			instance = new LiveBeansModelCollection();
		}
		return instance;
	}

	private final Set<LiveBeansModel> collection;

	private LiveBeansModelCollection() {
		collection = new TreeSet<LiveBeansModel>();
	}

	public void addModel(LiveBeansModel model) {
		if (collection.contains(model)) {
			collection.remove(model);
		}
		collection.add(model);
	}

	public Set<LiveBeansModel> getCollection() {
		return collection;
	}

}
