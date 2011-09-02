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

import java.util.Set;

import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.Assert;

/**
 * Extension to {@link BeansComponent} used for nesting {@link BeanComponentDefinition} for a <code>beans</code>
 * element.
 * <p>
 * This supports Spring 3.1 environment profiles
 * @author Christian Dupuis
 * @since 2.8.0
 */
public class ProfileAwareBeansComponent extends BeansComponent implements IProfileAwareBeansComponent {

	private ProfileAwareCompositeComponentDefinition definition = null;

	public ProfileAwareBeansComponent(IModelElement parent, ComponentDefinition definition) {
		super(parent, definition);
		Assert.isInstanceOf(ProfileAwareCompositeComponentDefinition.class, definition);
		this.definition = (ProfileAwareCompositeComponentDefinition) definition;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getProfiles() {
		return definition.getProfiles();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasProfiles() {
		return definition.getProfiles() != null && definition.getProfiles().size() > 0;
	}
}
