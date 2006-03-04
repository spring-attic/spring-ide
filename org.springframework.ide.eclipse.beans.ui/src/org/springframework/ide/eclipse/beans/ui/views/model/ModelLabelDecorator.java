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

package org.springframework.ide.eclipse.beans.ui.views.model;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;

/**
 * This label decorator decorates the nodes of externally defined configs and
 * beans. 
 * 
 * @author Torsten Juergeleit
 */
public class ModelLabelDecorator implements ILabelDecorator {

	public Image decorateImage(Image image, Object element) {
		INode node = (INode) element;
		if (node instanceof ConfigNode && node.getName().charAt(0) == '/' ||
				node instanceof BeanNode &&
				((BeanNode) node).getConfigNode().getName().charAt(0) == '/') {
			image = BeansModelImages.getDecoratedImage(image,
											   BeansModelImages.FLAG_EXTERNAL);
		}
		int flags = node.getFlags();
		if (flags != 0) {
			ImageDescriptor descriptor = new ModelImageDescriptor(image, flags);
			image = BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return image;
	}

	public String decorateText(String text, Object element) {
		INode node = (INode) element;
		if (node instanceof ConfigNode && node.getName().charAt(0) == '/') {
			ConfigNode config = (ConfigNode) node;
			if (config.getName().charAt(0) == '/') {
				text = '/' + config.getParent().getParent().getName() + '/' +
					   text;
			}
		}
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

	private class ModelImageDescriptor extends CompositeImageDescriptor {

		private Image baseImage;
		private int flags;
		private Point size;

		public ModelImageDescriptor(Image baseImage, int flags) {
			this.baseImage = baseImage;
			this.flags = flags;
		}

		protected Point getSize() {
			if (size == null) {
				ImageData data = baseImage.getImageData();
				size = new Point(data.width, data.height);
			}
			return size;
		}

		protected void drawCompositeImage(int width, int height) {
			ImageData background = baseImage.getImageData();
			if (background == null) {
				background = DEFAULT_IMAGE_DATA;
			}
			drawImage(background, 0, 0);
			drawOverlays();
		}

		protected void drawOverlays() {
			ImageData data = null;
			if ((flags & INode.FLAG_HAS_ERRORS) != 0) {
				data = BeansUIImages.DESC_OVR_ERROR.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			} else if ((flags & INode.FLAG_HAS_WARNINGS) != 0) {
				data = BeansUIImages.DESC_OVR_WARNING.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			}
		}
	}
}
