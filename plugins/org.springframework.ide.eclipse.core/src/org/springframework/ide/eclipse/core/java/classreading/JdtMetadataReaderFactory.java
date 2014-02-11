/*******************************************************************************
 * Copyright (c) 2009, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class JdtMetadataReaderFactory implements MetadataReaderFactory {

	private final IJavaProject project;
	private final ClassLoader classloader;
	private final CachingClassReaderFactory classReaderFactory;

	public JdtMetadataReaderFactory(IJavaProject project, ClassLoader classloader) {
		this.project = project;
		this.classloader = classloader;
		this.classReaderFactory = new CachingClassReaderFactory(this.classloader);
	}

	public MetadataReader getMetadataReader(String className) throws IOException {
		IType type = JdtUtils.getJavaType(project.getProject(), className);
		if (type == null && !className.contains("$")) {
			int ix = className.lastIndexOf('.');
			if (ix > 1) {
				className = className.substring(0, ix) + "$" + className.substring(ix + 1, className.length());
				type = JdtUtils.getJavaType(project.getProject(), className);
			}
		}
		if (type == null) {
			throw new IOException("Could not find " + className);
		}

		return new JdtConnectedMetadataReader(type, classReaderFactory, classloader);
	}

	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		throw new JdtMetadataReaderException("'getMetadataReader' is not supported");
	}

}
