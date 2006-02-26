/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views.model;

import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;

public class ConstructorArgumentNode extends AbstractNode {

	public ConstructorArgumentNode(BeanNode bean,
								   IBeanConstructorArgument carg) {
		super(bean, null);
		setElement(carg);
	}

	public IBeanConstructorArgument getConstructorArgument() {
		return (IBeanConstructorArgument) getElement();
	}

	public String getName() {
		StringBuffer name = new StringBuffer();
		if (getIndex() != -1) {
			name.append(getIndex());
			name.append(':');
		}
		if (getType() != null) {
			name.append(getType());
			name.append(':');
		}
		if (getValue() == null) {
			name.append("NULL");
		} else {
			name.append(getValue().toString());
		}
		return name.toString();
	}

	public int getIndex() {
		return getConstructorArgument().getIndex();
	}

	public String getType() {
		return getConstructorArgument().getType();
	}
	
	public Object getValue() {
		return getConstructorArgument().getValue();
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

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getName());
		text.append(": index=");
		text.append(getIndex());
		text.append(", type=");
		text.append(getType());
		text.append(": value=");
		if (getValue() == null) {
			text.append("NULL");
		} else {
			text.append(getValue());
		}
		return text.toString();
	}
}
