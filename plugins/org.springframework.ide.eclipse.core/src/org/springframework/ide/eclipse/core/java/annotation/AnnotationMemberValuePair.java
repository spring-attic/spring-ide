/*******************************************************************************
 * Copyright (c) 2008, 2012 Spring IDE Developers
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
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0.5
 */
public class AnnotationMemberValuePair implements Serializable {
	
	private static final long serialVersionUID = 7480231952618333532L;

	private String name;
	private String value;
	private transient Object valueAsObject;
	
	public AnnotationMemberValuePair(String name, String value) {
		this.name = name;
		this.value = value;
		this.valueAsObject = value;
	}
	
	public AnnotationMemberValuePair(String name, String value, Object valueAsObject) {
		this.name = name;
		this.value = value;
		this.valueAsObject = valueAsObject;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	public Object getValueAsObject() {
		return valueAsObject;
	}
	
}
