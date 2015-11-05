/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.blueprint.config.internal.support;

import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * A temporary runtime bean reference that does not implement the equality contract to prevent set merges.
 * 
 * @author Costin Leau
 */
//FIXME: delete when SPR-5861 is fixed
public class InstanceEqualityRuntimeBeanReference extends RuntimeBeanReference {

	public InstanceEqualityRuntimeBeanReference(String beanName, boolean toParent) {
		super(beanName, toParent);
	}

	public InstanceEqualityRuntimeBeanReference(String beanName) {
		super(beanName);
	}

	@Override
	public boolean equals(Object other) {
		return this == other;
	}
}
