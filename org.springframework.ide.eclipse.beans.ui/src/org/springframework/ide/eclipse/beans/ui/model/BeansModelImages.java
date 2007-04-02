/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansList;
import org.springframework.ide.eclipse.beans.core.model.IBeansMap;
import org.springframework.ide.eclipse.beans.core.model.IBeansMapEntry;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProperties;
import org.springframework.ide.eclipse.beans.core.model.IBeansSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.ui.AbstractCompositeImageDescriptor;

/**
 * This class provides images for the beans core model's {@link IModelElement}s.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansModelImages implements BeansModelImageFlags {

	public static Image getImage(IModelElement element) {
		return getImage(element, null);
	}

	public static Image getImage(IModelElement element,
			IModelElement context) {
		if (element instanceof IBeansProject) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROJECT);
		} else if (element instanceof IBeansConfig) {
			return getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_CONFIG), element, context);
		} else if (element instanceof IBeansConfigSet) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
		} else if (element instanceof IBeansImport) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_IMPORT);
		} else if (element instanceof IBeanAlias) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
		} else if (element instanceof IBean) {
			return getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_BEAN), element, context);
		} else if (element instanceof IBeanConstructorArgument) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		} else if (element instanceof IBeanProperty) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		} else if (element instanceof IBeansList || element instanceof IBeansSet
				|| element instanceof IBeansMap
				|| element instanceof IBeansMapEntry
				|| element instanceof IBeansProperties) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
		} else if (element instanceof IBeanReference) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
		} else if (element instanceof IBeansTypedString) {
			if (element.getElementParent() instanceof IBeansMapEntry
					&& element.equals(((IBeansMapEntry) element
							.getElementParent()).getKey())) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_KEY);
			} else {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_VALUE);
			}
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	public static Image getDecoratedImage(Image baseImage,
			IModelElement element, IModelElement context) {
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
		} else if (element instanceof Bean) {
			Bean bean = (Bean) element;
			if (ModelUtils.isExternal(element, context)) {
				flags |= FLAG_EXTERNAL;
			}
			BeanDefinition bd = bean.getBeanDefinition();
			if (bean.isChildBean()) {
				flags |= FLAG_CHILD;
			}
			if (bean.isFactory()) {
				flags |= FLAG_FACTORY;
			}
			if (bean.isAbstract()) {
				flags |= FLAG_ABSTRACT;
			}
			if (!bd.isSingleton()) {
				flags |= FLAG_PROTOTYPE;
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

		@Override
		protected void drawOverlays() {
			int flags = getFlags();
			if ((flags & BeansModelImages.FLAG_WARNING) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_WARNING.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			}
			if ((flags & BeansModelImages.FLAG_ERROR) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_ERROR.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			}
			if ((flags & BeansModelImages.FLAG_EXTERNAL) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_EXTERNAL.getImageData();
				drawImage(data, getSize().x - data.width, getSize().y
						- data.height);
			}
			if ((flags & FLAG_CHILD) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_CHILD.getImageData();
				drawImage(data, getSize().x - data.width, 0);
			}
			if ((flags & FLAG_FACTORY) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_FACTORY.getImageData();
				drawImage(data, 0, 0);
			}
			if ((flags & FLAG_ABSTRACT) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_ABSTRACT.getImageData();
				drawImage(data, getSize().x - data.width, 0);
			}
			if ((flags & FLAG_PROTOTYPE) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_PROTOTYPE
						.getImageData();
				drawImage(data, getSize().x / 2 - data.width / 2 - 1, 0);
			}
		}
	}
}
