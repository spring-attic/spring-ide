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

import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractMethodAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IMethodMetadata} for RequestMapping annotation usage on the method level.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class RequestMappingMethodAnnotationMetadata extends AbstractMethodAnnotationMetadata {

	private static final long serialVersionUID = -825792015551282251L;

	public RequestMappingMethodAnnotationMetadata(String key, String handle, Object value,
			IModelSourceLocation location) {
		super(key, handle, value, location);
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
