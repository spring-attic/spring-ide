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

	public Map<String, Object> getCollectedInput() {
		return collectedInput;
	}

	public Map<String, String> getInputKinds() {
		return inputKinds;
	}
}
