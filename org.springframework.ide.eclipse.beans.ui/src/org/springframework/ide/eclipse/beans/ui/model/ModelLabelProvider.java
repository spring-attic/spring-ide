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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

public class ModelLabelProvider extends LabelProvider {

	public Image getImage(Object obj) {
		if (obj instanceof ProjectNode) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROJECT);
		} else if (obj instanceof ConfigSetNode) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
		} else if (obj instanceof ConfigNode) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG);
		} else if (obj instanceof BeanNode) {
			BeanNode bean = (BeanNode) obj;
			if (bean.getParentName() != null) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CHILD_BEAN);
			} else {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
			}
		} else if (obj instanceof ConstructorArgumentNode) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		} else if (obj instanceof PropertyNode) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof INode) {
			StringBuffer label = new StringBuffer();
			label.append(((INode) element).getName());
			if (element instanceof BeanNode) {
				BeanNode bean = (BeanNode) element;
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
				Object value = ((PropertyNode) element).getValue();
				if (value instanceof String) {
					label.append(" \"");
					label.append(value);
					label.append('"');
				} if (value instanceof BeanDefinitionHolder) {
					BeanDefinitionHolder bdHolder = (BeanDefinitionHolder)
																		  value;
					BeanDefinition beanDef = bdHolder.getBeanDefinition();
					label.append(" {");
					label.append(bdHolder.getBeanName());
					if (beanDef instanceof RootBeanDefinition) {
						label.append(" [");
						label.append(((RootBeanDefinition)
												   beanDef).getBeanClassName());
						label.append(']');
					} else {
						label.append(" <");
						label.append(((ChildBeanDefinition)
													  beanDef).getParentName());
						label.append('>');
					}
					label.append('}');
				} else {
					label.append(' ');
					label.append(value);
				}
			}
			return label.toString();
		}
		return super.getText(element);
	}
}
