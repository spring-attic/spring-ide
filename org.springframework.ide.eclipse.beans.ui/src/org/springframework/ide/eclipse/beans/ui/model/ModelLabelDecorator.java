/*
 * Copyright 2002-2004 the original author or authors.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

public class ModelLabelDecorator implements ILabelDecorator {

	public ModelLabelDecorator() {
	}

	public Image decorateImage(Image image, Object element) {
		int flags = ((INode) element).getFlags();
		if (flags != 0) {
			ImageDescriptor descriptor = new ModelImageDescriptor(image, flags);
			image = BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return image;
	}

	public String decorateText(String text, Object element) {
		return text;
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}
}
