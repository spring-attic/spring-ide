/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
 * Interface that exposes a reference to a bean by it's bean name.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeanReference extends IBeansModelElement,
		ISourceModelElement {

	/**
	 * Returns the target bean name that this reference points to (never
	 * <code>null</code>).
	 */
	String getBeanName();
}
