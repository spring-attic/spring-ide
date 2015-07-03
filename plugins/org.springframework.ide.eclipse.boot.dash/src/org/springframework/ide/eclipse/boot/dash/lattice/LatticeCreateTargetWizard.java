/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.lattice;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

public class LatticeCreateTargetWizard extends Wizard {

	private LatticeCreateTargetWizardModel model;

	public LatticeCreateTargetWizard(LatticeCreateTargetWizardModel model) {
		this.model = model;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new Page(model));
	}

	
	class Page extends WizardPageWithSections {

		private LatticeCreateTargetWizardModel model;

		public Page(LatticeCreateTargetWizardModel model) {
			super("Create Lattice Target", "Create Lattice Target", BootDashActivator.getImageDescriptor("icons/lattice-wizard-icon.png"));
			this.model = model;
		}

		@Override
		protected List<WizardPageSection> createSections() {
			ArrayList<WizardPageSection> sections = new ArrayList<WizardPageSection>();
			sections.add(new StringFieldSection(this, model.getTarget()));
			return sections;
		}

	}
	
	@Override
	public boolean performFinish() {
		try {
			return model.performFinish();
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return false;
	}

}
