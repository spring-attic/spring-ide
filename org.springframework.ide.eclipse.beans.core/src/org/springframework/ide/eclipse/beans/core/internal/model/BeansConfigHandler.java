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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
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
	private List innerBeans;
    private Stack nestedBeans;
	private Bean currentBean;

	public BeansConfigHandler(IBeansConfig config) {
		this.config = config;
		this.beans = new ArrayList();
		this.beansMap = new HashMap();
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
			nestedBeans.push(currentBean);
		}
		currentBean = new Bean(config, "");
		setXmlTextRange(currentBean, element);
		
	}

	/**
	 * sets the start and end lines on the given model element.
     * @param currentBean2
     * @param element
     */
    private void setXmlTextRange(BeansModelElement modelElement, Element xmlElement)
    {
		int startLine = LineNumberPreservingDOMParser.getStartLineNumber(xmlElement);
		int endLine = LineNumberPreservingDOMParser.getEndLineNumber(xmlElement);
		modelElement.setElementStartLine(startLine);
		modelElement.setElementEndLine(endLine);
    }

    public void registerConstructorArgument(Element element, int index,
										   Object value, String type) {
		BeanConstructorArgument carg = new BeanConstructorArgument(currentBean, index,
														   type, value);
		currentBean.addConstructorArgument(carg);
		setXmlTextRange(carg, element);
	}

	public void registerBeanProperty(Element element, String propertyName,
									 PropertyValues pvs) {
		BeanProperty property = new BeanProperty(currentBean, propertyName);
		property.setValue(pvs.getPropertyValue(propertyName).getValue());
		currentBean.addProperty(property);
		setXmlTextRange(property, element);
	}

	public void registerBean(BeanDefinitionHolder bdHolder,
							 boolean isNestedBean) {
		BeanDefinition beanDef = bdHolder.getBeanDefinition();
		String name;
		if (isNestedBean) {
			if (beanDef instanceof RootBeanDefinition) {
				name = ((RootBeanDefinition) beanDef).getBeanClassName();
			} else {
				name = "Child bean with parent '" +
						  ((ChildBeanDefinition) beanDef).getParentName() + "'";
			}
		} else {
			name = bdHolder.getBeanName();
		}
		currentBean.setElementName(name);
		currentBean.setBeanDefinition(beanDef);
		currentBean.setAliases(bdHolder.getAliases());
		if (isNestedBean) {
			innerBeans.add(currentBean);
			currentBean = (Bean) nestedBeans.pop();
		} else {
			beans.add(currentBean);
			if (name != null && name.length() > 0) {
				beansMap.put(name, currentBean);
			}
		}
	}
}
