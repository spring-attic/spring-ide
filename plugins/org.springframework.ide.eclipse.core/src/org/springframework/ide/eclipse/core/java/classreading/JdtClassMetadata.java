/*******************************************************************************
 * Copyright (c) 2009, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.core.type.ClassMetadata;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class JdtClassMetadata implements ClassMetadata, JdtConnectedMetadata {

	private final IType type;
	private String superclassName;
	private boolean superclassNameInitialized;

	public JdtClassMetadata(IType type) {
		this.type = type;
	}

	public String getClassName() {
		return type.getFullyQualifiedName();
	}

	public String getEnclosingClassName() {
		IType enclosingType = type.getDeclaringType();
		if (enclosingType != null) {
			return enclosingType.getFullyQualifiedName();
		}
		return null;
	}

	public String[] getInterfaceNames() {
		List<String> implementedInterfaces = new ArrayList<String>();
		try {
			String[] interfaces = type.getSuperInterfaceTypeSignatures();
			if (interfaces != null) {
				for (String iface : interfaces) {
					String fqin = JdtUtils.resolveClassNameBySignature(iface, type);
					IType interfaceType;
					interfaceType = type.getJavaProject().findType(fqin);
					if (interfaceType != null) {
						implementedInterfaces.add(interfaceType.getFullyQualifiedName());
					}
				}
			}
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
		return (String[]) implementedInterfaces.toArray(new String[implementedInterfaces.size()]);
	}

	public String getSuperClassName() {
		if (!superclassNameInitialized) {
			try {
				IType superType = Introspector.getSuperType(type);
				if (superType != null) {
					this.superclassName = superType.getFullyQualifiedName();
				}
				superclassNameInitialized = true;
			}
			catch (JavaModelException e) {
				throw new JdtMetadataReaderException(e);
			}
		}
		return this.superclassName;
	}

	public boolean hasEnclosingClass() {
		return type.getDeclaringType() != null;
	}

	public boolean hasSuperClass() {
		return getSuperClassName() != null;
	}

	public boolean isAbstract() {
		try {
			return Flags.isAbstract(type.getFlags());
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isConcrete() {
		return !(isInterface() || isAbstract());
	}

	public boolean isFinal() {
		try {
			return Flags.isFinal(type.getFlags());
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isIndependent() {
		try {
			return (!hasEnclosingClass() || (this.type.getDeclaringType() != null && Flags
					.isStatic(type.getFlags())));
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public boolean isInterface() {
		try {
			return type.isInterface();
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
	}

	public String[] getMemberClassNames() {
		Set<String> memberClassNames = new LinkedHashSet<String>();
		try {
			for (IType memberType : type.getTypes()) {
				memberClassNames.add(memberType.getFullyQualifiedName());
			}
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
		return memberClassNames.toArray(new String[memberClassNames.size()]); 
	}

	public IJavaElement getJavaElement() {
		return this.type;
	}

}
