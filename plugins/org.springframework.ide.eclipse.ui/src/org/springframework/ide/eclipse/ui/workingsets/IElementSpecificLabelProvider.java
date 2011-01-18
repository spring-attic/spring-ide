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
package org.springframework.ide.eclipse.ui.workingsets;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Extension to the {@link ILabelProvider} interface.
 * <p>
 * To be implemented by label providers that want to get a callback
 * {@link #supportsElement(Object)} to determine if they can provide meaningful
 * text and images.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IElementSpecificLabelProvider extends ILabelProvider {

	/**
	 * Return true if for given <code>object</code> a text and image can be
	 * provided.
	 * @param object the object to check
	 * @return true if supported
	 */
	boolean supportsElement(Object object);

}
