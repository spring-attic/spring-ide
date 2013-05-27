/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
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
	 * @param inputKinds
	 */
	void setInputKinds(Map<String, String> inputKinds);

	void setProjectNameToken(String projectNameToken);

	void setTopLevelPackageTokens(String[] topLevelPackageTokens);

	/**
	 * The user input is a key-value pair, where the key is the original token
	 * that needs replacement (for example, the default top level package name
	 * as defined in the template files) and the value, the user replacement
	 * (for example, the user defined top level package name). Note that the
	 * same token key value is also used in the input kinds map, when defining
	 * the type of the original token.
	 * @param userInput
	 */
	void setUserInput(Map<String, Object> userInput);

}