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
package org.springframework.ide.eclipse.core.type;

import java.util.Map;
import java.util.Set;

import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

/**
 * Interface that defines abstract access to the annotations of a specific
 * class, in a form that does not require that class to be loaded yet.
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.0.2
 * @see StandardAnnotationMetadata
 * @see org.springframework.ide.eclipse.core.type.asm.AnnotationMetadataReadingVisitor
 */
public interface AnnotationMetadata extends ClassMetadata {

	/**
	 * Return the names of all annotation types defined on the underlying class.
	 * @return the annotation type names
	 */
	Set<String> getAnnotationTypes();

	/**
	 * Determine whether the underlying class has an annotation of the given
	 * type defined.
	 * @param annotationType the annotation type to look for
	 * @return whether a matching annotation is defined
	 */
	boolean hasAnnotation(String annotationType);

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e.
	 * if defined on the underlying class).
	 * @param annotationType the annotation type to look for
	 * @return a Map of attributes, with the attribute name as key (e.g.
	 * "value") and the defined attribute value as Map value. This return value
	 * will be <code>null</code> if no matching annotation is defined.
	 */
	Map<String, Object> getAnnotationAttributes(String annotationType);

}
