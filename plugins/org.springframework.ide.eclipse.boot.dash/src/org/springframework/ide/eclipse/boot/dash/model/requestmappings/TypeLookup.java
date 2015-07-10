/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.requestmappings;

import org.eclipse.jdt.core.IType;

/**
 * Represents a context in which it is possible to find Java types. Typcially
 * this context is some IJavaProject (or something that is linked to one, like a BDE)
 *
 * @author Kris De Volder
 */
public interface TypeLookup {
	IType findType(String fqName);
}
