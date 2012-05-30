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
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WizardUIInfoElement {

	public static final String DEFAULT_KIND = "token";

	public static final String PROJECT_NAME_KIND = "projectName";

	public static final String TOP_LEVEL_PACKAGE_NAME_KIND = "topLevelPackage";

	private String name;

	private String replaceKind;

	private String description;

	// Typically String (text field) or Boolean (check box), but may be enums
	// (radio button) later as well
	private Class<?> type;

	private int page;

	private int order;

	private boolean required;

	private String defaultValue;

	private String pattern;

	private String errorMessage;

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public int getPage() {
		return page;
	}

	public String getPattern() {
		return pattern;
	}

	public String getReplaceKind() {
		return replaceKind;
	}

	public boolean getRequired() {
		return required;
	}

	public Class<?> getType() {
		return type;
	}
}
