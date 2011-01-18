/*******************************************************************************
 * Copyright (c) 2004, 2010 Spring IDE Developers
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
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeanConstructorArgument extends IBeansValueHolder {

	int getIndex();

	String getType();
	
	String getName();
}
