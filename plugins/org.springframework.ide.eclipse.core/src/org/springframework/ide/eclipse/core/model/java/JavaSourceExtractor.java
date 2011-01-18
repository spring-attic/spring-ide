/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.java;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.ClassReader;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;

/**
 * A {@link SourceExtractor} implementation which retrieves
 * {@link DefaultModelSourceLocation} from a given
 * {@link FileSystemResource}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class JavaSourceExtractor implements SourceExtractor {
	
	private final IProject project;
	
	private final ClassReaderFactory classReaderFactory = 
		new CachingClassReaderFactory();
	
	public JavaSourceExtractor(final IProject project) {
		this.project = project;
	}

	public Object extractSource(Object sourceCandidate,
			Resource definingResource) {
		if (sourceCandidate instanceof FileSystemResource) {
			try {
				ClassReader	reader = classReaderFactory.
					getClassReader((FileSystemResource) sourceCandidate);
				ClassMetadataReadingVisitor v = new ClassMetadataReadingVisitor();
				reader.accept(v, true);
				String className = v.getClassName();
				IType type = JdtUtils.getJavaType(project, className);
				if (type != null) {
					return new JavaModelSourceLocation(type);
				}
			}
			catch (IOException e) {
			}
			catch (JavaModelException e) {
			}
		}
		return sourceCandidate;
	}
}
