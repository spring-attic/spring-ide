/*******************************************************************************
 * Copyright (c) 2014 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

/**
 * Extension of {@link RadioInfo} adds extra attribute(s) only
 * applicable to the the 'type' input field.
 *
 * @author Kris De Volder
 */
public class TypeRadioInfo extends RadioInfo {

	private String action;

	public String getAction() {
		return action;
	}

	public TypeRadioInfo(String groupName, String value, boolean checked, String action) {
		super(groupName, value, checked);
		this.action = action;
	}

	@Override
	public String toString() {
		return "Radio(" + getGroupName()+", "+getValue()+", "+action+")";
	}

}
