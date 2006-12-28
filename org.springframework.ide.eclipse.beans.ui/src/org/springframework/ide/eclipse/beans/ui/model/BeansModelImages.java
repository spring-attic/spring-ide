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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.AbstractCompositeImageDescriptor;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides images for the beans core model's
 * {@link IModelElement}s.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansModelImages {

    public static final int FLAG_EXTERNAL = 1 << 1;

	public static Image getImage(IModelElement element) {
		return getImage(element, null);
	}

	public static Image getImage(IModelElement element,
			IModelElement context) {
		if (element instanceof IBeansProject) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROJECT);
		} else if (element instanceof IBeansConfig) {
			return getImage(BeansUIImages.getImage(
					BeansUIImages.IMG_OBJS_CONFIG), element, context);
		} else if (element instanceof IBeansConfigSet) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	private static Image getImage(Image baseImage, IModelElement element,
			IModelElement context) {
		int flags = getFlags(element, context);
		ImageDescriptor descriptor = new BeansModelCompositeImageDescriptor(
				baseImage, flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	public static Image getDecoratedImage(String baseImageKey, int flags) {
		return getDecoratedImage(BeansUIImages.getImage(baseImageKey), flags);
	}

	public static Image getDecoratedImage(Image baseImage, int flags) {
		ImageDescriptor descriptor = new BeansModelCompositeImageDescriptor(
				baseImage, flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	private static int getFlags(IModelElement element,
			IModelElement context) {
		int flags = 0;
		if (element instanceof IBeansConfig) {
			if (ModelUtils.isExternal(element, context)) {
				flags |= FLAG_EXTERNAL;
			}
		}
		return flags;
	}

	private static class BeansModelCompositeImageDescriptor extends
			AbstractCompositeImageDescriptor {

		public BeansModelCompositeImageDescriptor(Image baseImage,
				int flags) {
			super(baseImage, flags);
		}

		protected void drawOverlays() {
			if ((getFlags() & BeansModelImages.FLAG_EXTERNAL) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_EXTERNAL
						.getImageData();
				drawImage(data, getSize().x - data.width, getSize().y
						- data.height);
			}
		}
	}
}
