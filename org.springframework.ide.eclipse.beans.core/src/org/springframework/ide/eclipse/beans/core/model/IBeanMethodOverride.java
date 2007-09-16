/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

/**
 * This interface provides information for an {@link IBean}'s constructor
 * argument.
 * 
 * @author Christian Dupuis
 * @since 2.0.2
 */
public interface IBeanMethodOverride extends IBeansValueHolder {

	enum TYPE {
		LOOKUP, REPLACE
	}

	TYPE getType();

	String getBeanName();
	
	String getMethodName();
}
