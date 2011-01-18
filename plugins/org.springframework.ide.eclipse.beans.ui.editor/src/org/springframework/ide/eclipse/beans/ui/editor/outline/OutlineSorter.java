/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.beans.core.BeansTags;
import org.w3c.dom.Node;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class OutlineSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		return BeansTags.getTag((Node) element).ordinal();
	}
}
