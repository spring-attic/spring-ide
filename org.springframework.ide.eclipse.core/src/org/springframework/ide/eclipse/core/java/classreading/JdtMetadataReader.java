/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import org.eclipse.jdt.core.IType;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;

/**
 * @author Christian Dupuis
 * @since 2.2.5
 */
public class JdtMetadataReader implements MetadataReader {

	private final IType type;

	public JdtMetadataReader(IType type) {
		this.type = type;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return new JdtAnnotationMetadata(type);
	}

	public ClassMetadata getClassMetadata() {
		return new JdtClassMetadata(type);
	}

	public Resource getResource() {
		throw new JdtMetadataReaderException("'getResource' is not supported");
	}

}
