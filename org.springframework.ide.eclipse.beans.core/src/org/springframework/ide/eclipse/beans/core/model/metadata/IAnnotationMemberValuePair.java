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
package org.springframework.ide.eclipse.beans.core.model.metadata;

/**
 * Represents one member of an annotation.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IAnnotationMemberValuePair {
		
	/**
	 * Returns the name of the member.
	 */
	String getName();
	
	/**
	 * Returns the value of the member in a String-based representation.
	 */
	String getValue();
	
}
