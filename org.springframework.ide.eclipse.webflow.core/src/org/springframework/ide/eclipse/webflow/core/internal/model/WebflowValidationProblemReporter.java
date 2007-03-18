/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
