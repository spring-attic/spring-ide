/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Model element that holds data for a {@link ReplaceOverride}. 
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanReplaceMethodOverride extends AbstractBeanMethodOverride {
	
	public BeanReplaceMethodOverride(IBean bean, ReplaceOverride methodOverride) {
		super(bean, methodOverride.getMethodReplacerBeanName(), methodOverride);
	}

	public TYPE getType() {
		return TYPE.REPLACE;
	}
	
	
}
