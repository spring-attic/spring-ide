/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public interface JdtConnectedMetadata {

	IJavaElement getJavaElement();
	JavaModelSourceLocation createSourceLocation() throws JavaModelException;

}
