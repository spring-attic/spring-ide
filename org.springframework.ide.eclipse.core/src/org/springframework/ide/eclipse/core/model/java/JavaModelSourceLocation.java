/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Java implementation of {@link IModelSourceLocation} interface that takes all
 * relevant information in the constructor.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class JavaModelSourceLocation implements Serializable, IModelSourceLocation {

	private static final long serialVersionUID = -1689438887512112705L;

	private String handleIdentifier;

	public JavaModelSourceLocation(IJavaElement type) throws JavaModelException {
		this.handleIdentifier = type.getHandleIdentifier();
	}

	public int getEndLine() {
		return JdtUtils.getLineNumber(JavaCore.create(handleIdentifier,
				DefaultWorkingCopyOwner.PRIMARY));
	}

	public int getStartLine() {
		return getEndLine();
	}

	public Resource getResource() {
		try {
			return new FileResource(JavaCore.create(handleIdentifier,
					DefaultWorkingCopyOwner.PRIMARY).getUnderlyingResource().getFullPath()
					.toString());
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
