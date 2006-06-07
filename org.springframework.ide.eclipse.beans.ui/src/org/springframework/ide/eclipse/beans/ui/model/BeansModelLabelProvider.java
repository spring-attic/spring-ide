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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;

/**
 * This class is an <code>ILabelProvider</code> which knows about the beans
 * core model's <code>IModelElement</code>s. If the given element is not of
 * type <code>IModelElement</code> the it tries to adapt it via
 * <code>IAdaptable</code>.
 *
 * @see org.springframework.ide.eclipse.core.model.IModelElement
 * @see org.eclipse.core.runtime.IAdaptable
 * @author Torsten Juergeleit
 */
public class BeansModelLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return BeansModelImages.getImage((IModelElement) adaptedElement);
		}
		return null;
	}

	public String getText(Object element) {
		Object adaptedElement = SpringCoreUtils.adaptToModelElement(element);
		if (adaptedElement instanceof IModelElement) {
			return getText((IModelElement) adaptedElement);
		}
		return null;
	}

	public static String getText(IModelElement element) {
		if (element instanceof IModelElement) {
			StringBuffer label = new StringBuffer();
			label.append(((IModelElement) element).getElementName());
			if (element instanceof IBean) {
				IBean bean = (IBean) element;
				if (bean.getAliases() != null && bean.getAliases().length > 0) {
					label.append(" '");
					label.append(StringUtils.arrayToDelimitedString(bean.getAliases(), ", "));
					label.append('\'');
				}
				if (bean.getClassName() != null) {
					label.append(" [");
					label.append(bean.getClassName());
					label.append(']');
				} else if (bean.getParentName() != null) {
					label.append(" <");
					label.append(bean.getParentName());
					label.append('>');
				}
			} else if (element instanceof IBeanProperty) {
				Object value = ((IBeanProperty) element).getValue();
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
		return element.toString();
	}
}
