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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A model of a running Spring application to be graphed in the Live Beans Graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModel implements Comparable<LiveBeansModel> {

	private final List<LiveBean> beans;

	private final LiveBeansSession session;

	public LiveBeansModel(LiveBeansSession session) {
		this.beans = new ArrayList<LiveBean>();
		this.session = session;
	}

	public void addBeans(Collection<LiveBean> beansToAdd) {
		beans.addAll(beansToAdd);
	}

	public int compareTo(LiveBeansModel o) {
		return getApplicationName().compareTo(o.getApplicationName());
	}

	public String getApplicationName() {
		if (session != null) {
			session.getApplicationName();
		}
		return "";
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

	public LiveBeansSession getSession() {
		return session;
	}

}
