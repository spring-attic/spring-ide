/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.guides.wizard;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Allow choosing a guide in pull-down style combo box or table viewer.
 * 
 * @author Kris De Volder
 */
public class ChooseGuideSection extends WizardPageSection {
	
	private ChooseOneSection wrappee;

	public ChooseGuideSection(
			GuideImportWizardPageOne owner,
			SelectionModel<GettingStartedGuide> model
	) {
		super(owner);
		if (model.selection.getValue()!=null) {
			wrappee = new ChooseOneSectionCombo<GettingStartedGuide>(
					owner, "Guide", model, GettingStartedContent.getInstance().getGuides());
		} else {
			wrappee = new ChooseOneSectionTable<GettingStartedGuide>(
					owner, "Guide", model, GettingStartedContent.getInstance().getGuides());
		}
		wrappee.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof GettingStartedGuide) {
					GettingStartedGuide gsg = (GettingStartedGuide) element;
					return gsg.getName();
				}
				return super.getText(element);
			}
		});
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return wrappee.getValidator();
	}

	@Override
	public void createContents(Composite page) {
		wrappee.createContents(page);
	}
		
}
