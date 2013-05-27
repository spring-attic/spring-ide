/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;

/**
 * 
 * Given a list of wizard info elements that defines the types of variables that
 * require values for a template, this handler collects input values for those
 * variables.
 * 
 * <p/>
 * Two components of a template input are:
 * <p/>
 * 1. User input, which is a key-value pair , where the key is the original
 * token that needs to be replaced for when processing templates, and it's
 * user-defined replacement, as a value.
 * <p/>
 * 2. Input kinds. The tokens that need to be replaced can also be assigned an
 * input kind, which is used to determine if the token requires additional
 * processing while it is being replaced. For example, top level package tokens
 * would need the '.' separator replaced with slashes when handling folder name
 * changes.
 * 
 */
public class TemplateInputCollector {
	private final Map<String, Object> collectedInput;

	private final Map<String, String> inputKinds;

	private final List<WizardUIInfoElement> elements;

	public TemplateInputCollector(List<WizardUIInfoElement> elements) {
		this.elements = elements;
		collectedInput = new HashMap<String, Object>();
		inputKinds = new HashMap<String, String>();
		initInputElements();
	}

	protected void initInputElements() {
		for (WizardUIInfoElement element : elements) {
			String elementName = element.getName();

			collectedInput.put(elementName, null);

			String replaceKind = element.getReplaceKind();
			if (replaceKind != null) {
				inputKinds.put(elementName, replaceKind);
			}
			else {
				inputKinds.put(elementName, WizardUIInfoElement.DEFAULT_KIND);
			}
		}
	}

	public boolean updateInput(String elementName, Object value) {
		if (collectedInput.containsKey(elementName)) {
			collectedInput.put(elementName, value);
			return true;
		}
		return false;
	}

	public List<WizardUIInfoElement> getInfoElements() {
		return elements;
	}

	/**
	 * Inputs are key-value pairs, where the key is the original token that
	 * needs to be replaced, and the value the user-defined replacement.
	 * @return
	 */
	public Map<String, Object> getCollectedInput() {
		return collectedInput;
	}

	/**
	 * Input kinds are types assigned to tokens values in a file , file name, or
	 * folder that are to be replaced by user defined tokens . The type of token
	 * indicates if the token should require additional processing, in
	 * particular if token is a qualified package name that affects the file
	 * path segments. For example, if a token "my.company.com" that needs to be
	 * replaced by a user defined value, would require the '.' separator
	 * replaced with a slash. Use "token" as the default kind if the token
	 * should undergo default processing. On the other hand, avoid further
	 * processing of a token use "fixedToken" as the kind for the token.
	 */
	public Map<String, String> getInputKinds() {
		return inputKinds;
	}
}
