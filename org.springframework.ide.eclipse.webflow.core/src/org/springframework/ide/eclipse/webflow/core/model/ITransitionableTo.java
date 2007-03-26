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

import java.util.List;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface ITransitionableTo extends IState {

	/**
	 * Gets the input transitions.
	 * 
	 * @return the input transitions
	 */
	List<ITransition> getInputTransitions();

	/**
	 * Adds the input transition.
	 * 
	 * @param transition the transition
	 */
	void addInputTransition(ITransition transition);

	/**
	 * Removes the input transition.
	 * 
	 * @param transition the transition
	 */
	void removeInputTransition(ITransition transition);

}
