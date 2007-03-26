/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
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

	/**
	 * 
	 * 
	 * @return
	 */
	public String getBean() {
		return getAttribute("bean");
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IInputMapper getInputMapper() {
		return this.inputMapper;
	}

	/**
	 * 
	 * 
	 * @param bean
	 */
	public void setBean(String bean) {
		setAttribute("bean", bean);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IOutputMapper getOutputMapper() {
		return this.outputMapper;
	}

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
			WebflowModelXmlUtils.insertNode(outputMapper.getNode(), getNode());
		}
		super.fireStructureChange(ADD_CHILDREN, outputMapper);
	}

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
			WebflowModelXmlUtils.insertNode(inputMapper.getNode(), getNode());
		}
		super.fireStructureChange(ADD_CHILDREN, inputMapper);
	}

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

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			if (getInputMapper() != null) {
				getInputMapper().accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			if (getOutputMapper() != null) {
				getOutputMapper().accept(visitor, monitor);
			}
		}
	}
}
