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
package org.springframework.ide.eclipse.beans.ui.namespaces.beans;

import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/beans"</code>.
 * 
 * @author Torsten Juergeleit
 */
public class BeansNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	protected String getElementLabel(ISourceModelElement element, int flags) {
		return BeansNamespaceLabels.getElementLabel(element, flags);
	}
}
