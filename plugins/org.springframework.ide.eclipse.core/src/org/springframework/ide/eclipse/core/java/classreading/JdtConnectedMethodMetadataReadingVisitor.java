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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.util.MultiValueMap;

/**
 * Addition to the standard class method metadata reading visitor that connects this method metadata to the corresponding
 * JDT IMethod
 *
 * @author Martin Lippert
 * @since 3.3.0
 */
public class JdtConnectedMethodMetadataReadingVisitor extends MethodMetadataReadingVisitor implements JdtConnectedMetadata {

	private final IMethod method;

	public JdtConnectedMethodMetadataReadingVisitor(String name, int access, String declaringClassName, ClassLoader classLoader,
			MultiValueMap<String, MethodMetadata> methodMetadataMap, IMethod method) {
		super(name, access, declaringClassName, classLoader, methodMetadataMap);
		this.method = method;
	}

	public IJavaElement getJavaElement() {
		return this.method;
	}

}