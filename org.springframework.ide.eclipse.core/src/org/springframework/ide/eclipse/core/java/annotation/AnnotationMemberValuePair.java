/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.annotation;

import java.io.Serializable;

/**
 * Default implementation of {@link IAnnotationMemberValuePair}
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class AnnotationMemberValuePair implements Serializable {
	
	private static final long serialVersionUID = 7480231952618333532L;

	private String name;
	
	private String value;
	
	public AnnotationMemberValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
