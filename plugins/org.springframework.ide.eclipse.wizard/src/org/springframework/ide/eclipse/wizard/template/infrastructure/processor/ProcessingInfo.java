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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.springframework.ide.eclipse.wizard.template.SpringVersion;

/**
 * Stores user input from template wizard UI elements
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public interface ProcessingInfo {

	Collection<String> getExclusionPatterns();

	Collection<String> getInclusionPatterns();

	Map<String, String> getResourceReplacementContext();

	Map<String, String> getTemplateReplacementContext();

	URL getTemplateSourceDirectory();

	SpringVersion getSpringVersion();

	void setInputKinds(Map<String, String> inputKinds);

	void setProjectNameToken(String projectNameToken);

	void setTopLevelPackageTokens(String[] topLevelPackageTokens);

	void setUserInput(Map<String, Object> userInput);

}