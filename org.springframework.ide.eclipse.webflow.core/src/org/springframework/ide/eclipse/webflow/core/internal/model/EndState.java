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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class EndState extends AbstractTransitionableTo implements IEndState,
		ICloneableModelElement<IEndState> {

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

		if (node != null) {
			NodeList children = node.getChildNodes();
			if (children != null && children.getLength() > 0) {
				for (int i = 0; i < children.getLength(); i++) {
					IDOMNode child = (IDOMNode) children.item(i);
					if ("output-mapper".equals(child.getLocalName())) {
						this.outputMapper = new OutputMapper();
						this.outputMapper.init(child, this);
					}
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String getView() {
		return getAttribute("view");
	}

	/**
	 * 
	 * 
	 * @param view
	 */
	public void setView(String view) {
		setAttribute("view", view);
	}

	/**
	 * 
	 * 
	 * @param parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("end-state");
		init(node, parent);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IEndState cloneModelElement() {
		EndState state = new EndState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * 
	 * 
	 * @param element
	 */
	public void applyCloneValues(IEndState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setId(element.getId());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
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

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			if (getEntryActions() != null) {
				getEntryActions().accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			for (IExceptionHandler state : getExceptionHandlers()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
}