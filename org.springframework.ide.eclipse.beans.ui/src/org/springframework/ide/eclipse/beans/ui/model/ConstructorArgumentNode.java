/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConstructorArgumentProperties;

public class ConstructorArgumentNode extends AbstractNode {

	private int index;
	private String type;
	private Object value;

	public ConstructorArgumentNode(BeanNode bean, int index, String type,
								 Object value) {
		super(bean, null);
		this.index = index;
		this.type = type;
		this.value = value;
	}

	public ConstructorArgumentNode(BeanNode bean,
								 ConstructorArgumentNode carg) {
		super(bean, null);
		setStartLine(carg.getStartLine()); 
		this.index = carg.getIndex();
		this.type = carg.getType();
		this.value = carg.getValue();
	}

	public String getName() {
		StringBuffer name = new StringBuffer();
		if (index != -1) {
			name.append(index);
			name.append(':');
		}
		if (type != null) {
			name.append(type);
			name.append(':');
		}
		name.append(value.toString());
		return name.toString();
	}

	public int getIndex() {
		return index;
	}

	public String getType() {
		return type;
	}
	
	public Object getValue() {
		return value;
	}

	/**
	 * Returns list of beans which are referenced from within this constructor
	 * argument.
	 */
	public List getReferencedBeans() {
		List beans = new ArrayList();
		ModelUtil.addReferencedBeansForValue(getParent().getParent(), value,
										   beans);
		return beans;
	}

	/**
	 * Returns the <code>ConfigNode</code> containing the bean this constructor
	 * argument belongs to.
	 * This method is equivalent to calling <code>getParent().getParent()</code>
	 * and casting the result to a <code>ConfigNode</code>.
	 * 
	 * @return ConfigNode the project containing this bean
	 */
	public ConfigNode getConfigNode() {
		return ((BeanNode) getParent()).getConfigNode();
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ConstructorArgumentProperties(this);
		}
		return null;
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getName());
		text.append(": index=");
		text.append(index);
		text.append(", type=");
		text.append(type);
		text.append(": value=");
		text.append(value);
		return text.toString();
	}
}
