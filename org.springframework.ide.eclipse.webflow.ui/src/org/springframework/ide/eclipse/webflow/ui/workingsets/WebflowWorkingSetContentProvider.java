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
package org.springframework.ide.eclipse.webflow.ui.workingsets;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.ui.navigator.WebflowNavigatorContentProvider;

/**
 * Simple wrapper of {@link WebflowNavigatorContentProvider} that prevents children
 * of a {@link IFile} instance.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowWorkingSetContentProvider extends
		WebflowNavigatorContentProvider {
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFile) {
			return IModelElement.NO_CHILDREN;
		}
		return super.getChildren(parentElement);
	}
	
}
