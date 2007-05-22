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
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;

public interface IPersistableWebflowModelElement extends IAdaptable {

	int MODEL = 1;

	int PROJECT = 2;

	int CONFIG = 3;

	IPersistableWebflowModelElement getPersistableElementParent();

	String getElementID();

	int getElementType();

	String getElementName();
	
	Set<IPersistableWebflowModelElement> getElementChildren();

}
