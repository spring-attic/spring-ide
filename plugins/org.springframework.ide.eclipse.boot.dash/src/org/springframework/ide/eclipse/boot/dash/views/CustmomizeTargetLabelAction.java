/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.dialogs.EditTemplateDialogModel;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class CustmomizeTargetLabelAction extends AbstractBootDashModelAction {

	protected CustmomizeTargetLabelAction(LiveExpression<BootDashModel> section, UserInteractions ui) {
		super(section, ui);
		setText("Customize Label...");
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(isApplicable(sectionSelection.getValue()));
	}

	public void updateVisibility() {
		this.setVisible(isApplicable(sectionSelection.getValue()));
	}

	private boolean isApplicable(BootDashModel section) {
		if (section!=null) {
			PropertyStoreApi props = section.getRunTarget().getType().getPersistentProperties();
			//Not all target types provide persistent properties yet. This feature only works on
			// those target types that do.
			return props!=null;
		}
		return false;
	}

	@Override
	public void run() {
		final BootDashModel section = sectionSelection.getValue();
		if (isApplicable(section)) {
			final RunTargetType type = section.getRunTarget().getType();
			EditTemplateDialogModel model = new EditTemplateDialogModel() {
				{
					template.setValue(section.getNameTemplate());
				}

				@Override
				public String getTitle() {
					String type = section.getRunTarget().getType().getName();
					return "Customize Labels for "+type+" Target(s)";
				}
				@Override
				public void performOk() throws Exception {
					if (applyToAll.getValue()) {
						section.getRunTarget().getType().setNameTemplate(template.getValue());
						//To *really* apply the template to *all* targets of a given type, we must make sure
						// that the targets do not override the value individually:
						for (BootDashModel model : section.getViewModel().getSectionModels().getValue()) {
							if (model.getRunTarget().getType().equals(type)) {
								model.setNameTemplate(null);
								model.notifyModelStateChanged();
							}
						}
					} else {
						section.setNameTemplate(template.getValue());
						section.notifyModelStateChanged();
					}
				}
				@Override
				public String getHelpText() {
					return type.getTemplateHelpText();
				}
				@Override
				public String getDefaultValue() {
					return type.getDefaultNameTemplate();
				}
				@Override
				public String getApplyToAllLabel() {
					return "Apply to all "+type.getName()+" targets";
				}
				@Override
				public boolean getApplyToAllDefault() {
					for (BootDashModel section : section.getViewModel().getSectionModels().getValue()) {
						if (
								section.getRunTarget().getType().equals(type) &&
								section.hasCustomNameTemplate()
						) {
							return false;
						}
					}
					return true;
				}
			};
			ui.openEditTemplateDialog(model);
		}
	}

}
