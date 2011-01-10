/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Holds an {@link IBean}'s alias.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeanAlias extends IBeansModelElement, ISourceModelElement {

	String getBeanName();
}
