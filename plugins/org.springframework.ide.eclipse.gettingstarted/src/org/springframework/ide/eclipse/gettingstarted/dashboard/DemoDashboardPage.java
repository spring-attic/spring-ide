/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * This demonstrates how to create a simple dashboard page.
 */
public class DemoDashboardPage extends ADashboardPage {

	private String name;
	private String displayText;

	/**
	 * When used as IExecutableExtension the instance is created with default constructor
	 * and initialized via the {@link IExecutableExtension} instead.
	 */
	public DemoDashboardPage() {
	}
	
	public DemoDashboardPage(String name, String displayText) {
		this.name = name;
		this.displayText = displayText;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		Label label = new Label(parent, SWT.WRAP);
		label.setText(displayText);
	}

//	@Override
//	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
//		if (!(data instanceof Map)) {
//			ExceptionUtil.coreException("Must provide a 'name' and 'text' property");
//		} else {
//			@SuppressWarnings("unchecked")
//			Map<String,String> props = (Map<String,String>)data;
//			this.name = props.get("name");
//			this.displayText = props.get("text");
//		}
//	}
//
}
