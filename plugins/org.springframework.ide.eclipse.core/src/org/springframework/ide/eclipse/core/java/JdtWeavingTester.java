/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import org.eclipse.contribution.jdt.IsWovenTester;

/**
 * Utility class to encapsulate the access to the Jdt weaving plugin.
 * @author Christian Dupuis
 * @since 2.2.7
 */
class JdtWeavingTester {

	static boolean isJdtWeavingActive() {
		return IsWovenTester.isWeavingActive();
	}

}