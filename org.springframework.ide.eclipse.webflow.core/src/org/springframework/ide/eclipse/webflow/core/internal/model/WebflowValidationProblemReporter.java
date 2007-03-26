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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * Helper class to report errors and warnings.
 * 
 * @author Christian Dupuis
 * @since 2.0
 * @see WebflowValidationVisitor
 */
public class WebflowValidationProblemReporter {

	private List<WebflowValidationProblem> errors = 
		new ArrayList<WebflowValidationProblem>();

	private List<WebflowValidationProblem> warnings = 
		new ArrayList<WebflowValidationProblem>();

	public void error(WebflowValidationProblem problem) {
		this.errors.add(problem);
	}

	public void error(String message, IWebflowModelElement element) {
		this.errors.add(new WebflowValidationProblem(message, element));
	}

	public void error(String message, IWebflowModelElement element, String... params) {
		this.errors.add(new WebflowValidationProblem(MessageUtils.format(message, (Object[]) params), element));
	}

	public void warning(WebflowValidationProblem problem) {
		this.warnings.add(problem);
	}
	
	public void warning(String message, IWebflowModelElement element, String... params) {
		this.warnings.add(new WebflowValidationProblem(MessageUtils.format(message, (Object[]) params), element));
	}
	
	public boolean hasErrors() {
		return getErrors() != null && getErrors().size() > 0;
	}

	public boolean hasWarnings() {
		return getWarnings() != null && getWarnings().size() > 0;
	}
	
	public List<WebflowValidationProblem> getErrors() {
		return errors;
	}

	public List<WebflowValidationProblem> getWarnings() {
		return warnings;
	}
}
