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
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IInputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IOutputMapper;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowState extends AbstractTransitionableFrom implements
		IWebflowState, ICloneableModelElement<IWebflowState> {

	/**
	 * The imports.
	 */
	private List<IImport> imports;

	/**
	 * The input mapper.
	 */
	private IInputMapper inputMapper;

	/**
	 * The output mapper.
	 */
	private IOutputMapper outputMapper;

	/**
	 * The states.
	 */
	private List<IState> states;

	/**
	 * The inline flows.
	 */
	private List<IInlineFlowState> inlineFlows;

	/**
	 * The vars.
	 */
	private List<IVar> vars;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.AbstractTransitionableFrom#init(org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode,
	 * org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement)
	 */
	/**
	 * @param node
	 * @param parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.imports = new ArrayList<IImport>();
		this.inlineFlows = new ArrayList<IInlineFlowState>();
		this.states = new ArrayList<IState>();
		this.vars = new ArrayList<IVar>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("action-state".equals(child.getLocalName())) {
					ActionState state = new ActionState();
					state.init(child, this);
					this.states.add(state);
				}
				else if ("view-state".equals(child.getLocalName())) {
					ViewState state = new ViewState();
					state.init(child, this);
					this.states.add(state);
				}
				else if ("decision-state".equals(child.getLocalName())) {
					DecisionState state = new DecisionState();
					state.init(child, this);
					this.states.add(state);
				}
				else if ("end-state".equals(child.getLocalName())) {
					EndState state = new EndState();
					state.init(child, this);
					this.states.add(state);
				}
				else if ("subflow-state".equals(child.getLocalName())) {
					SubflowState state = new SubflowState();
					state.init(child, this);
					this.states.add(state);
				}
				else if ("inline-flow".equals(child.getLocalName())) {
					InlineFlowState state = new InlineFlowState();
					state.init(child, this);
					this.inlineFlows.add(state);
				}
				else if ("import".equals(child.getLocalName())) {
					Import im = new Import();
					im.init(child, this);
					this.imports.add(im);
				}
				else if ("var".equals(child.getLocalName())) {
					Variable var = new Variable();
					var.init(child, this);
					this.vars.add(var);
				}
				else if ("start-actions".equals(child.getLocalName())) {
					this.entryActions = new EntryActions();
					this.entryActions.init(child, this);
				}
				else if ("end-actions".equals(child.getLocalName())) {
					this.exitActions = new ExitActions();
					this.exitActions.init(child, this);
				}
				else if ("input-mapper".equals(child.getLocalName())) {
					this.inputMapper = new InputMapper();
					this.inputMapper.init(child, this);
				}
				else if ("output-mapper".equals(child.getLocalName())) {
					this.outputMapper = new OutputMapper();
					this.outputMapper.init(child, this);
				}
			}
		}

		// reconnect transistions
		for (IState state : this.states) {
			if (state instanceof ITransitionableFrom) {
				for (ITransition trans : ((ITransitionableFrom) state)
						.getOutputTransitions()) {
					if (trans instanceof IStateTransition) {
						if (((IStateTransition) trans).getToState() != null) {
							((IStateTransition) trans).getToState()
									.getInputTransitions().add(trans);
						}
					}
				}
				if (state instanceof IDecisionState) {
					for (IIf i : ((IDecisionState) state).getIfs()) {
						if (i.getThenTransition() != null
								&& i.getThenTransition().getToState() != null
								&& !i.getThenTransition().getToState()
										.getInputTransitions().contains(
												i.getThenTransition())) {
							i.getThenTransition().getToState()
									.getInputTransitions().add(
											i.getThenTransition());
						}
						if (i.getElseTransition() != null
								&& i.getElseTransition().getToState() != null
								&& !i.getElseTransition().getToState()
										.getInputTransitions().contains(
												i.getElseTransition())) {
							i.getElseTransition().getToState()
									.getInputTransitions().add(
											i.getElseTransition());
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addImport(org.springframework.ide.eclipse.webflow.core.model.IImport)
	 */
	/**
	 * @param ip
	 */
	public void addImport(IImport ip) {
		if (!getImports().contains(ip)) {
			this.getImports().add(ip);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.getStates()
					.indexOf(ip)), ip);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addImport(org.springframework.ide.eclipse.webflow.core.model.IImport,
	 * int)
	 */
	/**
	 * @param i
	 * @param pm
	 */
	public void addImport(IImport pm, int i) {
		if (!getImports().contains(pm)) {
			this.getImports().add(i, pm);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), pm);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addState(org.springframework.ide.eclipse.webflow.core.model.IState)
	 */
	/**
	 * @param state
	 */
	public void addState(IState state) {
		if (!getStates().contains(state)) {
			// attach to xml after last state
			WebflowModelXmlUtils.insertNode(state.getNode(), node);
			this.states.add(state);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.states
					.indexOf(state)), state);

			// add possible dead transitions to new state
			if (state instanceof ITransitionableTo) {
				for (IState s : this.states) {
					if (s instanceof ITransitionableFrom) {
						for (ITransition trans : ((ITransitionableFrom) s)
								.getOutputTransitions()) {
							if (trans instanceof IStateTransition) {
								IStateTransition stateTrans = (IStateTransition) trans;
								if (state.getId().equals(
										stateTrans.getToStateId())) {
									if (!((ITransitionableTo) state)
											.getInputTransitions().contains(
													trans)) {
										stateTrans.getFromState()
												.fireStructureChange(OUTPUTS,
														stateTrans);
										((ITransitionableTo) state)
												.addInputTransition(trans);
									}
								}
							}
						}
					}
					if (s instanceof IDecisionState) {
						for (IIf i : ((IDecisionState) s).getIfs()) {
							if (i.getThenTransition() != null) {
								IIfTransition ifTrans = (IIfTransition) i
										.getThenTransition();
								if (state.getId()
										.equals(ifTrans.getToStateId())) {
									if (!((ITransitionableTo) state)
											.getInputTransitions().contains(
													ifTrans)) {
										ifTrans.getElementParent()
												.fireStructureChange(OUTPUTS,
														ifTrans);
										((ITransitionableTo) state)
												.addInputTransition(ifTrans);
									}
								}
							}
							if (i.getElseTransition() != null) {
								IIfTransition ifTrans = (IIfTransition) i
										.getElseTransition();
								if (state.getId()
										.equals(ifTrans.getToStateId())) {
									if (!((ITransitionableTo) state)
											.getInputTransitions().contains(
													ifTrans)) {
										ifTrans.getElementParent()
												.fireStructureChange(OUTPUTS,
														ifTrans);
										((ITransitionableTo) state)
												.addInputTransition(ifTrans);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addState(org.springframework.ide.eclipse.webflow.core.model.IState,
	 * int)
	 */
	/**
	 * @param i
	 * @param state
	 */
	public void addState(IState state, int i) {
		if (!getStates().contains(state)) {
			int refIndex = i;
			if (i >= this.states.size()) {
				refIndex = this.states.size() - 1;
			}
			IState refState = getStates().get(refIndex);
			this.states.add(i, state);
			WebflowModelXmlUtils.insertBefore(state.getNode(), refState.getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), state);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addVar(org.springframework.ide.eclipse.webflow.core.model.IVar)
	 */
	/**
	 * @param state
	 */
	public void addVar(IVar state) {
		if (!getVars().contains(state)) {
			this.getVars().add(state);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.getVars()
					.indexOf(state)), state);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState,
	 * int)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addVar(org.springframework.ide.eclipse.webflow.core.model.IVar,
	 * int)
	 */
	/**
	 * @param i
	 * @param state
	 */
	public void addVar(IVar state, int i) {
		if (!getVars().contains(state)) {
			this.getVars().add(i, state);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), state);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.ISubFlowState#getStates()
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#getImports()
	 */
	/**
	 * @return
	 */
	public List<IImport> getImports() {
		return this.imports;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#getInputMapper()
	 */
	/**
	 * @return
	 */
	public IInputMapper getInputMapper() {
		return inputMapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#getOutputMapper()
	 */
	/**
	 * @return
	 */
	public IOutputMapper getOutputMapper() {
		return outputMapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#getStartState()
	 */
	/**
	 * @return
	 */
	public IState getStartState() {
		if (hasStartState()) {
			List<IDOMNode> nodes = getChildrenNodeByTagName("start-state");
			IDOMNode node = nodes.get(0);
			return WebflowModelXmlUtils.getStateById(this, getAttribute(node,
					"idref"));
		}
		return null;
	}

	/**
	 * @return
	 */
	public List<IState> getStates() {
		return this.states;
	}

	/**
	 * @return
	 */
	public List<IInlineFlowState> getInlineFlowStates() {
		return this.inlineFlows;
	}

	/**
	 * @return
	 */
	public List<IVar> getVars() {
		return this.vars;
	}

	/**
	 * @return
	 */
	public boolean hasStartState() {
		List<IDOMNode> nodes = getChildrenNodeByTagName("start-state");
		return nodes != null && nodes.size() == 1;
	}

	/**
	 * @param state
	 * @return
	 */
	public boolean isStartState(IState state) {
		return hasStartState() && state.getId() != null
				&& state.equals(getStartState());
	}

	/**
	 * @param i
	 * @param im
	 */
	public void moveImport(IImport im, int i) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param i
	 * @param state
	 */
	public void moveState(IState state, int i) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param i
	 * @param state
	 */
	public void moveVar(IVar state, int i) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param im
	 */
	public void removeImport(IImport im) {
		if (getImports().contains(im)) {
			this.getImports().remove(im);
			super.fireStructureChange(REMOVE_CHILDREN, im);
		}

	}

	/**
	 * @param state
	 */
	public void removeState(IState state) {
		if (getStates().contains(state)) {
			this.states.remove(state);
			getNode().removeChild(state.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, state);
		}
	}

	/**
	 * @param state
	 */
	public void removeVar(IVar state) {
		if (getVars().contains(state)) {
			this.getVars().remove(state);
			super.fireStructureChange(REMOVE_CHILDREN, state);
		}

	}

	/**
	 * @param state
	 */
	public void setStartState(IState state) {
		List<IDOMNode> nodes = getChildrenNodeByTagName("start-state");
		IDOMNode node = nodes.get(0);
		setAttribute(node, "idref", state.getId());
		super.fireStructureChange(MOVE_CHILDREN, state);
	}

	/**
	 * @param parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("flow");
		init(node, parent);
	}

	/**
	 * @return
	 */
	public IWebflowState cloneModelElement() {
		WebflowState state = new WebflowState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * @param element
	 */
	public void applyCloneValues(IWebflowState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			init(element.getNode(), parent);
		}
	}

	/**
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.core.model.ISubFlowState#addChildState(org.springframework.ide.eclipse.web.core.model.IState)
	 */
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.model.IWebflowState#addState(org.springframework.ide.eclipse.webflow.core.model.IState)
	 */
	/**
	 * @param state
	 */
	public void addInlineFlowState(IInlineFlowState state) {
		if (!this.inlineFlows.contains(state)) {
			// attach to xml after last state
			WebflowModelXmlUtils.insertNode(state.getNode(), node);
			this.inlineFlows.add(state);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.inlineFlows
					.indexOf(state)), state);
		}
	}

	/**
	 * @param i
	 * @param state
	 */
	public void addInlineFlowState(IInlineFlowState state, int i) {
		if (!this.inlineFlows.contains(state)) {
			int refIndex = i;
			if (i >= this.inlineFlows.size()) {
				refIndex = this.inlineFlows.size();
			}
			IState refState = getStates().get(refIndex);
			this.inlineFlows.add(i, state);
			WebflowModelXmlUtils.insertBefore(state.getNode(), refState.getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), state);
		}

	}

	/**
	 * @param i
	 * @param state
	 */
	public void moveInlineFlowState(IInlineFlowState state, int i) {
	}

	/**
	 * @param state
	 */
	public void removeInlineFlowState(IInlineFlowState state) {
		if (this.inlineFlows.contains(state)) {
			this.inlineFlows.remove(state);
			getNode().removeChild(state.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, state);
		}
	}
}
