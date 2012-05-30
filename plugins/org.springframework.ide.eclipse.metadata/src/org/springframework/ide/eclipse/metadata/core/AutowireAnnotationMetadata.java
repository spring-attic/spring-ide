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

import org.springframework.context.annotation.Configuration;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IAnnotationMetadata} implementation that represents a single information from {@link Configuration} annotation.
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class AutowireAnnotationMetadata extends AbstractAnnotationMetadata {

	private static final long serialVersionUID = 2292067179938558318L;

	public AutowireAnnotationMetadata(IBean bean, String handle, IModelSourceLocation location,
			Set<IMethodMetadata> methodMetaData) {
		super(bean, handle, null, location, methodMetaData);
	}

}
