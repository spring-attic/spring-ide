/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.guides.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

/**
 * @author Kris De Volder
 */
public class GuideImportWizardPageOne extends WizardPageWithSections {

	private GuideImportWizardModel model;

	protected GuideImportWizardPageOne(GuideImportWizardModel model) {
		super("Page One", "Import Getting Started Guide", null);
		this.model = model;
	}
	
	@Override
	protected List<WizardPageSection> createSections() {
		List<WizardPageSection> sections = new ArrayList<WizardPageSection>();
		ChooseOneSection<GettingStartedGuide> chooseGuide = new ChooseOneSection<GettingStartedGuide>(this, 
			"Guide", 
			model.getGuideSelectionModel(),
			GettingStartedContent.getInstance().getGuides()
		).setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GettingStartedGuide) {
					GettingStartedGuide gsg = (GettingStartedGuide) element;
					return gsg.getName();
				}
				return super.getText(element);
			}
		});
		sections.add(new RowSection(this, new WizardPageSection[] {
				chooseGuide,
				new DownloadButtonSection(this, model)
		}));
		
		sections.add(new CodeSetCheckBoxesSection(this, GettingStartedGuide.codesetNames, model.getCodeSetModel()));
		sections.add(new BuildTypeRadiosSection(this, model.getBuildTypeModel()));
		sections.add(new DescriptionSection(this, model.description));
		
		return sections;
	}

}
