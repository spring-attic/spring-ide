/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public class JdtMetadataReaderAdaper implements MetadataReader {

	private final IType type;
	private final AnnotationMetadataReadingVisitor visitor;
	private final JdtAnnotationMetadataAdapter adapter;

	public JdtMetadataReaderAdaper(IType type, CachingClassReaderFactory classReaderFactory, ClassLoader classloader) {
		this.type = type;
		
		this.visitor = new AnnotationMetadataReadingVisitor(true);
		this.visitor.setType(type);
		this.visitor.setClassloader(classloader);

		try {
			ClassReader classReader = classReaderFactory.getClassReader(type.getFullyQualifiedName());
			classReader.accept(this.visitor, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		adapter = new JdtAnnotationMetadataAdapter(type, visitor);
	}

	public ClassMetadata getClassMetadata() {
		return this.adapter;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return this.adapter;
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
