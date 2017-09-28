/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class LiveBeansGroup extends AbstractLiveBeansModelElement {

	private final String label;

	private final List<LiveBean> beans;

	public LiveBeansGroup(String label) {
		super();
		this.label = label;
		beans = new ArrayList<LiveBean>();
	}

	public void addBean(LiveBean bean) {
		beans.add(bean);
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

	public String getDisplayName() {
		return getLabel();
	}

	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LiveBeansGroup) {
			LiveBeansGroup other = (LiveBeansGroup) obj;
			return Objects.equals(label, other.label)
					&& Objects.equals(attributes, other.attributes)
					&& Objects.equals(beans, other.beans);
		}
		return super.equals(obj);
	}
	
	

}
