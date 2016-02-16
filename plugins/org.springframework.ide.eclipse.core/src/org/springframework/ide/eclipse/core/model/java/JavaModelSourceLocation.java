/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.java;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Java implementation of {@link IModelSourceLocation} interface that takes all relevant information in the constructor.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class JavaModelSourceLocation implements Serializable, IModelSourceLocation {

	private static final long serialVersionUID = -1689438887512112705L;

	private String handleIdentifier;

	private int lineNumber;

	public JavaModelSourceLocation(IJavaElement type) throws JavaModelException {
		this.handleIdentifier = type.getHandleIdentifier();
		this.lineNumber = JdtUtils.getLineNumber(JdtUtils.getByHandle(handleIdentifier));
	}

	public int getEndLine() {
		return lineNumber;
	}

	public int getStartLine() {
		return getEndLine();
	}

	public Resource getResource() {
		try {
			IJavaElement element = JdtUtils.getByHandle(handleIdentifier);
			if (element != null) {
				IResource resource = element.getUnderlyingResource();
				if (resource != null) {
					return new FileResource(resource.getFullPath().toString());
				}
				resource = element.getCorrespondingResource();
				if (resource != null) {
					return new FileResource(resource.getFullPath().toString());
				}
				resource = element.getResource();
				if (resource != null) {
					return new FileResource(resource.getFullPath().toString());
				}
				IPath path = element.getPath();
				if (path != null && path.toFile().exists()) {
					if (path.isAbsolute()) {
						return new FileSystemResource(path.toFile());
					}
					else {
						return new FileResource(path.toString());
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JavaModelSourceLocation)) {
			return false;
		}
		JavaModelSourceLocation that = (JavaModelSourceLocation) other;
		return ObjectUtils.nullSafeEquals(this.handleIdentifier, that.handleIdentifier);
	}

	@Override
	public int hashCode() {
		return this.handleIdentifier.hashCode();
	}

	public String getHandleIdentifier() {
		return handleIdentifier;
	}
}
