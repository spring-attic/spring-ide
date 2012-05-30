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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractMethodAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IMethodMetadata} for Spring 3.0 {@link Bean} method level annotations.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class BeanMethodAnnotationMetadata extends AbstractMethodAnnotationMetadata {

	private static final long serialVersionUID = 4590913995747027452L;

	public BeanMethodAnnotationMetadata(String key, String handle, Object value, IModelSourceLocation location) {
		super(key, handle, value, location);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getValueAsText() {
		StringBuilder buf = new StringBuilder();
		IJavaElement je =  JavaCore.create(getHandleIdentifier());
		if (getValue() instanceof Set) {

			Set<AnnotationMemberValuePair> pairs = new HashSet<AnnotationMemberValuePair>(
					(Set<AnnotationMemberValuePair>) getValue());

			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) getValue()) {
				if ("name".equals(pair.getName())) { //$NON-NLS-1$
					buf.append(pair.getValue());
					pairs.remove(pair);
				}
			}
			
			if (buf.length() == 0) {
				buf.append(je.getElementName());
			}
			
			if (pairs.size() > 0) {
				buf.append(" ("); //$NON-NLS-1$
			}
			
			for (AnnotationMemberValuePair pair : pairs) {
				if (pair.getName() != null) {
					buf.append(pair.getName());
					buf.append(" = "); //$NON-NLS-1$
				}
				buf.append(pair.getValue());
				buf.append(", "); //$NON-NLS-1$
			}
			
			if (pairs.size() > 0) {
				buf = new StringBuilder(buf.substring(0, buf.length() - 2));
				buf.append(")"); //$NON-NLS-1$
			}
			
			return buf.append(" - ").toString(); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

}
