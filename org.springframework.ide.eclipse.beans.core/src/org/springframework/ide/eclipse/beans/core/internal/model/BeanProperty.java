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

package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.PropertyValue;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;

/**
 * This class defines a property within a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 */
public class BeanProperty extends AbstractBeansModelElement implements
		IBeanProperty {

	private Object value;

	public BeanProperty(IBean bean, PropertyValue propValue) {
		super(bean, propValue.getName(), propValue);
		value = propValue.getValue();
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROPERTY_TYPE;
	}
	
	public Object getValue() {
		return value;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanProperty)) {
			return false;
		}
		BeanProperty that = (BeanProperty) other;
		if (!ObjectUtils.nullSafeEquals(this.value, that.value)) return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(value);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": value=");
		text.append(value);
		return text.toString();
	}
}
