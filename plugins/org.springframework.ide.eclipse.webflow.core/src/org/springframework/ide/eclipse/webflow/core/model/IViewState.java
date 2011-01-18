/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IViewState extends ITransitionableFrom, IViewEnabled {

	/**
	 * Gets the render actions.
	 * 
	 * @return the render actions
	 */
	IRenderActions getRenderActions();

	/**
	 * Sets the render actions.
	 * 
	 * @param renderActions the render actions
	 */
	void setRenderActions(IRenderActions renderActions);
	
	void setRedirect(String redirect);
	
	String getRedirect();
	
	void setPopup(String popup);
	
	String getPopup();
	
	String getModel();
	
	void setModel(String model);
	
}
