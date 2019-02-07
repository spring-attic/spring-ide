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

	public static void openInEditor(AbstractLiveBeansModelElement element) {
		if (element instanceof LiveBean) {
			openInEditor((LiveBean) element);
		} else if (element instanceof LiveBeanRelation) {
			openInEditor(((LiveBeanRelation) element).getBean());
		} else if (element instanceof LiveBeansResource) {
			LiveBeansResource resource = (LiveBeansResource) element;
			openInEditorFromResource(resource.getTypeLookup(), resource.getLabel());
		}
	}

	public static void openInEditor(LiveBean bean) {
		TypeLookup appName = bean.getTypeLookup();
		String beanClass = bean.getBeanType();
		if (appName != null) {
			if (beanClass != null && beanClass.trim().length() > 0) {
				if (beanClass.startsWith("com.sun.proxy")) {
					// Special case for proxy beans, extract the type
					// from the resource field
					openInEditorFromResource(bean);
				} else {
					openInEditor(appName, beanClass);
				}
			} else {
				// No type field, so infer class from bean ID
				openInEditor(appName, bean.getId());
			}
		}
	}

	public static void openInEditorFromResource(LiveBean bean) {
		TypeLookup session = bean.getTypeLookup();
		String resource = bean.getResource();
		openInEditorFromResource(session, resource);
	}

	private static void openInEditorFromResource(TypeLookup session, String resource) {
		if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
			String resourcePath = LiveBeanUtil.extractResourcePath(resource);
			if (resourcePath.endsWith(".class")) {
				LiveBeanUtil.openInEditor(session, LiveBeanUtil.extractClassName(resourcePath));
			}
		}
	}

	private static void openInEditor(TypeLookup workspaceContext, String className) {
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
