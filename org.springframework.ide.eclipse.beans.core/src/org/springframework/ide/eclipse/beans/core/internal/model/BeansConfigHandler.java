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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.ide.eclipse.beans.core.internal.parser.IBeanDefinitionEvents;
import org.springframework.ide.eclipse.beans.core.internal.parser.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IBeanDefinitionEvents</code> which populates an
 * instance of <code>IBeansConfig</code> with data from the the bean definition
 * events.
 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfig
 */
public class BeansConfigHandler implements IBeanDefinitionEvents {

	private IBeansConfig config;
	private List beans;
	private Map beansMap;
    private Stack nestedElements;
	private BeansModelElement currentElement;
	private List innerBeans;
    private Stack nestedBeans;
	private Bean currentBean;
	private Bean outerBean;

	public BeansConfigHandler(IBeansConfig config) {
		this.config = config;
		this.beans = new ArrayList();
		this.beansMap = new HashMap();
		this.nestedElements = new Stack();
		this.innerBeans = new ArrayList();
		this.nestedBeans = new Stack();
	}

	public final IBean getBean(String beanName) {
		if (beansMap.containsKey(beanName)) {
			return (IBean) beansMap.get(beanName);
		}
		return null;
	}

	public final List getBeans() {
		return beans;
	}

	public final List getInnerBeans() {
		return innerBeans;
	}

	public void startBean(Element element, boolean isNestedBean) {
		if (isNestedBean) {
			nestedElements.push(currentElement);
			nestedBeans.push(currentBean);
		}
		currentBean = new Bean(config);
		setXmlTextRange(currentBean, element);
		if (isNestedBean) {
			currentBean.setOuterBean(outerBean);
		} else {
			outerBean = currentBean;
		}
	}

	public void registerBean(BeanDefinitionHolder bdHolder,
							 boolean isNestedBean) {
		currentBean.setBeanDefinitionHolder(bdHolder);
		if (isNestedBean) {

			// Use current bean as an inner bean for the current constructor
			// argument or property
			currentElement = (BeansModelElement) nestedElements.pop();
			currentBean.setElementParent(currentElement);
			innerBeans.add(currentBean);
			outerBean.addInnerBean(currentBean);
			currentBean = (Bean) nestedBeans.pop();
		} else {
			beans.add(currentBean);
			beansMap.put(bdHolder.getBeanName(), currentBean);
		}
	}

	public void startConstructorArgument(Element element) {
		BeanConstructorArgument carg = new BeanConstructorArgument(currentBean);
		setXmlTextRange(carg, element);
		currentBean.addConstructorArgument(carg);
		currentElement = carg;
	}

	public void registerConstructorArgument(int index, Object value,
											String type) {
		BeanConstructorArgument carg = (BeanConstructorArgument) currentElement;
		carg.setIndex(index);
		carg.setValue(value);
		carg.setType(type);
		StringBuffer name = new StringBuffer();
		if (index != -1) {
			name.append(index);
			name.append(':');
		}
		if (type != null) {
			name.append(type);
			name.append(':');
		}
		name.append(value.toString());
		carg.setElementName(name.toString());
	}

	public void startProperty(Element element) {
		BeanProperty property = new BeanProperty(currentBean);
		setXmlTextRange(property, element);
		currentBean.addProperty(property);
		currentElement = property;
	}

	public void registerProperty(String name, PropertyValues pvs) {
		BeanProperty property = (BeanProperty) currentElement;
		property.setElementName(name);
		Object value = pvs.getPropertyValue(name).getValue();
		property.setValue(value);
	}

	/**
	 * Sets the start and end lines on the given model element.
     */
	private void setXmlTextRange(BeansModelElement modelElement,
								 Element xmlElement) {
		int startLine = LineNumberPreservingDOMParser.getStartLineNumber(xmlElement);
		int endLine = LineNumberPreservingDOMParser.getEndLineNumber(xmlElement);
		modelElement.setElementStartLine(startLine);
		modelElement.setElementEndLine(endLine);
    }
}
