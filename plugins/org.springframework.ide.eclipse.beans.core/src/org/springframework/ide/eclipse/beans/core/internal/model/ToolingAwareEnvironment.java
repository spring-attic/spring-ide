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

import org.springframework.core.env.StandardEnvironment;

/**
 * The ToolingAwareEnvironment is used to accept all kind of profiles
 * for creating the internal beans model for the tooling.
 * @author Martin Lippert
 * @since 2.9.0
 */
public class ToolingAwareEnvironment extends StandardEnvironment {
	
	@Override
	public boolean acceptsProfiles(String... profiles) {
		return true;
	}

}
