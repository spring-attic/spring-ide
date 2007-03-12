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

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Attribute extends WebflowModelElement implements IAttribute {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#getName()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getName() {
		return getAttribute("name");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#getType()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getType() {
		return getAttribute("type");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#getValue()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getValue() {
		return getAttribute("value");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#setName(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param name 
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#setType(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param type 
	 */
	public void setType(String type) {
		setAttribute("type", type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#setValue(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param value 
	 */
	public void setValue(String value) {
		setAttribute("value", value);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttribute#createNew(org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param parent 
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("attribute");
		init(node, parent);
	}
}