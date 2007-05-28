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
package org.springframework.ide.eclipse.core.model.validation;

import org.eclipse.core.resources.IResource;
import org.springframework.util.ObjectUtils;

/**
 * This class holds the information regarding a validation error.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ValidationProblem {

	private String ruleID;
	private int severity;
	private String message;
	private IResource resource;
	private int line;

	public ValidationProblem(int severity, String message, IResource resource) {
		this((String) null, severity, message, resource, -1);
	}

	public ValidationProblem(int severity, String message, IResource resource,
			int line) {
		this((String) null, severity, message, resource, line);
	}

	public ValidationProblem(String ruleID, int severity,
			String message, IResource resource, int line) {
		this.ruleID = ruleID;
		this.severity = severity;
		this.message = message;
		this.resource = resource;
		this.line = line;
	}

	/**
	 * Returns the ID of the {@link IValidationRule}Êwhich raised this problem
	 * or <code>null</code> if no validation rule was involved.
	 */
	public String getRuleID() {
		return ruleID;
	}

	/**
	 * Returns the severity level of this problem.
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Returns message text of this problem.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns the {@link IResource}Êthis problem belongs to.
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Returns the line number of this problem or <code>-1</code> if not
	 * available.
	 */
	public int getLine() {
		return line;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(ruleID);
		hashCode = 2 * hashCode + severity;
		hashCode = 3 * hashCode + ObjectUtils.nullSafeHashCode(message);
		hashCode = 4 * hashCode + ObjectUtils.nullSafeHashCode(resource);
		hashCode = 5 * hashCode + line;
		return 6 * hashCode + super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ValidationProblem)) {
			return false;
		}
		ValidationProblem that = (ValidationProblem) other;
		if (!ObjectUtils.nullSafeEquals(this.ruleID, that.ruleID)) return false;
		if (this.severity != that.severity) return false;
		if (!ObjectUtils.nullSafeEquals(this.message, that.message)) return false;
		if (!ObjectUtils.nullSafeEquals(this.resource, that.resource)) return false;
		if (this.line != that.line) return false;
		return super.equals(other);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
