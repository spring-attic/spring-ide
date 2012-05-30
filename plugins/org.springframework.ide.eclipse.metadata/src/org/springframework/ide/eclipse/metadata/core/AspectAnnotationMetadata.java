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

import org.aspectj.lang.annotation.Aspect;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IAnnotationMetadata} implementation that represents a single information from
 * {@link Aspect} annotation.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class AspectAnnotationMetadata extends AbstractAnnotationMetadata {

	private static final long serialVersionUID = 6978657032146327628L;

	public AspectAnnotationMetadata(IBean bean, String handle, Object value,
			IModelSourceLocation location, Set<IMethodMetadata> methodMetaData) {
		super(bean, handle, value, location, methodMetaData);
	}

}
