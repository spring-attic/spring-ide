/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

/**
 * Extension to {@link IBeansComponent} to specify that a component is annotated with Spring 3.1 
 * environment profiles.
 * @author Christian Dupuis
 * @since 2.8.0
 */
public interface IProfileAwareBeansComponent extends IBeansComponent {
	
	/**
	 * Returns the list of Spring 3.1 environment profiles.
	 * @return the list of profiles.
	 */
	Set<String> getProfiles();
	
	/**
	 * Checks if this config set has configured profiles.
	 * @since 2.8.0 
	 */
	boolean hasProfiles();
	
}
