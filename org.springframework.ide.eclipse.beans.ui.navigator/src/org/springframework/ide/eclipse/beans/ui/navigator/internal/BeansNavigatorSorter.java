/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Sorter for Spring beans {@link ISourceModelElement}s. It keeps them in the
 * order found in the corresponding {@link IBeansConfig} file.
 * 
 * @author Torsten Juergeleit
 */
public class BeansNavigatorSorter extends ViewerSorter {

	public int category(Object element) {

		// Keep the config sets separate
		if (element instanceof IBeansConfigSet) {
			return 1;
		}
		return 0;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ISourceModelElement
				|| e2 instanceof ISourceModelElement) {

			// We don't want to sort it, just show it in the order it's found
			// in the file
			return 0;
		}
		return super.compare(viewer, e1, e2);
	}
}
