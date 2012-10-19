/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;

/**
 * Processing info for wizard providing replacement context and patterns for
 * copying resources
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class AbstractProcessingInfo implements ProcessingInfo {

	protected Map<String, Object> userInput;

	protected Map<String, String> inputKinds;

	private Map<String, String> createReplacementContext() {
		// Use linked hashmap so that the ordering is preserved
		Map<String, String> replacementContext = new LinkedHashMap<String, String>();
		for (String token : userInput.keySet()) {
			Object value = userInput.get(token);
			if (value instanceof String) {
				replacementContext.put(token, (String) value);
			}
			if (value == null) {
				// First put modified versions, so that the original version
				// gets the last opportunity during replacement
				// For packages
				replacementContext.put("." + token, "");
				// For paths
				replacementContext.put("/" + token, "");
				// For Window's paths
				replacementContext.put("\\" + token, "");
				// For direct text
				replacementContext.put(token, "");
			}
		}
		return replacementContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.ewe.wizard.infrastructure.processor.ProcessingInfo
	 * #getExclusionPatterns()
	 */
	public Collection<String> getExclusionPatterns() {
		Collection<String> exclusionPatterns = new ArrayList<String>();
		exclusionPatterns.add("/**/target/**");
		exclusionPatterns.add("/**/*.svn/**");
		exclusionPatterns.add("/**/*.git/**");
		return exclusionPatterns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.ewe.wizard.infrastructure.processor.ProcessingInfo
	 * #getInclusionPatterns()
	 */
	public Collection<String> getInclusionPatterns() {
		return null;
	}

	private String getNameFromKind(String kind, String defaultKind) {
		for (String elementName : inputKinds.keySet()) {
			String inputKind = inputKinds.get(elementName);
			if (inputKind.equals(kind)) {
				return (String) userInput.get(elementName);
			}
		}
		return (String) userInput.get(defaultKind);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.ewe.wizard.infrastructure.processor.ProcessingInfo
	 * #getResourceReplacementContext()
	 */
	public Map<String, String> getResourceReplacementContext() {
		Map<String, String> replacementContext = getTemplateReplacementContext();

		for (String token : replacementContext.keySet()) {
			String value = replacementContext.get(token);

			String kind = this.inputKinds.get(token);
			if (kind == null || !kind.equals("fixedtoken")) {
				value = value.replace(".", "/");
			}
			replacementContext.put(token, value);
		}
		return replacementContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.ewe.wizard.infrastructure.processor.ProcessingInfo
	 * #getTemplateReplacementContext()
	 */
	public Map<String, String> getTemplateReplacementContext() {
		return createReplacementContext();
	}

	public String getUserTopLevelPackageName() {
		return getNameFromKind(WizardUIInfoElement.TOP_LEVEL_PACKAGE_NAME_KIND, "topLevelPackage");
	}

	public void setInputKinds(Map<String, String> inputKinds) {
		this.inputKinds = inputKinds;
	}

	public void setUserInput(Map<String, Object> userInput) {
		this.userInput = userInput;
	}
}
