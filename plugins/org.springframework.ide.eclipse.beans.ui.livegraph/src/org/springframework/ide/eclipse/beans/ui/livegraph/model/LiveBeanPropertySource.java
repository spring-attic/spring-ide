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
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * @author Leo Dos Santos
 */
public class LiveBeanPropertySource implements IPropertySource {

	private final AbstractLiveBeansModelElement model;

	private IPropertyDescriptor[] descriptors;

	public LiveBeanPropertySource(AbstractLiveBeansModelElement model) {
		this.model = model;
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (descriptors == null) {
			List<IPropertyDescriptor> descrList = new ArrayList<IPropertyDescriptor>();
			Map<String, String> attributes = model.getAttributes();
			Set<String> keys = attributes.keySet();
			for (String key : keys) {
				PropertyDescriptor desc = new PropertyDescriptor(key, key);
				descrList.add(desc);
			}
			descriptors = descrList.toArray(new IPropertyDescriptor[descrList.size()]);
		}
		return descriptors;
	}

	public Object getPropertyValue(Object id) {
		return model.getAttributes().get(id);
	}

	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub

	}

}
