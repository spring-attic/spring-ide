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
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.parsing.CompositeComponentDefinition;

/**
 * Extension to {@link CompositeComponentDefinition} that encapsulates a <code>beans</code> element.
 * <p>
 * Most notably this class exposes the profiles configured on the <code>beans</code> element.
 * @author Christian Dupuis
 * @since 2.8.0
 */
public class ProfileAwareCompositeComponentDefinition extends CompositeComponentDefinition {
	
	private String[] profiles = null;
	
	public ProfileAwareCompositeComponentDefinition(String name, Object source, String[] profiles) {
		super(name, source);
		this.profiles = profiles;
	}
	
	/**
	 * Returns the set of profiles in wh
	 * @return
	 */
	public String[] getProfiles() {
		return profiles;
	}
}