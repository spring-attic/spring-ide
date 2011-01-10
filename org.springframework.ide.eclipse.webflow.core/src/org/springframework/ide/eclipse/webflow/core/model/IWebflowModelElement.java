/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public interface IWebflowModelElement extends IModelElement, IResourceModelElement, ISourceModelElement {
	
	int MODEL = 1;

	int PROJECT = 2;

	int CONFIG = 3;

	String ADD_CHILDREN = "add_children";

	String INPUTS = "inputs";

	String OUTPUTS = "outputs";

	String MOVE_CHILDREN = "move_children";

	String PROPS = "properties";

	String REMOVE_CHILDREN = "remove_children";

	void addPropertyChangeListener(PropertyChangeListener l);

	void fireStructureChange(String prop, Object child);

	int getElementStartLine();

	IDOMNode getNode();

	void init(IDOMNode node, IWebflowModelElement parent);

	void removePropertyChangeListener(PropertyChangeListener l);

	void setElementParent(IWebflowModelElement parent);

}
