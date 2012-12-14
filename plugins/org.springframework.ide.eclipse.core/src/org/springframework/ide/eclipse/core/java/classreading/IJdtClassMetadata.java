/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import org.eclipse.jdt.core.IType;
import org.springframework.core.type.ClassMetadata;

/**
 * @author Martin Lippert
 * @since 3.2.0
 */
public interface IJdtClassMetadata extends ClassMetadata {
	
	IType getType();

}
