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
package org.springframework.ide.eclipse.webflow.core.model;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public interface IWebflowModelElement {

	int ACTION = 7;

	int ACTION_STATE = 51;

	String ADD_CHILDREN = "add_children";

	int ATTRIBUTEMAPPER = 9;

	int CONFIG = 3;

	int CONFIG_SET = 4;

	int DECISION_STATE = 55;

	int END_STATE = 54;

	int IF = 10;

	int INLINE_FLOW = 11;

	int INPUT = 12;

	int OUTPUT = 13;

	int IF_TRANSITION = 81;

	String INPUTS = "inputs";

	int MODEL = 1;

	String MOVE_CHILDREN = "move_children";

	String OUTPUTS = "outputs";

	int PROJECT = 2;

	int PROPERTY = 6;

	String PROPS = "properties";

	String REMOVE_CHILDREN = "remove_children";

	int STATE_TRANSITION = 80;

	int SUBFLOW_STATE = 53;

	int VIEW_STATE = 52;

	int ENTRY_ACTIONS = 520;

	int EXIT_ACTIONS = 530;

	int WEBFLOW_STATE = 50;

	void addPropertyChangeListener(PropertyChangeListener l);

	void fireStructureChange(String prop, Object child);

	IWebflowModelElement getElementParent();

	int getElementStartLine();

	IDOMNode getNode();

	void init(IDOMNode node, IWebflowModelElement parent);

	void removePropertyChangeListener(PropertyChangeListener l);

	void setElementParent(IWebflowModelElement parent);

	void accept(IWebflowModelElementVisitor visitor, IProgressMonitor monitor);

}
