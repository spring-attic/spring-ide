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
package org.springframework.ide.eclipse.aop.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;

public interface IAopReference {

	public enum ADVICE_TYPES {
		BEFORE, AROUND, AFTER, AFTER_RETURNING, AFTER_THROWING, DECLARE_PARENTS
	};

	ADVICE_TYPES getAdviceType();

	IMember getSource();

	IAspectDefinition getDefinition();

	IResource getResource();

	IMember getTarget();

	//IBean getTargetBean();
	
	String getTargetBeanId();

}
