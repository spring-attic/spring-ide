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

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardClassMetadata;

/**
 * Interface that defines abstract metadata of a specific class, in a form that
 * does not require that class to be loaded yet.
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.0.2
 * @see StandardClassMetadata
 * @see org.springframework.ide.eclipse.core.type.asm.ClassMetadataReadingVisitor
 * @see AnnotationMetadata
 */
public interface ClassMetadata {

	/**
	 * Return the name of the underlying class.
	 */
	String getClassName();

	/**
	 * Return whether the underlying class represents an interface.
	 */
	boolean isInterface();

	/**
	 * Return whether the underlying class is marked as abstract.
	 */
	boolean isAbstract();

	/**
	 * Return whether the underlying class represents a concrete class, i.e.
	 * neither an interface nor an abstract class.
	 */
	boolean isConcrete();

	/**
	 * Return whether the underlying class has a super class.
	 */
	boolean hasSuperClass();

	/**
	 * Return the name of the super class of the underlying class, or
	 * <code>null</code> if there is no super class defined.
	 */
	String getSuperClassName();

	/**
	 * Return the name of all interfaces that the underlying class implements,
	 * or an empty array if there are none.
	 */
	String[] getInterfaceNames();

}
