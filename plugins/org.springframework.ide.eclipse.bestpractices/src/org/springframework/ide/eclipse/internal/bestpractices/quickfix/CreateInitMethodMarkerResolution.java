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
 * Resolution for missing init method markers. Creates a corresponding method in
 * the class referenced by the bean definition.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class CreateInitMethodMarkerResolution extends AbstractCreateMethodMarkerResolution {

	private String missingMethod = "";

	private String targetClass = "";

	public CreateInitMethodMarkerResolution(IMarker marker) {
		super(marker);
		missingMethod = extractQuotedString("Init-method '", getMarkerMessage());
		targetClass = extractQuotedString("class '", getMarkerMessage());
	}

	@Override
	public String getDescription() {
		return "Create method '" + missingMethod + "' in class '" + targetClass + "'";
	}

	@Override
	public String getLabel() {
		return "Create method '" + missingMethod + "' in class '" + targetClass + "'";
	}

	@Override
	protected String getNewMethodName() {
		return missingMethod;
	}

	@Override
	protected String getTargetClass() {
		return targetClass;
	}

}
