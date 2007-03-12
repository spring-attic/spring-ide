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

package org.springframework.ide.eclipse.webflow.core.model;

import java.beans.PropertyChangeListener;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public interface IWebflowModelElement {

	/**
	 * The ACTION.
	 */
	int ACTION = 7;

	/**
	 * The ACTIO n_ STATE.
	 */
	int ACTION_STATE = 51;

	/**
	 * The AD d_ CHILDREN.
	 */
	String ADD_CHILDREN = "add_children";

	/**
	 * The ATTRIBUTEMAPPER.
	 */
	int ATTRIBUTEMAPPER = 9;

	/**
	 * The CONFIG.
	 */
	int CONFIG = 3;

	/**
	 * The CONFI g_ SET.
	 */
	int CONFIG_SET = 4;

	/**
	 * The DECISIO n_ STATE.
	 */
	int DECISION_STATE = 55;

	/**
	 * The EN d_ STATE.
	 */
	int END_STATE = 54;

	/**
	 * The IF.
	 */
	int IF = 10;

	/**
	 * The INLIN e_ FLOW.
	 */
	int INLINE_FLOW = 11;

	/**
	 * The INPUT.
	 */
	int INPUT = 12;

	/**
	 * The OUTPUT.
	 */
	int OUTPUT = 13;

	/**
	 * The I f_ TRANSITION.
	 */
	int IF_TRANSITION = 81;

	/**
	 * The INPUTS.
	 */
	String INPUTS = "inputs";

	/**
	 * The MODEL.
	 */
	int MODEL = 1;

	/**
	 * The MOV e_ CHILDREN.
	 */
	String MOVE_CHILDREN = "move_children";

	/**
	 * The OUTPUTS.
	 */
	String OUTPUTS = "outputs";

	/**
	 * The PROJECT.
	 */
	int PROJECT = 2;

	/**
	 * The PROPERTY.
	 */
	int PROPERTY = 6;

	/**
	 * The PROPS.
	 */
	String PROPS = "properties";

	/**
	 * The REMOV e_ CHILDREN.
	 */
	String REMOVE_CHILDREN = "remove_children";

	/**
	 * The STAT e_ TRANSITION.
	 */
	int STATE_TRANSITION = 80;

	/**
	 * The SUBFLO w_ STATE.
	 */
	int SUBFLOW_STATE = 53;

	/**
	 * The VIE w_ STATE.
	 */
	int VIEW_STATE = 52;

	/**
	 * The ENTR y_ ACTIONS.
	 */
	int ENTRY_ACTIONS = 520;

	/**
	 * The EXI t_ ACTIONS.
	 */
	int EXIT_ACTIONS = 530;

	/**
	 * The WEBFLO w_ STATE.
	 */
	int WEBFLOW_STATE = 50;

	/**
	 * Adds the property change listener.
	 * 
	 * @param l the l
	 */
	void addPropertyChangeListener(PropertyChangeListener l);

	/**
	 * Removes the property change listener.
	 * 
	 * @param l the l
	 */
	void removePropertyChangeListener(PropertyChangeListener l);

	/**
	 * Fire structure change.
	 * 
	 * @param child the child
	 * @param prop the prop
	 */
	void fireStructureChange(String prop, Object child);

	/**
	 * Gets the element parent.
	 * 
	 * @return the element parent
	 */
	IWebflowModelElement getElementParent();

	/**
	 * Gets the node.
	 * 
	 * @return the node
	 */
	IDOMNode getNode();

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	void init(IDOMNode node, IWebflowModelElement parent);
	
	/**
	 * 
	 * 
	 * @param parent 
	 */
	public void setElementParent(IWebflowModelElement parent);
	
	int getElementStartLine();

}