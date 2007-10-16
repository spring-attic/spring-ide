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
package org.springframework.ide.eclipse.core.java;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * {@link IMethodFilter} implementation that is capable of checking the
 * {@link IMethod}'s signature.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class FlagsMethodFilter extends ParameterMethodFilter {

	// visibility flags
	public static final int PUBLIC = 1 << 2;
	public static final int PRIVATE = 1 << 3;
	public static final int PROTECTED = 1 << 4;

	// abstract flags
	public static final int ABSTRACT = 1 << 5;
	public static final int NOT_ABSTRACT = 1 << 6;

	// return type flags
	public static final int VOID = 1 << 7;
	public static final int NOT_VOID = 1 << 8;

	// final flags
	public static final int FINAL = 1 << 9;
	public static final int NOT_FINAL = 1 << 10;

	// interface flags
	public static final int INTERFACE = 1 << 11;
	public static final int NOT_INTERFACE = 1 << 12;

	// constructor flags
	public static final int CONSTRUCTOR = 1 << 13;
	public static final int NOT_CONSTRUCTOR = 1 << 14;

	// static flags
	public static final int STATIC = 1 << 15;
	public static final int NOT_STATIC = 1 << 16;

	/**
	 * Flags to check for
	 */
	private final int flags;

	/**
	 * Constructor
	 * @param flags flags to match
	 */
	public FlagsMethodFilter(int flags) {
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param flags flags to match
	 * @param parameterTypes parameter types to match
	 */
	public FlagsMethodFilter(int flags, String[] parameterTypes) {
		super(parameterTypes);
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param flags flags to match
	 * @param returnType return type to match
	 * @param parameterTypes parameter types to match
	 */
	public FlagsMethodFilter(int flags, String returnType,
			String[] parameterTypes) {
		super(returnType, parameterTypes);
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param flags flags to match
	 * @param returnType return type to match
	 */
	public FlagsMethodFilter(int flags, String returnType) {
		super(returnType);
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param flags flags to match
	 * @param parameterCount parameter count to match
	 */
	public FlagsMethodFilter(int flags, int parameterCount) {
		super(parameterCount);
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param flags flags to match
	 * @param returnType return type to match
	 * @param parameterCount parameter count to match
	 */
	public FlagsMethodFilter(int flags, String returnType, int parameterCount) {
		super(returnType, parameterCount);
		this.flags = flags;
	}

	/**
	 * Calls {@link #matches(IMethod, String)}, {@link #matchesFlags(IMethod)}.
	 * Returns if and only if all calls returned true.
	 */
	@Override
	public boolean matches(IMethod method, String prefix) {
		return super.matches(method, prefix) && matchesFlags(method);
	}
	
	/**
	 * Checks if the given {@link IMethod} satisfies the {@link #flags}.
	 * <p>
	 * Note: always returns false if a {@link JavaModelException} occurs.
	 * @param method the method to check 
	 * @return true if all flags apply to this method
	 */
	private boolean matchesFlags(IMethod method) {
		try {
			int methodFlags = method.getFlags();

			// first check method existence
			if (!method.exists()) {
				return false;
			}

			// visibility checks
			if ((flags & PUBLIC) != 0 && !Flags.isPublic(methodFlags)) {
				return false;
			}
			if ((flags & PROTECTED) != 0 && !Flags.isProtected(methodFlags)) {
				return false;
			}
			if ((flags & PRIVATE) != 0 && !Flags.isPrivate(methodFlags)) {
				return false;
			}

			// abstract checks
			if ((flags & ABSTRACT) != 0 && !Flags.isAbstract(methodFlags)) {
				return false;
			}
			if ((flags & NOT_ABSTRACT) != 0 && Flags.isAbstract(methodFlags)) {
				return false;
			}

			// return type checks
			if ((flags & VOID) != 0 && method.getReturnType() != null) {
				return false;
			}
			if ((flags & NOT_VOID) != 0 && method.getReturnType() == null) {
				return false;
			}

			// final checks
			if ((flags & FINAL) != 0 && !Flags.isFinal(methodFlags)) {
				return false;
			}
			if ((flags & NOT_FINAL) != 0 && Flags.isFinal(methodFlags)) {
				return false;
			}

			// interface checks
			if ((flags & INTERFACE) != 0 && !Flags.isInterface(methodFlags)) {
				return false;
			}
			if ((flags & NOT_INTERFACE) != 0 && Flags.isInterface(methodFlags)) {
				return false;
			}

			// constructor checks
			if ((flags & CONSTRUCTOR) != 0 && !method.isConstructor()) {
				return false;
			}
			if ((flags & NOT_CONSTRUCTOR) != 0 && method.isConstructor()) {
				return false;
			}

			// static checks
			if ((flags & STATIC) != 0 && !Flags.isStatic(methodFlags)) {
				return false;
			}
			if ((flags & NOT_STATIC) != 0 && Flags.isStatic(methodFlags)) {
				return false;
			}
		}
		catch (JavaModelException e) {
			return false;
		}
		return true;
	}
}
