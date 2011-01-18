/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.type.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;
import org.springframework.ide.eclipse.core.type.ClassMetadata;
import org.springframework.util.ClassUtils;

/**
 * ASM class visitor which looks only for the class name and implemented types,
 * exposing them through the
 * {@link org.springframework.ide.eclipse.core.type.ClassMetadata} interface.
 * @author Christian Dupuis
 * @author Rod Johnson
 * @author Costin Leau
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @since 2.0.2
 * @see AnnotationMetadataReadingVisitor
 */
public class ClassMetadataReadingVisitor extends EmptyVisitor implements
		ClassMetadata {

	private String className;

	private boolean isInterface;

	private boolean isAbstract;

	private String superClassName;

	private String[] interfaces;

	public void visit(int version, int access, String name, String signature,
			String supername, String[] interfaces) {
		this.className = ClassUtils.convertResourcePathToClassName(name);
		this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
		this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
		if (supername != null) {
			this.superClassName = ClassUtils
					.convertResourcePathToClassName(supername);
		}
		this.interfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = ClassUtils
					.convertResourcePathToClassName(interfaces[i]);
		}
	}

	public String getClassName() {
		return this.className;
	}

	public boolean isInterface() {
		return this.isInterface;
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public boolean isConcrete() {
		return !(this.isInterface || this.isAbstract);
	}

	public String getSuperClassName() {
		return this.superClassName;
	}

	public boolean hasSuperClass() {
		return (this.superClassName != null);
	}

	public String[] getInterfaceNames() {
		return this.interfaces;
	}

}
