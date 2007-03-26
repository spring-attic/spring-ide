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
package org.springframework.ide.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * This is a simple wrapper which is used to hide the Eclipse 3.2 stuff when
 * running within Eclipse 3.1.
 * @author Torsten Juergeleit
 */
public class PatternFilteredTree extends FilteredTree {

	public PatternFilteredTree(Composite parent, int treeStyle) {
		super(parent, treeStyle, new PatternFilter());
	}

	@Override
	public TreeViewer getViewer() {
		return super.getViewer();
	}
}
