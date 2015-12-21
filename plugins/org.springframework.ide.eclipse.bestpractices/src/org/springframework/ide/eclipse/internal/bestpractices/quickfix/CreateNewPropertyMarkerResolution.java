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
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import org.eclipse.core.resources.IMarker;

/**
 * Resolution for missing property markers. Creates a corresponding setter in
 * the class referenced by the bean definition.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class CreateNewPropertyMarkerResolution extends AbstractCreateMethodMarkerResolution {

	private String missingProperty = "";

	private String targetClass = "";

	public CreateNewPropertyMarkerResolution(IMarker marker) {
		super(marker);
		missingProperty = extractQuotedString("property '", getMarkerMessage());
		targetClass = extractQuotedString("class '", getMarkerMessage());
	}

	@Override
	public String getDescription() {
		return "Create property '" + missingProperty + "' in class '" + targetClass + "'";
	}

	@Override
	public String getLabel() {
		return "Create property '" + missingProperty + "' in class '" + targetClass + "'";
	}

	@Override
	protected String getNewMethodName() {
		return propertyToSetterName(missingProperty);
	}

	@Override
	protected String getNewMethodParameters() {
		return "object";
	}

	@Override
	protected String getTargetClass() {
		return targetClass;
	}

	private String propertyToSetterName(String propertyName) {
		String firstChar = propertyName.substring(0, 1);
		String setterName = "set" + firstChar.toUpperCase() + propertyName.substring(1);
		return setterName;
	}
}
