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
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
@SuppressWarnings("restriction")
public class ClassloaderTypeHierarchyClassReader implements TypeHierarchyClassReader {

	public TypeHierarchyElement readTypeHierarchyInformation(char[] fullyQualifiedClassName, IProject project) {
		try {
			ClassLoader classLoader = JdtUtils.getClassLoader(project, null);
			String classFileName = new String(fullyQualifiedClassName) + ".class";
			InputStream stream = classLoader.getResourceAsStream(classFileName);
			if (stream != null) {
				ClassFileReader classReader = ClassFileReader.read(stream, classFileName);
				if (classReader != null) {
					return new TypeHierarchyElement(classReader.getName(), classReader.getSuperclassName(), classReader.getInterfaceNames());
				}
			}
		} catch (Exception e) {
			SpringCore.log(e);
		}
		
		return null;
	}

	public void cleanup() {
	}

}
