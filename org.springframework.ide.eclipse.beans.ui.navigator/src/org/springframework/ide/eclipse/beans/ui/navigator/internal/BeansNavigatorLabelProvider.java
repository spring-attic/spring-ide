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

package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelElementLabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class is an <code>ICommonLabelProvider</code> which knows about the
 * beans core model's <code>IModelElement</code>s.
 *
 * @author Torsten Juergeleit
 */
public class BeansNavigatorLabelProvider extends BeansModelLabelProvider
		implements ICommonLabelProvider {

	public String getDescription(Object element) {
		if (element instanceof IModelElement) {
			return BeansModelElementLabels.getElementLabel(
					(IModelElement) element,
					BeansModelElementLabels.APPEND_PATH);
		}
		return null;
	}

	public void init(ICommonContentExtensionSite aConfig) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public void saveState(IMemento aMemento) {
	}
}
