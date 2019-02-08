/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.utils;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.live.model.AbstractLiveBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanRelation;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansResource;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

public class LiveBeanUtil {

	public static void navigateToResourceDefinition(AbstractLiveBeansModelElement element) {
		if (element instanceof LiveBean) {
			navigateToResourceDefinitionInBean((LiveBean) element);
		} else if (element instanceof LiveBeanRelation) {
			navigateToResourceDefinitionInBean(((LiveBeanRelation) element).getBean());
		} else if (element instanceof LiveBeansResource) {
			LiveBeansResource resource = (LiveBeansResource) element;
			navigateToResourceDefinition(resource);
		}
	}

	/**
	 * Navigates to the bean TYPE in the live bean. It does NOT look at the resource
	 * definition, UNLESS the type happens to be a proxy. In this case, it will
	 * navigate to the resource definition.
	 * 
	 * @param bean
	 */
	public static void navigateToBeanType(LiveBean bean) {
		TypeLookup appName = bean.getTypeLookup();
		String beanClass = bean.getBeanType();
		if (appName != null) {
			if (beanClass != null && beanClass.trim().length() > 0) {
				if (beanClass.startsWith("com.sun.proxy")) {
					// Special case for proxy beans, extract the type
					// from the resource field
					navigateToResourceDefinition(bean);
				} else {
					navigateToBeanType(appName, beanClass);
				}
			} else {
				// No type field, so infer class from bean ID
				navigateToBeanType(appName, bean.getId());
			}
		}
	}

	private static void navigateToResourceDefinition(LiveBeansResource resource) {
		TypeLookup typeLookup = resource.getTypeLookup();
		String resourceVal = null;
		if (resource.getAttributes() != null) {
			resourceVal = resource.getAttributes().get(LiveBean.ATTR_RESOURCE);
		}
		if (resourceVal == null) {
			resourceVal = resource.getLabel();
		}
		if (typeLookup != null && resourceVal != null) {
			navigateToResourceDefinition(typeLookup, resourceVal);
		}
	}

	/**
	 * Navigates to the resource definition in the bean, as opposed to the bean
	 * type. Even if the live bean may contain the bean type, this method
	 * specifically looks at the resource attribute in the live bean instead.
	 * </p>
	 * This is in contrast to {@link #navigateToBeanType(LiveBean)}, where
	 * navigation occurs to the TYPE instead of the the resource. NOTE that these
	 * two are not always the same.
	 * 
	 * @param bean
	 */
	public static void navigateToResourceDefinitionInBean(LiveBean bean) {
		TypeLookup session = bean.getTypeLookup();
		String resource = bean.getResource();
		navigateToResourceDefinition(session, resource);
	}

	private static void navigateToResourceDefinition(TypeLookup session, String resource) {
		if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
			String resourcePath = extractResourcePath(resource);
			if (resourcePath.endsWith(".class")) {
				navigateToBeanType(session, extractClassName(resourcePath));
			}
		}
	}

	private static void navigateToBeanType(TypeLookup workspaceContext, String className) {
		IType type = workspaceContext.findType(className);
		if (type != null) {
			SpringUIUtils.openInEditor(type);
		}
	}

	private static String extractClassName(String resourcePath) {
		int index = resourcePath.lastIndexOf("/WEB-INF/classes/");
		int length = "/WEB-INF/classes/".length();
		if (index >= 0) {
			resourcePath = resourcePath.substring(index + length);
		}
		resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf(".class"));
		resourcePath = resourcePath.replaceAll("\\\\|\\/", "."); // Tolerate both '/' and '\'.
		return resourcePath;
	}

	private static String extractResourcePath(String resourceStr) {
		// Extract the resource path out of the descriptive text
		int indexStart = resourceStr.indexOf("[");
		int indexEnd = resourceStr.indexOf("]");
		if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
			resourceStr = resourceStr.substring(indexStart + 1, indexEnd);
		}
		return resourceStr;
	}
}
