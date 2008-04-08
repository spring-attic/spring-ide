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

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

/**
 * Interface implement by different mechanisms to obtain annotation meta data.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IAnnotationMetadata {

	/**
	 * Returns the method-level annotations meta data grouped by {@link IMethod}
	 * for the given <code>annotationClasses</code>.
	 */
	Map<IMethod, Annotation> getMethodLevelAnnotations(String... annotationClasses);

	/**
	 * Returns the type-level annotation meta data grouped by {@link IMethod}
	 * for the given <code>annotationClass</code>.
	 */
	Annotation getTypeLevelAnnotation(String annotationClass);

	/**
	 * Returns all type-level annotation classes.
	 */
	Set<String> getTypeLevelAnnotationClasses();

	/**
	 * Returns <code>true</code> if a any method-level annotation from the
	 * given <code>annotationClasses</code> has been found.
	 */
	boolean hasMethodLevelAnnotations(String... annotationClasses);

	/**
	 * Returns <code>true</code> if a any type-level annotation from the given
	 * <code>annotationClasses</code> has been found.
	 */
	boolean hasTypeLevelAnnotation(String... annotationClasses);

}
