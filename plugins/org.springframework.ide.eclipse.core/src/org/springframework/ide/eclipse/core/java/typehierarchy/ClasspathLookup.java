/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
*******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.io.InputStream;

/**
 * A simple lookup mechansim for finding resources on a number of directories and zip/jar files.
 * This can be used to lookup resources from a classpath of a project within the IDE.
 * 
 * You have to close a ClasspathLookup after using it in order to release possible file locks on zip
 * files, for example.
 * 
 * @author Martin Lippert
 * @since 3.7.0
 */
public class ClasspathLookup {
	
	private ClasspathElement[] cpElements;

	public ClasspathLookup(ClasspathElement[] cpElements) {
		this.cpElements = cpElements;
	}

	public InputStream getStream(String fullyQualifiedClassFileName, String packageName, String className) {
		for (int i = 0; i < cpElements.length; i++) {
			InputStream stream = null;
			synchronized(cpElements[i]) {
				try {
					stream = cpElements[i].getStream(fullyQualifiedClassFileName, packageName, className);
					if (stream != null) {
						return stream;
					}
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	public void close() {
		for (int i = 0; i < cpElements.length; i++) {
			synchronized(cpElements[i]) {
				cpElements[i].cleanup();
			}
		}
	}

}
