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
package org.springframework.ide.eclipse.core.java.classreading;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class JdtConnectedMetadataReader implements MetadataReader {

	private final IType type;
	private final JdtConnectedAnnotationMetadataReadingVisitor visitor;

	public JdtConnectedMetadataReader(IType type, CachingClassReaderFactory classReaderFactory, ClassLoader classloader) {
		this.type = type;

		this.visitor = new JdtConnectedAnnotationMetadataReadingVisitor(classloader, type);

		try {
			ClassReader classReader = classReaderFactory.getClassReader(type.getFullyQualifiedName());
			classReader.accept(this.visitor, 0);
		} catch (IOException e) {
			SpringCore.log(e);
		}
		
	}

	public ClassMetadata getClassMetadata() {
		return this.visitor;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return this.visitor;
	}

	public Resource getResource() {
		try {
			return new FileResource((IFile) this.type.getUnderlyingResource());
		}
		catch (JavaModelException e) {
			return null;
		}
	}

}
