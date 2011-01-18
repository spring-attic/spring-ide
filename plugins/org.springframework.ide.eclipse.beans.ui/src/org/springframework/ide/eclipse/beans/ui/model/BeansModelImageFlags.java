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
package org.springframework.ide.eclipse.beans.ui.model;

import org.springframework.ide.eclipse.ui.SpringUIImageFlags;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface BeansModelImageFlags extends SpringUIImageFlags {

	int FLAG_EXTERNAL = 1 << 3;

	int FLAG_CHILD = 1 << 4;

	int FLAG_FACTORY = 1 << 5;

	int FLAG_ABSTRACT = 1 << 6;

	int FLAG_PROTOTYPE = 1 << 7;

	int FLAG_LAZY_INIT = 1 << 8;

	int FLAG_ANNOTATION = 1 << 9;
}
