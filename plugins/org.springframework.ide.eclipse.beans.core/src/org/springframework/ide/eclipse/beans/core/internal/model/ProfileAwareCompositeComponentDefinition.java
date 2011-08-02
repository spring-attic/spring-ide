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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.parsing.CompositeComponentDefinition;

/**
 * Extension to {@link CompositeComponentDefinition} that encapsulates a <code>beans</code> element.
 * <p>
 * Most notably this class exposes the profiles configured on the <code>beans</code> element.
 * @author Christian Dupuis
 * @since 2.8.0
 */
public class ProfileAwareCompositeComponentDefinition extends CompositeComponentDefinition {
	
	private Set<String> profiles = null;
	
	public ProfileAwareCompositeComponentDefinition(String name, Object source, String[] profiles) {
		super(name, source);
		if (profiles != null && profiles.length > 0) {
			this.profiles = new HashSet<String>(Arrays.asList(profiles));
		}
		else {
			this.profiles = Collections.emptySet();
		}
	}
	
	/**
	 * Returns the set of profiles for which this component and all children is valid
	 * @return
	 */
	public Set<String> getProfiles() {
		return profiles;
	}
}