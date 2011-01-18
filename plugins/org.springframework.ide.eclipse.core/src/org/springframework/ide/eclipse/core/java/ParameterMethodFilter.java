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
package org.springframework.ide.eclipse.core.java;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * {@link IMethodFilter} that matches the method name, method parameter count
 * and type as well as return type.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class ParameterMethodFilter extends PrefixMethodNameFilter {

	private final String[] parameterTypes;

	private final String returnType;

	private final int parameterCount;

	/**
	 * Default constructor
	 */
	public ParameterMethodFilter() {
		this.parameterTypes = null;
		this.returnType = null;
		this.parameterCount = -1;
	}

	/**
	 * Constructor
	 * @param parameterTypes parameter types to match
	 */
	public ParameterMethodFilter(String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		this.returnType = null;
		this.parameterCount = parameterTypes.length;
	}

	/**
	 * Constructor
	 * @param returnType return type to match
	 * @param parameterTypes parameter types to match
	 */
	public ParameterMethodFilter(String returnType, String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
		this.parameterCount = parameterTypes.length;
	}

	/**
	 * Constructor
	 * @param returnType return type to match
	 */
	public ParameterMethodFilter(String returnType) {
		this.parameterTypes = null;
		this.returnType = returnType;
		this.parameterCount = -1;
	}

	/**
	 * Constructor
	 * @param parameterCount parameter count to match
	 */
	public ParameterMethodFilter(int parameterCount) {
		this.parameterTypes = null;
		this.returnType = null;
		this.parameterCount = parameterCount;
	}

	/**
	 * Constructor
	 * @param returnType return type to match
	 * @param parameterCount parameter count to match
	 */
	public ParameterMethodFilter(String returnType, int parameterCount) {
		this.parameterTypes = null;
		this.returnType = returnType;
		this.parameterCount = parameterCount;
	}

	/**
	 * Calls {@link #matchesMethodPrefix(IMethod, String)},
	 * {@link #matchParameterTypes(IMethod)},
	 * {@link #matchParamterCount(IMethod)} and
	 * {@link #matchReturnType(IMethod)}.
	 * Returns true if all calls returned true.
	 */
	@Override
	public boolean matches(IMethod method, String prefix) {
		return super.matchesMethodPrefix(method, prefix)
				&& matchParamterCount(method) && matchParameterTypes(method)
				&& matchReturnType(method);
	}

	private boolean matchReturnType(IMethod method) {
		if (returnType != null) {
			IType type = (IType) method.getParent();
			IType mReturnType = JdtUtils.getJavaTypeForMethodReturnType(method,
					type);
			if (mReturnType == null
					|| !returnType.equals(mReturnType.getFullyQualifiedName())) {
				return false;
			}

		}
		return true;
	}

	private boolean matchParameterTypes(IMethod method) {
		if (parameterTypes != null) {
			IType type = (IType) method.getParent();
			List<IType> mParameterTypes = JdtUtils
					.getJavaTypesForMethodParameterTypes(method, type);

			if (parameterCount != mParameterTypes.size()) {
				return false;
			}

			for (int i = 0; i < mParameterTypes.size(); i++) {
				if (mParameterTypes.get(i) == null
						|| !parameterTypes[i].equals(mParameterTypes.get(i)
								.getFullyQualifiedName())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean matchParamterCount(IMethod method) {
		try {
			return (parameterCount == -1 && parameterTypes == null)
					|| method.getParameterNames().length == parameterCount;
		}
		catch (JavaModelException e) {
			return false;
		}
	}
}
