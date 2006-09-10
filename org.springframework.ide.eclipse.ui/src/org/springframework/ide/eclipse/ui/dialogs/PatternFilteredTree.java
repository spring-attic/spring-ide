/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public TreeViewer getViewer() {
		return super.getViewer();
	}
}
