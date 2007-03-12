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

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
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
public class DecisionState extends AbstractTransitionableFrom implements
		IDecisionState, ICloneableModelElement<IDecisionState> {

	/**
	 * The ifs.
	 */
	private List<IIf> ifs = null;

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
		init(node, parent, true);
	}

	/**
	 * Init.
	 * 
	 * @param initIfs the init ifs
	 * @param node the node
	 * @param parent the parent
	 */
	public void init(IDOMNode node, IWebflowModelElement parent, boolean initIfs) {
		super.init(node, parent);

		if (initIfs) {
			this.ifs = new ArrayList<IIf>();
			initIfs(node, parent);
		}
	}

	/**
	 * Inits the ifs.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	private void initIfs(IDOMNode node, IWebflowModelElement parent) {
		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("if".equals(child.getLocalName())) {
					If if_ = new If((IWebflowState) parent);
					if_.init(child, this);
					ifs.add(if_);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#getIfs()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IDecisionState#getIfs()
	 */
	/**
	 * 
	 * 
	 * @return 
	 */
	public List<IIf> getIfs() {
		return this.ifs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.web.flow.core.model.IIf)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.webflow.core.model.IIf)
	 */
	/**
	 * 
	 * 
	 * @param theIf 
	 */
	public void addIf(IIf theIf) {
		if (!this.ifs.contains(theIf)) {
			this.ifs.add(theIf);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.ifs
					.indexOf(theIf)), theIf);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.web.flow.core.model.IIf,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IDecisionState#addIf(org.springframework.ide.eclipse.webflow.core.model.IIf,
	 * int)
	 */
	/**
	 * 
	 * 
	 * @param i 
	 * @param theIf 
	 */
	public void addIf(IIf theIf, int i) {
		if (!this.ifs.contains(theIf)) {
			this.ifs.add(i, theIf);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), theIf);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IDecisionState#removeIf(org.springframework.ide.eclipse.web.flow.core.model.IIf)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IDecisionState#removeIf(org.springframework.ide.eclipse.webflow.core.model.IIf)
	 */
	/**
	 * 
	 * 
	 * @param theIf 
	 */
	public void removeIf(IIf theIf) {
		if (this.ifs.contains(theIf)) {
			this.ifs.remove(theIf);
			getNode().removeChild(theIf.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, theIf);
		}
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
				.createElement("decision-state");
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
	public IDecisionState cloneModelElement() {
		DecisionState state = new DecisionState();
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
	public void applyCloneValues(IDecisionState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}

			setId(element.getId());
			init(element.getNode(), parent, false);

			// if (this.node.getParentNode() != null) {
			if (this.ifs != null && this.ifs.size() > 0) {
				for (int i = 0; i < this.ifs.size(); i++) {
					this.ifs.get(i).setTest(element.getIfs().get(i).getTest());
				}
			}
			// }

			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
	}
}