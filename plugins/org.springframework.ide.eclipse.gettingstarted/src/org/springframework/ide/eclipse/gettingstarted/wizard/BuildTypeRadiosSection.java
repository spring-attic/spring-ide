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
package org.springframework.ide.eclipse.gettingstarted.wizard;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

/**
 * Group of radio buttons that allows selection a BuildType
 */
public class BuildTypeRadiosSection extends GroupSection {
	
	private static class BuildTypeChoice extends WizardPageSection {

		private BuildType type;
		private LiveVariable<BuildType> selection;

		public BuildTypeChoice(IPageWithSections owner, BuildType buildType, LiveVariable<BuildType> selection) {
			super(owner);
			this.type = buildType;
			this.selection = selection;
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			//This validator really doesn't matter because for a radio button group it makes
			// more sense to create a validator for the group rather than to compose it from
			// each component in the group.
			return Validator.constant(ValidationResult.OK);
		}

		@Override
		public void createContents(Composite page) {
			final Button button = new Button(page, SWT.RADIO);
			button.setText(type.displayName());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(button);
			button.setSelection(selection.getValue()==type);
			button.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (button.getSelection()) {
						selection.setValue(type);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (button.getSelection()) {
						selection.setValue(type);
					}
				}
			});
		}

	}

	private SelectionModel<BuildType> selection;

	@Override
	protected GridLayout createLayout() {
		return new GridLayout(2, true);
	}
	
	public BuildTypeRadiosSection(WizardPageWithSections owner, SelectionModel<BuildType> selection) {
		super(owner, "Build Type", createSections(owner, selection));
		this.selection = selection;
	}

	private static WizardPageSection[] createSections(WizardPageWithSections owner, SelectionModel<BuildType> selection) {
		BuildType[] types = BuildType.values();
		WizardPageSection[] section = new WizardPageSection[types.length];
		for (int i = 0; i < section.length; i++) {
			section[i] = new BuildTypeChoice(owner, types[i], selection.selection);
		}
		return section;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return selection.validator;
	}
	
}
