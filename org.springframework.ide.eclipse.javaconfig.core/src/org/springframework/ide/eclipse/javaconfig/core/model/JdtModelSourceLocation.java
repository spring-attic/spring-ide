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
package org.springframework.ide.eclipse.javaconfig.core.model;

import org.eclipse.jdt.core.IMethod;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class JdtModelSourceLocation extends DefaultModelSourceLocation {

	private final IMethod method;
	
	public JdtModelSourceLocation(IMethod method, int startline, int endLine, Resource resource) {
		super(startline, endLine, resource);
		this.method = method;
	}

	public IMethod getMethod() {
		return method;
	}
}
