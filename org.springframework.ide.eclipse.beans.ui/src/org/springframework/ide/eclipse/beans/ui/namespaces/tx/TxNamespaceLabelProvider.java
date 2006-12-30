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

package org.springframework.ide.eclipse.beans.ui.namespaces.tx;

import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/tx"</code>.
 * 
 * @author Torsten Juergeleit
 */
public class TxNamespaceLabelProvider extends LabelProvider implements
		ITreePathLabelProvider, IDescriptionProvider {

	public Image getImage(Object element) {
		if (element instanceof ISourceModelElement) {
			return TxNamespaceImages.getImage((ISourceModelElement)
					element);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof ISourceModelElement) {
			return TxNamespaceLabels.getElementLabel(
					(ISourceModelElement) element, 0);
		}
		return null;
	}

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = ModelUtils.adaptToModelElement(elementPath
				.getLastSegment());
		if (element instanceof ISourceModelElement
				&& elementPath.getSegmentCount() > 1) {
			Object parentElement = elementPath.getParentPath()
					.getLastSegment();
			if (parentElement instanceof IModelElement) {
				label.setImage(TxNamespaceImages.getImage(
						(ISourceModelElement) element,
						(IModelElement) parentElement));
				label.setText(getText(element));
			}
		}
	}

	public String getDescription(Object element) {
		if (element instanceof ISourceModelElement) {
			return TxNamespaceLabels.getElementLabel(
					(ISourceModelElement) element,
					TxNamespaceLabels.APPEND_PATH
							| BeansModelLabels.DESCRIPTION);
		}
		return null;
	}
}
