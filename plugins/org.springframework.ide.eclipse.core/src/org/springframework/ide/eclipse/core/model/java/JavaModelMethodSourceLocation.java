/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/**
 * specific source location for a method that contains direct and exact return type information
 * 
 * @author Martin Lippert
 */
public class JavaModelMethodSourceLocation extends JavaModelSourceLocation {

	private static final long serialVersionUID = -4616187760224394345L;
	private String returnType;

	public JavaModelMethodSourceLocation(IJavaElement type, String returnType) throws JavaModelException {
		super(type);
		this.returnType = returnType;
	}
	
	public String getReturnType() {
		return returnType;
	}

}
