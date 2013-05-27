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

import org.springframework.ide.eclipse.wizard.template.infrastructure.processor.TemplateProcessor;

/**
 * Processes Spring version that is used during project configuration. Only
 * Supports replacing Spring version in Maven POM.
 * 
 * NOTE: This is not used, but leaving it here in case Spring version handling
 * needs to be changed from its current case where it is handled as any other
 * template token replacement, and a seperate processor is required for spring
 * version. The SpringVersionProcessor should be invoked in TemplateProcessor
 * during token replacement if it is to be used.
 * @see TemplateProcessor
 * 
 */
public class SpringVersionProcessor {

	public static final String MAVEN_SPRING_ELEMENT_START_TAG = "<org.springframework-version>";

	// "<spring.integration.version>";

	public static final String MAVEN_SPRING_ELEMENT_END_TAG = "</org.springframework-version>";

	public final String springVersion;

	private boolean isReplacing = false;

	public SpringVersionProcessor(String springVersion) {
		this.springVersion = springVersion;
	}

	public boolean isReplacing(String line) {
		if (!isReplacing) {
			// Update the flag to see if replacement should begin
			isReplacing = line != null && line.contains(MAVEN_SPRING_ELEMENT_START_TAG);
		}
		return isReplacing;
	}

	public String replace(String line) {
		if (line == null || !isReplacing) {
			return null;
		}

		String replacedLine = "";

		// Even if the start tag is on a separate line that the end tag, replace
		// the entire start tag line with
		// the full, correct XML line.
		if (line.contains(MAVEN_SPRING_ELEMENT_START_TAG)) {
			// The end tag may be in the same line. Check for it, before
			// replacing the line
			if (line.contains(MAVEN_SPRING_ELEMENT_END_TAG)) {
				isReplacing = false;
			}
			replacedLine = MAVEN_SPRING_ELEMENT_START_TAG + springVersion + MAVEN_SPRING_ELEMENT_END_TAG;
		}
		else if (line.contains(MAVEN_SPRING_ELEMENT_END_TAG)) {
			isReplacing = false;
		}

		return replacedLine;
	}

}
