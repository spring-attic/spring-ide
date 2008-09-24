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
package org.springframework.ide.eclipse.ui.navigator.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * (Useful?) extension of the {@link ILinkHelper} interface from the common navigator framework.
 * <p>
 * Implementations of this extension interface should be able to resolve model instances based on
 * any arbitrary object.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public interface ILinkHelperExtension extends ILinkHelper {
	
	/**
	 * Resolve the given <code>input</code> to a {@link IStructuredSelection}.
	 * @param input the input object to select in the viewer
	 * @return the resolved selection; can be null if on selection could be created.
	 */
	IStructuredSelection findSelection(Object input);
		
}
