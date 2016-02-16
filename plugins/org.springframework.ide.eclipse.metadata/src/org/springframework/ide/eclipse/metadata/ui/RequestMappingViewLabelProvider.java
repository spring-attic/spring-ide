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
package org.springframework.ide.eclipse.metadata.ui;

import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingMethodAnnotationMetadata;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class RequestMappingViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private AntPathMatcher matcher;

	private JavaElementLabelProvider javaLabelProvider;

	public RequestMappingViewLabelProvider() {
		matcher = new AntPathMatcher();
		javaLabelProvider = new JavaElementLabelProvider();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof RequestMappingMethodToClassMap) {
			if (columnIndex == RequestMappingView.COLUMN_HANDLER_METHOD) {
				RequestMappingMethodAnnotationMetadata annotation = ((RequestMappingMethodToClassMap) element)
						.getMethodMetadata();
				IMethod method = (IMethod) JdtUtils.getByHandle(annotation
						.getHandleIdentifier());
				return javaLabelProvider.getImage(method);
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof RequestMappingAnnotationMetadata) {
			if (columnIndex == RequestMappingView.COLUMN_URL) {
				return getClassUrl((RequestMappingAnnotationMetadata) element);
			}
		}
		if (element instanceof RequestMappingMethodToClassMap) {
			if (columnIndex == RequestMappingView.COLUMN_URL) {
				return getMethodUrl((RequestMappingMethodToClassMap) element);
			}
			if (columnIndex == RequestMappingView.COLUMN_REQUEST_METHOD) {
				return getRequestMethod((RequestMappingMethodToClassMap) element);
			}
			if (columnIndex == RequestMappingView.COLUMN_HANDLER_METHOD) {
				return getHandlerMethod((RequestMappingMethodToClassMap) element);
			}
		}
		return ""; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private String getClassUrl(RequestMappingAnnotationMetadata annotation) {
		Object value = annotation.getValue();
		if (value instanceof Set) {
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) value) {
				if (pair.getName() == null) {
					String url = pair.getValue();
					if (url.endsWith("/**")) { //$NON-NLS-1$
						url = url.substring(0, url.length() - 3);
					}
					if (!url.startsWith("/")) { //$NON-NLS-1$
						url = "/".concat(url); //$NON-NLS-1$
					}
					return url;
				}
			}
		}
		return "/"; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private String getMethodUrl(RequestMappingMethodToClassMap map) {
		RequestMappingMethodAnnotationMetadata annotation = map
				.getMethodMetadata();
		String classUrl = getClassUrl(map.getClassMetadata());
		Object value = annotation.getValue();
		if (value instanceof Set) {
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) value) {
				if (pair.getName() == null) {
					if (classUrl.equals("/") && pair.getValue().startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
						return pair.getValue();
					}
					String url = matcher.combine(classUrl, pair.getValue());
					if (url.contains("/**/")) { //$NON-NLS-1$
						url = StringUtils.delete(url, "/**"); //$NON-NLS-1$
					}
					return url;
				}
			}
		}
		return classUrl;
	}

	@SuppressWarnings("unchecked")
	private String extractMethodFromAnnotation(IBeanMetadata metadata) {
		Object value = metadata.getValue();
		if (value instanceof Set) {
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) value) {
				if ("method".equalsIgnoreCase(pair.getName())) { //$NON-NLS-1$
					String method = pair.getValue();
					// normalize result by omitting RequestMethod.*** and just showing *** 
					if (method.startsWith("RequestMethod.")) {
						return method.substring("RequestMethod.".length());
					}
					return method;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private String getRequestMethod(RequestMappingMethodToClassMap map) {
		RequestMappingMethodAnnotationMetadata annotation = map
				.getMethodMetadata();
		String classMethod = extractMethodFromAnnotation(map.getClassMetadata());
		String requestMethod = extractMethodFromAnnotation(annotation);
		if (requestMethod != null) {
			return requestMethod;
		} else if (classMethod != null) {
			return classMethod;
		}
		return ""; //$NON-NLS-1$
	}
	
	private String getHandlerMethod(RequestMappingMethodToClassMap map) {
		RequestMappingMethodAnnotationMetadata annotation = map
				.getMethodMetadata();
		IMethod method = (IMethod) JdtUtils.getByHandle(annotation
				.getHandleIdentifier());
		return javaLabelProvider.getText(method.getDeclaringType()) + "." //$NON-NLS-1$
				+ javaLabelProvider.getText(method);
	}

}
