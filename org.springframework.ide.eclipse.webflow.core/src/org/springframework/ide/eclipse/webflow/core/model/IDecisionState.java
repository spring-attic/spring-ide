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
public interface IDecisionState extends ITransitionableTo {

	/**
	 * Gets the ifs.
	 * 
	 * @return the ifs
	 */
	List<IIf> getIfs();

	/**
	 * Adds the if.
	 * 
	 * @param theIf the if
	 */
	void addIf(IIf theIf);

	/**
	 * Adds the if.
	 * 
	 * @param i the i
	 * @param theIf the if
	 */
	void addIf(IIf theIf, int i);

	/**
	 * Removes the if.
	 * 
	 * @param theIf the if
	 */
	void removeIf(IIf theIf);

}
