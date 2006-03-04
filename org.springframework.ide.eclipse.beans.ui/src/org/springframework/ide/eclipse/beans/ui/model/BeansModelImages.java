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

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class provides images for the beans core model's
 * <code>IModelElement</code>s.
 *
 * @see org.springframework.ide.eclipse.core.model.IModelElement
 *
 * @author Torsten Juergeleit
 */
public final class BeansModelImages {

	public static final int ELEMENT_PROJECT = 1;
	public static final int ELEMENT_CONFIG = 2;
	public static final int ELEMENT_CONFIG_SET = 3;
	public static final int ELEMENT_ALIAS = 4;
	public static final int ELEMENT_BEAN = 5;
	public static final int ELEMENT_CONSTRUCTOR_ARG = 6;
	public static final int ELEMENT_PROPERTY = 7;

	public static final int FLAG_EXTERNAL = 1 << 1;
	public static final int FLAG_CHILD = 1 << 2;
	public static final int FLAG_FACTORY = 1 << 3;
	public static final int FLAG_ABSTRACT = 1 << 4;
	public static final int FLAG_PROTOTYPE = 1 << 5;
	public static final int FLAG_LAZY_INIT = 1 << 6;

	private BeansModelImages() {
		// Don't instatiate
	}

	public static Image getImage(IModelElement element) {
		if (element instanceof IBeansProject) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROJECT);
		} else if (element instanceof IBeansConfig) {
			return getImage(BeansUIImages.getImage(
					BeansUIImages.IMG_OBJS_CONFIG), element);
		} else if (element instanceof IBeansConfigSet) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
		} else if (element instanceof IBeanAlias) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
		} else if (element instanceof IBean) {
			return getImage(BeansUIImages.getImage(
										BeansUIImages.IMG_OBJS_BEAN), element);
		} else if (element instanceof IBeanConstructorArgument) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		} else if (element instanceof IBeanProperty) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	private static Image getImage(Image baseImage, IModelElement element) {
		ImageDescriptor descriptor = new ElementImageDescriptor(baseImage,
																element);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	public static Image getImage(int element) {
		return getImage(element, 0);
	}

	public static Image getImage(int element, int flags) {
		ImageDescriptor descriptor = new FlagsImageDescriptor(element,
															  flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	public static Image getDecoratedImage(Image baseImage, int flags) {
		ImageDescriptor descriptor = new FlagsImageDescriptor(baseImage,
															  flags);
		return BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	private static class BaseImageDescriptor extends CompositeImageDescriptor {

		private Image baseImage;
		private Point size;

		public BaseImageDescriptor(Image baseImage) {
			this.baseImage = baseImage;
		}

		protected final Point getSize() {
			if (size == null) {
				ImageData data = baseImage.getImageData();
				size = new Point(data.width, data.height);
			}
			return size;
		}

		protected final void drawCompositeImage(int width, int height) {
			ImageData background = baseImage.getImageData();
			if (background == null) {
				background = DEFAULT_IMAGE_DATA;
			}
			drawImage(background, 0, 0);
			drawOverlays();
		}

		protected void drawOverlays() {
			// Do nothing
		}
	}

	private static class ElementImageDescriptor extends BaseImageDescriptor {

		private IModelElement element;

		public ElementImageDescriptor(Image baseImage, IModelElement element) {
			super(baseImage);
			this.element = element;
		}

		protected void drawOverlays() {
			ImageData data = null;
			if (element instanceof Bean) {
				Bean bean = (Bean) element;
				BeanDefinitionHolder bh = bean.getBeanDefinitionHolder();
				if (bean.isChildBean()) {
					data = BeansUIImages.DESC_OVR_CHILD.getImageData();
					drawImage(data, getSize().x - data.width, 0);
				} else if (bean.isRootBean() &&
								 ((RootBeanDefinition) bh.getBeanDefinition()).
								 getFactoryMethodName() != null) {
					data = BeansUIImages.DESC_OVR_FACTORY.getImageData();
					drawImage(data, getSize().x - data.width, 0);
				}
				if (bean.isAbstract()) {
					data = BeansUIImages.DESC_OVR_ABSTRACT.getImageData();
					drawImage(data, getSize().x - data.width, 0);
				}
				if (!bh.getBeanDefinition().isSingleton()) {
					data = BeansUIImages.DESC_OVR_PROTOTYPE.getImageData();
					drawImage(data, 0, 0);
				}
			}
		}
	}

	private static class FlagsImageDescriptor extends BaseImageDescriptor {
		private int flags;

		public FlagsImageDescriptor(int element, int flags) {
			super(getBaseImage(element));
			this.flags = flags;
		}

		public FlagsImageDescriptor(Image baseImage, int flags) {
			super(baseImage);
			this.flags = flags;
		}

		private static Image getBaseImage(int element) {
			String key;
			switch (element) {
				case ELEMENT_PROJECT :
					key = BeansUIImages.IMG_OBJS_PROJECT;
					break;

				case ELEMENT_CONFIG :
					key = BeansUIImages.IMG_OBJS_CONFIG;
					break;

				case ELEMENT_CONFIG_SET :
					key = BeansUIImages.IMG_OBJS_CONFIG_SET;
					break;

				case ELEMENT_ALIAS :
					key = BeansUIImages.IMG_OBJS_ALIAS;
					break;

				case ELEMENT_BEAN :
					key = BeansUIImages.IMG_OBJS_BEAN;
					break;

				case ELEMENT_CONSTRUCTOR_ARG :
					key = BeansUIImages.IMG_OBJS_CONSTRUCTOR;
					break;

				case ELEMENT_PROPERTY :
					key = BeansUIImages.IMG_OBJS_PROPERTY;
					break;

				default :
					key = BeansUIImages.IMG_OBJS_SPRING;
			}
			return BeansUIImages.getImage(key);
		}

		protected void drawOverlays() {
			ImageData data = null;
			if ((flags & FLAG_EXTERNAL) != 0) {
				data = BeansUIImages.DESC_OVR_EXTERNAL.getImageData();
				drawImage(data, getSize().x - data.width,
						  getSize().y - data.height);
			}
			if ((flags & FLAG_CHILD) != 0) {
				data = BeansUIImages.DESC_OVR_CHILD.getImageData();
				drawImage(data, getSize().x - data.width, 0);
			}
			if ((flags & FLAG_FACTORY) != 0) {
				data = BeansUIImages.DESC_OVR_FACTORY.getImageData();
				drawImage(data, getSize().x - data.width, 0);
			}
			if ((flags & FLAG_ABSTRACT) != 0) {
				data = BeansUIImages.DESC_OVR_ABSTRACT.getImageData();
				drawImage(data, getSize().x - data.width, 0);
			}
			if ((flags & FLAG_PROTOTYPE) != 0) {
				data = BeansUIImages.DESC_OVR_PROTOTYPE.getImageData();
				drawImage(data, 0, 0);
			}
		}
	}
}
