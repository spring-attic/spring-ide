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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanClassReferences;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

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
public class BeansModelLabelProvider extends LabelProvider implements
		ITreePathLabelProvider {

	WorkbenchLabelProvider wbLabelProvider;

	public BeansModelLabelProvider() {
		wbLabelProvider = new WorkbenchLabelProvider();
	}

    public void dispose() {
    	wbLabelProvider.dispose();
    	super.dispose();
    }

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = ModelUtils.adaptToModelElement(elementPath
				.getLastSegment());
		if (element instanceof IModelElement
				&& elementPath.getSegmentCount() > 1) {
			Object parentElement = elementPath.getParentPath()
					.getLastSegment();
			if (parentElement instanceof IModelElement) {
				label.setImage(BeansModelImages.getImage(
						(IModelElement) element,
						(IModelElement) parentElement));
			} else {
				label.setImage(getImage(element));
			}
		} else {
			label.setImage(getImage(element));
		}
		label.setText(getText(element));
	}

    public Image getImage(Object element) {
		Object adaptedElement = ModelUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return BeansModelImages.getImage((IModelElement) adaptedElement);
		}
		if (element instanceof ZipEntryStorage) {
			return wbLabelProvider.getImage(((ZipEntryStorage) element)
					.getZipResource());
		} else if (element instanceof BeanClassReferences) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		}
		return wbLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		Object adaptedElement = ModelUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return BeansModelLabels.getElementLabel(
					(IModelElement) adaptedElement, 0);
		}
		if (element instanceof IFile) {
			return ((IFile) element).getProjectRelativePath().toString();
		} else if (element instanceof ZipEntryStorage) {
			ZipEntryStorage storage = (ZipEntryStorage) element;
			StringBuffer buf = new StringBuffer(storage.getZipResource()
					.getProjectRelativePath().toString());
			buf.append(" - " + storage.getFullPath().toString());
			return buf.toString();
		} else if (element instanceof BeanClassReferences) {
			return BeansUIPlugin.getResourceString("BeanClassReferences.label");
		}
		return wbLabelProvider.getText(element);
	}
}
