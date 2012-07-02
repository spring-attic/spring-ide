/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Interface to get rid off the static dependency to Spring IDE's {@link JdtUtils}.
 * 
 * @author Oliver Gierke
 */
interface TypePredicates {

	boolean typeImplements(IType type, String candidateType);
}
