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

package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class is an <code>ILabelProvider</code> which knows about the beans
 * core model's <code>IModelElement</code>s. If the given element is not of
 * type <code>IModelElement</code> the it tries to adapt it via
 * <code>IAdaptable</code>.
 * 
 * @see org.springframework.ide.eclipse.core.model.IModelElement
 * @see org.eclipse.core.runtime.IAdaptable
 * @author Torsten Juergeleit
 */
public class BeansModelLabelProvider extends LabelProvider {

	WorkbenchLabelProvider wbLabelProvider;

	public BeansModelLabelProvider() {
		wbLabelProvider = new WorkbenchLabelProvider();
	}

    public void dispose() {
    	wbLabelProvider.dispose();
    	super.dispose();
    }

    public Image getImage(Object element) {
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return BeansModelImages.getImage((IModelElement) adaptedElement);
		}
		return wbLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return BeansModelElementLabels.getElementLabel(
					(IModelElement) adaptedElement, 0);
		}
		return wbLabelProvider.getText(element);
	}
}
