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
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AttributeMapper extends AbstractModelElement implements
		IAttributeMapper, ICloneableModelElement<IAttributeMapper> {

	/**
	 * The input mapper.
	 */
	private IInputMapper inputMapper;

	/**
	 * The output mapper.
	 */
	private IOutputMapper outputMapper;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.AbstractModelElement#init(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
	 * org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param node 
	 * @param parent 
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("input-mapper".equals(child.getLocalName())) {
					this.inputMapper = new InputMapper();
					this.inputMapper.init(child, this);
				}
				else if ("output-mapper".equals(child.getLocalName())) {
					this.outputMapper = new OutputMapper();
					this.outputMapper.init(child, this);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#getBean()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getBean() {
		return getAttribute("bean");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#getInputMapper()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public IInputMapper getInputMapper() {
		return this.inputMapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#setBean(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param bean 
	 */
	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#getOutputMapper()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public IOutputMapper getOutputMapper() {
		return this.outputMapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#setOutputMapper(org.springframework.ide.eclipse.webflow.core.model.IOutputMapper)
	 */
	/**
	 * 
	 * 
	 * @param outputMapper 
	 */
	public void setOutputMapper(IOutputMapper outputMapper) {
		if (this.outputMapper != null) {
			getNode().removeChild(this.outputMapper.getNode());
		}
		this.outputMapper = outputMapper;
		if (outputMapper != null) {
			WebflowModelUtils.insertNode(outputMapper.getNode(), getNode());
		}
		super.fireStructureChange(ADD_CHILDREN, outputMapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#setInputMapper(org.springframework.ide.eclipse.webflow.core.model.IInputMapper)
	 */
	/**
	 * 
	 * 
	 * @param inputMapper 
	 */
	public void setInputMapper(IInputMapper inputMapper) {
		if (this.inputMapper != null) {
			getNode().removeChild(this.inputMapper.getNode());
		}
		this.inputMapper = inputMapper;
		if (inputMapper != null) {
			WebflowModelUtils.insertNode(inputMapper.getNode(), getNode());
		}
		super.fireStructureChange(ADD_CHILDREN, inputMapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement#cloneModelElement()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public IAttributeMapper cloneModelElement() {
		AttributeMapper state = new AttributeMapper();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement#applyCloneValues(org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param element 
	 */
	public void applyCloneValues(IAttributeMapper element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setBean(element.getBean());
			init(element.getNode(), parent);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper#createNew(org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * 
	 * 
	 * @param parent 
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("attribute-mapper");
		init(node, parent);
	}
}