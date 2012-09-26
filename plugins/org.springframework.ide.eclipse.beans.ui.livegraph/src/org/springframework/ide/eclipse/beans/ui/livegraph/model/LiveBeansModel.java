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
import java.util.List;

/**
 * A mock pre-generated beans model
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModel {

	private final List<LiveBean> beans;

	public LiveBeansModel() {
		beans = new ArrayList<LiveBean>();
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

}
