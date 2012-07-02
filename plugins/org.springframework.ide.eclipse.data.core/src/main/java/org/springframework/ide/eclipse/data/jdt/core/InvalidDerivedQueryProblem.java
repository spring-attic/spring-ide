/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.springframework.util.Assert;

/**
 * {@link CategorizedProblem} to express an invalid Spring Data query method.
 * 
 * @author Oliver Gierke
 */
public class InvalidDerivedQueryProblem extends CategorizedProblem {

	private final IMethod method;
	private final String message;

	/**
	 * Creates a new {@link InvalidDerivedQueryProblem} for the given {@link IMethod} and message.
	 * 
	 * @param method must not be {@literal null}.
	 * @param message must not be {@literal null} or empty.
	 */
	public InvalidDerivedQueryProblem(IMethod method, String message) {

		Assert.notNull(method);
		Assert.hasText(message);

		this.method = method;
		this.message = message;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getArguments()
	 */
	public String[] getArguments() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getID()
	 */
	public int getID() {
		return 0;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getMessage()
	 */
	public String getMessage() {
		return "Invalid derived query! " + message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getOriginatingFileName()
	 */
	public char[] getOriginatingFileName() {
		return method.getResource().getFullPath().toString().toCharArray();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceEnd()
	 */
	public int getSourceEnd() {
		try {
			return method.getSourceRange().getOffset() + method.getSourceRange().getLength();
		} catch (JavaModelException e) {
			return 0;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceLineNumber()
	 */
	public int getSourceLineNumber() {

		try {
			return method.getNameRange().getOffset();
		} catch (JavaModelException e) {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#getSourceStart()
	 */
	public int getSourceStart() {
		return getSourceLineNumber();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#isError()
	 */
	public boolean isError() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#isWarning()
	 */
	public boolean isWarning() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceEnd(int)
	 */
	public void setSourceEnd(int arg0) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceLineNumber(int)
	 */
	public void setSourceLineNumber(int arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.IProblem#setSourceStart(int)
	 */
	public void setSourceStart(int arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CategorizedProblem#getCategoryID()
	 */
	@Override
	public int getCategoryID() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CategorizedProblem#getMarkerType()
	 */
	@Override
	public String getMarkerType() {
		// TODO Auto-generated method stub
		return null;
	}

}
