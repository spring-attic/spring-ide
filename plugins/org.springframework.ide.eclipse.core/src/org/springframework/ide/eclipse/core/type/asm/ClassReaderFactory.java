/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.type.asm;

import java.io.IOException;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;

/**
 * Factory interface for ASM {@link org.objectweb.asm.ClassReader} instances.
 * Allows for caching a ClassReader per original resource.
 * @author Christian Dupuis
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see org.objectweb.asm.ClassReader
 */
public interface ClassReaderFactory {

	/**
	 * Obtain a ClassReader for the given class name.
	 * @param className the class name (to be resolved to a ".class" file)
	 * @return the ClassReader instance (never <code>null</code>)
	 * @throws IOException in case of I/O failure
	 */
	ClassReader getClassReader(String className) throws IOException;

	/**
	 * Obtain a ClassReader for the given resource.
	 * @param resource the resource (pointing to a ".class" file)
	 * @return the ClassReader instance (never <code>null</code>)
	 * @throws IOException in case of I/O failure
	 */
	ClassReader getClassReader(Resource resource) throws IOException;

}
