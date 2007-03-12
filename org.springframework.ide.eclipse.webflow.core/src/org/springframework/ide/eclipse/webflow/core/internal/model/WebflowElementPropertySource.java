/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowElementPropertySource implements IPropertySource {

	/**
	 * 
	 */
	private final IPropertySource propertySource;

	/**
	 * 
	 * 
	 * @param propertySource 
	 */
	public WebflowElementPropertySource(IPropertySource propertySource) {
		this.propertySource = propertySource;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public Object getEditableValue() {
		return propertySource.getEditableValue();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
		IPropertyDescriptor[] wrappedDescriptors = propertySource
				.getPropertyDescriptors();
		if (wrappedDescriptors != null) {
			for (IPropertyDescriptor pd : wrappedDescriptors) {
				PropertyDescriptor newPd = new PropertyDescriptor(pd.getId(),
						pd.getDisplayName());
				newPd.setCategory(pd.getCategory());
				newPd.setDescription(pd.getDescription());
				descriptors.add(newPd);
			}
		}
		return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	/**
	 * 
	 * 
	 * @param id 
	 * 
	 * @return 
	 */
	public Object getPropertyValue(Object id) {
		return propertySource.getPropertyValue(id);
	}

	/**
	 * 
	 * 
	 * @param id 
	 * 
	 * @return 
	 */
	public boolean isPropertySet(Object id) {
		return propertySource.isPropertySet(id);
	}

	/**
	 * 
	 * 
	 * @param id 
	 */
	public void resetPropertyValue(Object id) {
		propertySource.resetPropertyValue(id);
	}

	/**
	 * 
	 * 
	 * @param value 
	 * @param id 
	 */
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}
}
