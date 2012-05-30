/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.springiderules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReference;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansTypedString;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;

/**
 * This rule checks for cases where it may be possible to simplify the
 * configuration by using bean inheritance. Using bean inheritance is suggested
 * when there are more than <code>DEFAULT_MIN_NUM_SIMILAR_BEAN_DEFS</code> beans
 * with <code>DEFAULT_MIN_NUM_SHARED_PROPERTIES</code> properties in common
 * where the values of the properties are the same.
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Leo Dos Santos
 */
public class UseBeanInheritance implements IValidationRule<IBean, IBeansValidationContext> {

	private final static int DEFAULT_MIN_NUM_SIMILAR_BEAN_DEFS = 3;

	private final static int DEFAULT_MIN_NUM_SHARED_PROPERTIES = 3;

	public final static String ERROR_ID = "useBeanInheritance";

	private int minNumSimilarBeanDefs = DEFAULT_MIN_NUM_SIMILAR_BEAN_DEFS;

	private int minNumSharedProperties = DEFAULT_MIN_NUM_SHARED_PROPERTIES;

	private final List<IBean> beanList = new ArrayList<IBean>();

	public void setMinNumSharedProperties(int minNumSharedProperties) {
		this.minNumSharedProperties = minNumSharedProperties;
	}

	public void setMinNumSimilarBeanDefs(int minNumSimilarBeanDefs) {
		this.minNumSimilarBeanDefs = minNumSimilarBeanDefs;
	}

	/**
	 * Returns <code>true</code> if this rule is able to validate the given
	 * {@link IModelElement} with the specified {@link IValidationContext}.
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBean && isBeanSupported((IBean) element);
	}

	/**
	 * Check if there are other beans with similar configuration.
	 */
	public void validate(IBean bean, IBeansValidationContext validationContext, IProgressMonitor progressMonitor) {

		List<IBean> similarBeanList = new ArrayList<IBean>();
		for (IBean currBean : beanList) {
			if (isSimilar(bean, currBean)) {
				similarBeanList.add(currBean);
			}
		}
		beanList.add(bean);

		// Add one to the similar bean count because the current bean counts as
		// one of the similar ones
		if (similarBeanList.size() + 1 >= minNumSimilarBeanDefs) {
			String similarBeanNames = getBeanNamesString(similarBeanList);
			validationContext
					.info(
							bean,
							ERROR_ID,
							"Consider using bean inheritance to simplify configuration of the "
									+ bean.getElementName()
									+ " bean. It may be possible to use a parent bean to share configuration with the the following beans: "
									+ similarBeanNames);
		}
	}

	private boolean constructorArgumentsEqual(IBean bean1, IBean bean2) {

		Set<IBeanConstructorArgument> bean1args = bean1.getConstructorArguments();
		Set<IBeanConstructorArgument> bean2args = bean2.getConstructorArguments();
		if (bean1args.size() != bean2args.size()) {
			return false;
		}

		for (IBeanConstructorArgument currBean1ConstructorArgument : bean1args) {
			boolean matchFound = false;
			for (IBeanConstructorArgument currBean2ConstructorArgument : bean2args) {
				if (constructorArgumentsEqual(currBean1ConstructorArgument, currBean2ConstructorArgument)) {
					matchFound = true;
					break;
				}
			}
			if (!matchFound) {
				return false;
			}
		}

		return true;
	}

	private boolean constructorArgumentsEqual(IBeanConstructorArgument argument1, IBeanConstructorArgument argument2) {
		if (argument1.getElementName().equals(argument2.getElementName())) {
			if (propertyValuesEqual(argument1.getValue(), argument2.getValue())) {
				return true;
			}
		}
		return false;
	}

	private String getBeanNamesString(List<IBean> similarBeanList) {
		String beanNames = "";
		for (IBean bean : similarBeanList) {
			beanNames += bean.getElementName() + " ";
		}
		return beanNames;
	}

	private boolean initMethodsEqual(IBean bean1, IBean bean2) {

		if (bean1 instanceof Bean && bean2 instanceof Bean) {
			AbstractBeanDefinition definition1 = (AbstractBeanDefinition) ((Bean) bean1).getBeanDefinition();
			AbstractBeanDefinition definition2 = (AbstractBeanDefinition) ((Bean) bean2).getBeanDefinition();
			String initMethod1 = definition1.getInitMethodName();
			String initMethod2 = definition2.getInitMethodName();
			if (initMethod1 == null) {
				initMethod1 = "";
			}
			if (initMethod2 == null) {
				initMethod2 = "";
			}
			if (!initMethod1.equals(initMethod2)) {
				return false;
			}
		}
		return true;
	}

	private boolean isBeanSupported(IBean bean) {
		if (bean.getElementSourceLocation() instanceof XmlSourceLocation
				&& !NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(((XmlSourceLocation) bean.getElementSourceLocation())
						.getNamespaceURI())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if two beans are similar in the sense that some common
	 * configuration can be factored out into a parent bean configuration.
	 * 
	 * Beans are considered similar if they don't have different constructor
	 * arguments or init methods and there are more than
	 * <code>DEFAULT_MIN_NUM_SHARED_PROPERTIES</code> property-value pairs in
	 * common
	 */
	private boolean isSimilar(IBean bean1, IBean bean2) {

		if (!isBeanSupported(bean2)) {
			return false;
		}

		if (!constructorArgumentsEqual(bean1, bean2)) {
			return false;
		}

		if (!initMethodsEqual(bean1, bean2)) {
			return false;
		}

		// Count the number of matching properties
		int matchingPropertyCount = 0;
		for (IBeanProperty currProperty1 : bean1.getProperties()) {
			for (IBeanProperty currProperty2 : bean2.getProperties()) {
				if (propertiesEqual(currProperty1, currProperty2)) {
					matchingPropertyCount++;
				}
			}
		}

		return matchingPropertyCount >= minNumSharedProperties;
	}

	/**
	 * Two properties are considered equal if they have they refer to the same
	 * property of the bean (element name) and their values are the same.
	 * @return true if the two properties are the same
	 */
	private boolean propertiesEqual(IBeanProperty beanProperty1, IBeanProperty beanProperty2) {
		if (beanProperty1.getElementName().equals(beanProperty2.getElementName())) {
			if (propertyValuesEqual(beanProperty1.getValue(), beanProperty2.getValue())) {
				return true;
			}
		}
		return false;
	}

	private boolean propertyValuesEqual(Object value1, Object value2) {
		if (value1 instanceof BeansTypedString && value2 instanceof BeansTypedString) {
			BeansTypedString beansTypedString1 = (BeansTypedString) value1;
			BeansTypedString beansTypedString2 = (BeansTypedString) value2;

			if (beansTypedString1.getString().equals(beansTypedString2.getString())) {
				return true;
			}
		}
		else if (value1 instanceof BeanReference && value2 instanceof BeanReference) {
			BeanReference beanReference1 = (BeanReference) value1;
			BeanReference beanReference2 = (BeanReference) value2;
			if (beanReference1.getBeanName().equals(beanReference2.getBeanName())) {
				return true;
			}
		}
		return false;
	}
}
