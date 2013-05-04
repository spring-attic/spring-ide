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
package org.springframework.ide.eclipse.core.type.asm;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.SpringAsmInfo;

/**
 * Null object implementation for ASM based annotation visitor.
 * interface.
 *
 * @author Martin Lippert
 * @since 3.3.0
 */
public class EmptyAnnotationVisitor extends AnnotationVisitor {

	public EmptyAnnotationVisitor() {
		super(SpringAsmInfo.ASM_VERSION);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return this;
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		return this;
	}

}
