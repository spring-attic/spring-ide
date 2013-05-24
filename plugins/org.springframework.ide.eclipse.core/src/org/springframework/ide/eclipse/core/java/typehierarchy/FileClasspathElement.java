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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class FileClasspathElement implements ClasspathElement {
	
	private File file;

	public FileClasspathElement(File file) {
		this.file = file;
	}

	public InputStream getStream(String classFileName) throws Exception {
		File concreteFile = new File(file, classFileName);
		if (concreteFile.exists()) {
			return new FileInputStream(concreteFile);
		}
		return null;
	}

}
