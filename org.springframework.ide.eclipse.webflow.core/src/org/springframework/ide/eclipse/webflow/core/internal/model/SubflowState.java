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
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class SubflowState extends AbstractTransitionableFrom implements
		ISubflowState, ICloneableModelElement<ISubflowState> {

	/**
	 * The attribute mapper.
	 */
	private IAttributeMapper attributeMapper;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.AbstractTransitionableFrom#init(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
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
				if ("attribute-mapper".equals(child.getLocalName())) {
					this.attributeMapper = new AttributeMapper();
					this.attributeMapper.init(child, this);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#getAttributeMapper()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ISubFlowState#getAttributeMapper()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public IAttributeMapper getAttributeMapper() {
		return this.attributeMapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#setAttributeMapper(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ISubFlowState#setAttributeMapper(org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper)
	 */
	/**
	 * 
	 * 
	 * @param attributeMapper 
	 */
	public void setAttributeMapper(IAttributeMapper attributeMapper) {
		IAttributeMapper oldValue = this.attributeMapper;
		if (this.attributeMapper != null) {
			getNode().removeChild(this.attributeMapper.getNode());
		}
		this.attributeMapper = attributeMapper;
		if (attributeMapper != null) {
			WebflowModelXmlUtils.insertNode(attributeMapper.getNode(), getNode());
		}
		super.firePropertyChange(ADD_CHILDREN, new Integer(0), oldValue);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ISubFlowState#removeAttributeMapper()
	 */
	/**
	 * 
	 */
	public void removeAttributeMapper() {
		IAttributeMapper oldValue = this.attributeMapper;
		if (this.attributeMapper != null) {
			getNode().removeChild(this.attributeMapper.getNode());
		}
		this.attributeMapper = null;
		super.firePropertyChange(REMOVE_CHILDREN, attributeMapper, oldValue);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#setFlow(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ISubFlowState#setFlow(java.lang.String)
	 */
	/**
	 * 
	 * 
	 * @param flow 
	 */
	public void setFlow(String flow) {
		setAttribute("flow", flow);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#getFlow()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.ISubFlowState#getFlow()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public String getFlow() {
		return getAttribute("flow");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IState#createNew(org.springframework.ide.eclipse.webflow.core.model.IWebflowState)
	 */
	/**
	 * 
	 * 
	 * @param parent 
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("subflow-state");
		init(node, parent);
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
	public ISubflowState cloneModelElement() {
		SubflowState state = new SubflowState();
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
	public void applyCloneValues(ISubflowState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setId(element.getId());
			setFlow(element.getFlow());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
	}
}