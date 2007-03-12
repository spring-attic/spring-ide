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

package org.springframework.ide.eclipse.webflow.ui.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link ViewerSorter} that sorts the tree elements depending on element's
 * resources and their line location in the resource.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelNavigatorSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		if (element instanceof IWebflowConfig) {
			return 2;
		}
		return 0;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof IWebflowConfig && e2 instanceof IWebflowConfig) {
			IWebflowConfig ref1 = (IWebflowConfig) e1;
			IWebflowConfig ref2 = (IWebflowConfig) e2;
			super.compare(viewer, ref1.getResource(), ref2.getResource());
		}
		return super.compare(viewer, e1, e2);
	}
}
