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

import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractMethodAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link IMethodMetadata} for {@link Transactional} annotation usage on the method level.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class TransactionalMethodAnnotationMetadata extends AbstractMethodAnnotationMetadata {

	private static final long serialVersionUID = -5855055542136631913L;

	public TransactionalMethodAnnotationMetadata(String key, String handle, Object value,
			IModelSourceLocation location) {
		super(key, handle, value, location);
	}

}
