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
package org.springframework.ide.eclipse.metadata.core;

import java.util.Set;

import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IBeanMetadata} for the RequestMapping annotation.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class RequestMappingAnnotationMetadata extends AbstractAnnotationMetadata {

	private static final long serialVersionUID = 6978657032146327628L;
	
	private String classHandle = null;
	
	public RequestMappingAnnotationMetadata(IBean bean, String handle, Object value,
			IModelSourceLocation location, Set<IMethodMetadata> methodMetaData, String classHandle) {
		super(bean, handle, value, location, methodMetaData);
		this.classHandle = classHandle;
	}
	
	@Override
	public String getClassHandle() {
		return classHandle;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String getValueAsText() {
		if (getValue() instanceof Set) {
			StringBuilder buf = new StringBuilder();
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) getValue()) {
				if (pair.getName() != null) {
					buf.append(pair.getName());
					buf.append(" = "); //$NON-NLS-1$
				}
				buf.append(pair.getValue().toString());
				buf.append(", "); //$NON-NLS-1$
			}

			if (buf.length() > 0) {
				return buf.substring(0, buf.length() - 2) + " -> "; //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
	
}
