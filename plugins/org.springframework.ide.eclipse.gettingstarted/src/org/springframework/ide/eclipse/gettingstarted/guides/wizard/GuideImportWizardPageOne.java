///*******************************************************************************
// * Copyright (c) 2013 GoPivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// * GoPivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.gettingstarted.guides.wizard;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;
//import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
//import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;
//
///**
// * @author Kris De Volder
// */
//public class GuideImportWizardPageOne extends WizardPageWithSections {
//
//	private GuideImportWizardModel model;
//
//	protected GuideImportWizardPageOne(GuideImportWizardModel model) {
//		super("Page One", "Import Getting Started Guide", null);
//		this.model = model;
//	}
//	
//	@Override
//	protected List<WizardPageSection> createSections() {
//		List<WizardPageSection> sections = new ArrayList<WizardPageSection>();
//
//		sections.add(new ChooseGuideSection(this, model.getGuideSelectionModel()));
//		sections.add(new BuildTypeRadiosSection(this, model.getBuildTypeModel()));
//		sections.add(new CodeSetCheckBoxesSection(this, GettingStartedGuide.codesetNames, model.getCodeSetModel()));
//		sections.add(new DescriptionSection(this, model.description));
//		
//		return sections;
//	}
//
//}
