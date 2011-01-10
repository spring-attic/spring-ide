/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.webflow.v1.config;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.support.ApplicationViewSelector;

/**
 * Encapsulates overall flow system configuration defaults. Allows for
 * centralized application of, and if necessary, overridding of system-wide
 * default values.
 * @author Christian Dupuis
 * @author Keith Donald
 */
@SuppressWarnings("serial")
public class FlowSystemDefaults implements Serializable {

	/**
	 * The default 'alwaysRedirectOnPause' execution attribute value.
	 */
	private boolean alwaysRedirectOnPause = true;

	/**
	 * The default flow execution repository type.
	 */
	private RepositoryType repositoryType = RepositoryType.CONTINUATION;

	/**
	 * Overrides the alwaysRedirectOnPause execution attribute default. Defaults
	 * to "true".
	 * @param alwaysRedirectOnPause the new default value
	 * @see ApplicationViewSelector#ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE
	 */
	public void setAlwaysRedirectOnPause(boolean alwaysRedirectOnPause) {
		this.alwaysRedirectOnPause = alwaysRedirectOnPause;
	}

	/**
	 * Overrides the default repository type.
	 * @param repositoryType the new default value
	 */
	public void setRepositoryType(RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}

	/**
	 * Applies default execution attributes if necessary. Defaults will only
	 * apply in the case where the user did not configure a value, or explicitly
	 * requested the 'default' value.
	 * @param executionAttributes the user-configured execution attribute map
	 * @return the map with defaults applied as appropriate
	 */
	public MutableAttributeMap applyExecutionAttributes(MutableAttributeMap executionAttributes) {
		if (executionAttributes == null) {
			executionAttributes = new LocalAttributeMap(1, 1);
		}
		if (!executionAttributes.contains(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE)) {
			executionAttributes.put(ApplicationViewSelector.ALWAYS_REDIRECT_ON_PAUSE_ATTRIBUTE,
					new Boolean(alwaysRedirectOnPause));
		}
		return executionAttributes;
	}

	/**
	 * Applies the default repository type if requested by the user.
	 * @param selectedType the selected repository type (may be null if no
	 * selection was made)
	 * @return the repository type, with the default applied if necessary
	 */
	public RepositoryType applyIfNecessary(RepositoryType selectedType) {
		if (selectedType == null) {
			return repositoryType;
		}
		else {
			return selectedType;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("alwaysRedirectOnPause", alwaysRedirectOnPause).append(
				"repositoryType", repositoryType).toString();
	}
}