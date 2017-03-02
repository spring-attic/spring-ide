/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public abstract class WizardPageSectionWithConfiguration extends WizardPageSection {


	public WizardPageSectionWithConfiguration(IPageWithSections owner) {
		super(owner);
	}

	protected WizardPageWithSections getWizardOwner() {
		return (WizardPageWithSections) owner;
	}

	protected Composite area(Composite parent) {

		Composite area = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(area);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(area);

		return area;
	}

}
