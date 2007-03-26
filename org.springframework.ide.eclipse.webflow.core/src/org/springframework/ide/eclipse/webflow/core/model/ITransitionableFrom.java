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
public interface ITransitionableFrom extends IState {

	/**
	 * Gets the output transitions.
	 * 
	 * @return the output transitions
	 */
	List<ITransition> getOutputTransitions();

	/**
	 * Adds the output transition.
	 * 
	 * @param transition the transition
	 */
	void addOutputTransition(ITransition transition);

	/**
	 * Removes the output transition.
	 * 
	 * @param transition the transition
	 */
	void removeOutputTransition(ITransition transition);
}
