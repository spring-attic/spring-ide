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

package org.springframework.ide.eclipse.beans.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class is an <code>ILabelProvider</code> which knows about the beans
 * core model's <code>IModelElement</code>s. If the given element is not of
 * type <code>IModelElement</code> the it tries to adapt it via
 * <code>IAdaptable</code>.
 *
 * @see org.springframework.ide.eclipse.core.model.IModelElement
 * @see org.eclipse.core.runtime.IAdaptable
 *
 * @author Torsten Juergeleit
 */
public class BeansModelLabelProvider extends LabelProvider {

	public Image getImage(Object element) {

		// At first try to adapt given element to IModelElement 
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);

		// Now check if given object is a member of the beans core model
		if (adaptedElement instanceof IBeansProject) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROJECT);
		} else if (adaptedElement instanceof IBeansConfigSet) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG_SET);
		} else if (adaptedElement instanceof IBeansConfig) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG);
		} else if (adaptedElement instanceof IBeanAlias) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
		} else if (adaptedElement instanceof IBean) {
			if (((IBean) adaptedElement).isRootBean()) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
			} else {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CHILD_BEAN);
			}
		} else if (adaptedElement instanceof IBeanConstructorArgument) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
		} else if (adaptedElement instanceof IBeanProperty) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	public String getText(Object element) {

		// At first try to adapt given element to IModelElement 
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);

		// Now check if given object is a member of the beans core model
		if (adaptedElement instanceof IModelElement) {
			StringBuffer label = new StringBuffer();
			label.append(((IModelElement) adaptedElement).getElementName());
			if (adaptedElement instanceof IBean) {
				IBean bean = (IBean) adaptedElement;
				if (bean.getClassName() != null) {
					label.append(" [");
					label.append(bean.getClassName());
					label.append(']');
				} else if (bean.getParentName() != null) {
					label.append(" <");
					label.append(bean.getParentName());
					label.append('>');
				}
			} else if (adaptedElement instanceof IBeanProperty) {
				Object value = ((IBeanProperty) adaptedElement).getValue();
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
			}
			return label.toString();
		}
		return super.getText(element);
	}
}
