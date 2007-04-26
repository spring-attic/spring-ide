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
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Extends {@link IBaseLabelProvider} with the methods to provide the text
 * and/or image for the label of a given {@link ISourceModelElement} with it's
 * context {@link IModelElement}.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface INamespaceLabelProvider extends IBaseLabelProvider {

	Image getImage(ISourceModelElement element, IModelElement context);

	String getText(ISourceModelElement element, IModelElement parentElement);
}
