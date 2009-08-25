/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.model;

import org.springframework.beans.PropertyValue;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Marker subclass to indicate a {@link BeanProperty} that originates from annotation scanning.
 * @author Jared Rodriguez
 * @since 2.0.5
 */
public class AnnotatedProperty extends BeanProperty {

	public AnnotatedProperty(IBean bean, PropertyValue propValue) {
		super(bean, propValue);
	}
}
