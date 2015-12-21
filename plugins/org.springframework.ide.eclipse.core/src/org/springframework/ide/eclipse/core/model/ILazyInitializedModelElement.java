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
package org.springframework.ide.eclipse.core.model;

/**
 * Interface that donates that a {@link IModelElement} is lazy loaded.
 * <p>
 * Access to any getter of the model element under question should be deferred
 * until {@link #isInitialized()} returns <code>true</code>.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public interface ILazyInitializedModelElement {
	
	/**
	 * Return <code>true</code> if this model element is fully initialized.
	 * @return true if fully initialized
	 */
	boolean isInitialized();
	
}
