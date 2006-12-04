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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;

/**
 * This class is an <code>ILabelProvider</code> which knows about the view
 * model's <code>INode</code>s.
 *
 * @see org.springframework.ide.eclipse.beans.ui.views.model.INode
 *
 * @author Torsten Juergeleit
 */
public class ModelLabelProvider extends LabelProvider {

	public Image getImage(Object obj) {
		INode node = (INode) obj;

		// Find base image for given node
		Image image;
		if (obj instanceof ProjectNode) {
			image = BeansModelImages.getImage(BeansModelImages.ELEMENT_PROJECT);
		} else if (obj instanceof ConfigSetNode) {
			image = BeansModelImages.getImage(
										  BeansModelImages.ELEMENT_CONFIG_SET);
		} else if (obj instanceof ConfigNode) {
			image = BeansModelImages.getImage(BeansModelImages.ELEMENT_CONFIG);
		} else if (obj instanceof BeanNode) {
			image = BeansModelImages.getImage(((BeanNode) obj).getElement());
		} else if (obj instanceof ConstructorArgumentNode) {
			image = BeansModelImages.getImage(
									 BeansModelImages.ELEMENT_CONSTRUCTOR_ARG);
		} else if (obj instanceof PropertyNode) {
			image = BeansModelImages.getImage(
											BeansModelImages.ELEMENT_PROPERTY);
		} else {
			image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
		}

		// Decorate images of externally defined configs or beans
		if (node instanceof ConfigNode && node.getName().charAt(0) == '/' ||
				node instanceof BeanNode &&
				((BeanNode) node).getConfigNode().getName().charAt(0) == '/') {
			image = BeansModelImages.getDecoratedImage(image,
											   BeansModelImages.FLAG_EXTERNAL);
		}

		// Decorate images of nodes with flags (error, warning, ...)
		int flags = node.getFlags();
		if (flags != 0) {
			ImageDescriptor descriptor = new ModelImageDescriptor(image, flags);
			image = BeansUIPlugin.getImageDescriptorRegistry().get(descriptor);
		}
		return image;
	}

	public String getText(Object element) {
		if (element instanceof INode) {
			StringBuffer label = new StringBuffer();
			if (element instanceof ConfigNode) {
				ConfigNode config = (ConfigNode) element;
				String configName = config.getName();
				if (config.getConfig().isElementArchived()) {
					ZipEntryStorage storage = new ZipEntryStorage(config
							.getConfig());
					if (configName.charAt(0) == IBeansConfig
							.EXTERNAL_FILE_NAME_PREFIX) {
						label.append(storage.getZipResource()
								.getFullPath().toString());
					} else {
						label.append(storage.getZipResource()
								.getProjectRelativePath().toString());
					}
					label.append(" - ");
					label.append(storage.getFullPath().toString());
				} else {
					label.append(config.getName());
				}
			} else if (element instanceof BeanNode) {
				BeanNode bean = (BeanNode) element;
				label.append(bean.getName());
				if (bean.getClassName() != null) {
					label.append(" [");
					label.append(bean.getClassName());
					label.append(']');
				} else if (bean.getParentName() != null) {
					label.append(" <");
					label.append(bean.getParentName());
					label.append('>');
				}
			} else if (element instanceof PropertyNode) {
				PropertyNode property = (PropertyNode) element;
				label.append(property.getName());
				Object value = property.getValue();
				if (value instanceof String) {
					label.append(" \"");
					label.append(value);
					label.append('"');
				} else if (value instanceof BeanDefinitionHolder) {
					BeanDefinition beanDef = ((BeanDefinitionHolder) value)
							.getBeanDefinition();
					label.append(" {");
					if (beanDef instanceof RootBeanDefinition) {
						label.append('[');
						label.append(((RootBeanDefinition)
												   beanDef).getBeanClassName());
						label.append(']');
					} else {
						label.append('<');
						label.append(((ChildBeanDefinition)
													  beanDef).getParentName());
						label.append('>');
					}
					label.append('}');
				} else {
					label.append(' ');
					label.append(value);
				}
			} else {
				label.append(((INode) element).getName());
			}
			return label.toString();
		}
		return super.getText(element);
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
