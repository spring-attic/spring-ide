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

package org.springframework.ide.eclipse.beans.ui.namespaces.beans;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.ui.AbstractCompositeImageDescriptor;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides images for the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/beans"</code>.
 * 
 * @author Torsten Juergeleit
 */
public class BeansNamespaceImages {

    public static final int FLAG_EXTERNAL = 1 << 1;
    public static final int FLAG_CHILD = 1 << 2;
    public static final int FLAG_FACTORY = 1 << 3;
    public static final int FLAG_ABSTRACT = 1 << 4;
    public static final int FLAG_PROTOTYPE = 1 << 5;
    public static final int FLAG_LAZY_INIT = 1 << 6;

	public static Image getImage(ISourceModelElement element) {
		return getImage(element, null);
	}

	public static Image getImage(ISourceModelElement element,
			IModelElement context) {
		if (element instanceof IBeansImport) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_IMPORT);
		} else if (element instanceof IBeanAlias) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
		} else if (element instanceof IBean) {
			return getImage(BeansUIImages.getImage(
					BeansUIImages.IMG_OBJS_BEAN), element, context);
		} else if (element instanceof IBeanConstructorArgument) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		} else if (element instanceof IBeanProperty) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	private static Image getImage(Image baseImage, ISourceModelElement element,
			IModelElement context) {
		int flags = getFlags(element, context);
		ImageDescriptor descriptor = new BeansNamespaceCompositeImageDescriptor(
				baseImage, flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	public static Image getDecoratedImage(Image baseImage, int flags) {
		ImageDescriptor descriptor = new BeansNamespaceCompositeImageDescriptor(
				baseImage, flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	private static int getFlags(ISourceModelElement element,
			IModelElement context) {
		int flags = 0;
		if (element instanceof Bean) {
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

	private static class BeansNamespaceCompositeImageDescriptor extends
			AbstractCompositeImageDescriptor {

		public BeansNamespaceCompositeImageDescriptor(Image baseImage,
				int flags) {
			super(baseImage, flags);
		}

		protected void drawOverlays() {
			int flags = getFlags();
			if ((flags & BeansModelImages.FLAG_EXTERNAL) != 0) {
				ImageData data = BeansUIImages.DESC_OVR_EXTERNAL
						.getImageData();
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
				ImageData data = BeansUIImages.DESC_OVR_ABSTRACT
						.getImageData();
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
