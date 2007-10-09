/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.type;

import java.lang.reflect.Modifier;

import org.springframework.core.type.ClassMetadata;

/**
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class StandardClassMetadata implements ClassMetadata {

	private final Class introspectedClass;

	public StandardClassMetadata(Class introspectedClass) {
		this.introspectedClass = introspectedClass;
	}

	public final Class getIntrospectedClass() {
		return this.introspectedClass;
	}

	public String getClassName() {
		return getIntrospectedClass().getName();
	}

	public boolean isInterface() {
		return getIntrospectedClass().isInterface();
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(getIntrospectedClass().getModifiers());
	}

	public boolean isConcrete() {
		return !(isInterface() || isAbstract());
	}

	public boolean hasSuperClass() {
		return (getIntrospectedClass().getSuperclass() != null);
	}

	public String getSuperClassName() {
		Class superClass = getIntrospectedClass().getSuperclass();
		return (superClass != null ? superClass.getName() : null);
	}

	public String[] getInterfaceNames() {
		Class[] ifcs = getIntrospectedClass().getInterfaces();
		String[] ifcNames = new String[ifcs.length];
		for (int i = 0; i < ifcs.length; i++) {
			ifcNames[i] = ifcs[i].getName();
		}
		return ifcNames;
	}

}
