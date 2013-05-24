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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class ZipClasspathElement implements ClasspathElement {
	
	private ZipFile zipFile;

	public ZipClasspathElement(ZipFile zipFile) {
		this.zipFile = zipFile;
	}

	public InputStream getStream(String classFileName) throws Exception {
		ZipEntry entry = zipFile.getEntry(classFileName);
		if (entry != null) {
			return zipFile.getInputStream(entry);
		}
		return null;
	}

}
