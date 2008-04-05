/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model.metadata;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * {@link ILabelProvider} extension that is only called to provide a label and
 * image if indicated to support a given element.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadataLabelProvider extends ILabelProvider, IDescriptionProvider {
	
	/**
	 * Returns <code>true</code> if this can provide a label, image and description for an element.
	 */
	boolean supports(Object object);

}
