/*******************************************************************************
 *  Copyright (c) 2012, 2017 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.core;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.metadata.model.IAnnotationBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.Annotation;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link IAnnotationBeanMetadataProvider} for creating bean metadata from
 * RequestMapping.
 * 
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class RequestMappingAnnotationMetadataProvider implements IAnnotationBeanMetadataProvider {

	/** The RequesMapping annotation class */
	private static final String REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.RequestMapping"; //$NON-NLS-1$
	private static final String GET_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.GetMapping"; //$NON-NLS-1$
	private static final String POST_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.PostMapping"; //$NON-NLS-1$
	private static final String PUT_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.PutMapping"; //$NON-NLS-1$
	private static final String DELETE_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.DeleteMapping"; //$NON-NLS-1$
	private static final String PATCH_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.PatchMapping"; //$NON-NLS-1$

	private static final String[] controllerClasses = new String[] {
			Controller.class.getName(),
			RestController.class.getName(),
			REQUEST_MAPPING_CLASS,
			"org.springframework.data.rest.webmvc.RepositoryRestController"
		};

	public Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type, IAnnotationMetadata visitor) {
		Set<IBeanMetadata> beanMetaDataSet = new LinkedHashSet<IBeanMetadata>();
		
		try {
			if (visitor.hasTypeLevelAnnotations(controllerClasses)) {
				Set<IMethodMetadata> methodMetaData = new HashSet<IMethodMetadata>();

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(REQUEST_MAPPING_CLASS).entrySet()) {
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							entry.getValue().getMembers(), new JavaModelSourceLocation(entry.getKey())));
				}

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(GET_REQUEST_MAPPING_CLASS).entrySet()) {
					Set<AnnotationMemberValuePair> members = entry.getValue().getMembers();
					members.add(new AnnotationMemberValuePair("method", "RequestMethod.GET"));
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(GET_REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							members, new JavaModelSourceLocation(entry.getKey())));
				}

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(POST_REQUEST_MAPPING_CLASS).entrySet()) {
					Set<AnnotationMemberValuePair> members = entry.getValue().getMembers();
					members.add(new AnnotationMemberValuePair("method", "RequestMethod.POST"));
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(POST_REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							members, new JavaModelSourceLocation(entry.getKey())));
				}

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(PUT_REQUEST_MAPPING_CLASS).entrySet()) {
					Set<AnnotationMemberValuePair> members = entry.getValue().getMembers();
					members.add(new AnnotationMemberValuePair("method", "RequestMethod.PUT"));
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(PUT_REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							members, new JavaModelSourceLocation(entry.getKey())));
				}

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(DELETE_REQUEST_MAPPING_CLASS).entrySet()) {
					Set<AnnotationMemberValuePair> members = entry.getValue().getMembers();
					members.add(new AnnotationMemberValuePair("method", "RequestMethod.DELETE"));
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(DELETE_REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							members, new JavaModelSourceLocation(entry.getKey())));
				}

				for (Map.Entry<IMethod, Annotation> entry : visitor.getMethodLevelAnnotations(PATCH_REQUEST_MAPPING_CLASS).entrySet()) {
					Set<AnnotationMemberValuePair> members = entry.getValue().getMembers();
					members.add(new AnnotationMemberValuePair("method", "RequestMethod.PATCH"));
					methodMetaData.add(new RequestMappingMethodAnnotationMetadata(PATCH_REQUEST_MAPPING_CLASS, entry.getKey().getHandleIdentifier(),
							members, new JavaModelSourceLocation(entry.getKey())));
				}

				beanMetaDataSet.add(new RequestMappingAnnotationMetadata(bean,
						REQUEST_MAPPING_CLASS, (visitor.hasTypeLevelAnnotations(REQUEST_MAPPING_CLASS)
								? visitor.getTypeLevelAnnotation(REQUEST_MAPPING_CLASS).getMembers() : null),
						new JavaModelSourceLocation(type), methodMetaData, type.getHandleIdentifier()));
			}
		} catch (JavaModelException e) {
		}
		return beanMetaDataSet;
	}
}
