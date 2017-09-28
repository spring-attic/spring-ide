/*******************************************************************************
 * Copyright (c) 2015, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * @author Kris De Volder
 */
public abstract class AbstractRequestMapping implements RequestMapping {

	protected final TypeLookup typeLookup;

	protected AbstractRequestMapping(TypeLookup typeLookup) {
		this.typeLookup = typeLookup;
	}

	@Override
	public IType getType() {
		String fqName = getFullyQualifiedClassName();
		if (fqName!=null) {
			return typeLookup.findType(fqName);
		}
		return null;
	}

	public IMethod getMethod() {
		try {
			IType type = getType();
			if (type!=null) {
				String mName = getMethodName();
				if (mName!=null) {
					IMethod[] methods = type.getMethods();
					if (methods!=null && methods.length>0) {
						for (IMethod m : methods) {
							//TODO: handle method overloading
							if (mName.equals(m.getElementName())) {
								return m;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	@Override
	public boolean isUserDefined() {
		try {
			IType type = getType();
			if (type!=null) {
				IPackageFragmentRoot pfr = (IPackageFragmentRoot)type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				if (pfr!=null) {
					return pfr.getKind()==IPackageFragmentRoot.K_SOURCE;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}
}
