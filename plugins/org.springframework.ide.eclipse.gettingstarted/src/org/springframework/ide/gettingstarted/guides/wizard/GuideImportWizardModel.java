/*******************************************************************************
 * Copyright (c) 2013 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.guides.wizard;

import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Core counterpart of GuideImportWizard (essentially this is a 'model' for the wizard
 * UI.
 * 
 * @author Kris De Volder
 */
public class GuideImportWizardModel {

	private LiveVariable<GettingStartedGuide> guide = new LiveVariable<GettingStartedGuide>();
	private LiveVariable<BuildType> buildType = new LiveVariable<BuildType>();
	
	private LiveExpression<ValidationResult> guideValidator = Validator.notNull(guide, "A Guide must be selected");
	private LiveExpression<ValidationResult> buildTypeValidator = new Validator() {
		@Override
		protected ValidationResult compute() {
			GettingStartedGuide g = guide.getValue();
			if (g!=null) {
				BuildType bt = buildType.getValue();
				if (bt==null) {
					return ValidationResult.error("No build type selected");
				}
				return g.validateBuildType(bt);
			}
			return ValidationResult.OK;
		}
	};

	{
		buildTypeValidator.dependsOn(guide);
		buildTypeValidator.dependsOn(buildType);
	}
	
	
	public void setGuide(GettingStartedGuide guide) {
		this.guide.setValue(guide);
	}
	
	public GettingStartedGuide getGuide() {
		return guide.getValue();
	}

	public SelectionModel<BuildType> getBuildTypeModel() {
		return new SelectionModel<BuildType>(buildType, buildTypeValidator);
	}

	public SelectionModel<GettingStartedGuide> getGuideSelectionModel() {
		return new SelectionModel<GettingStartedGuide>(guide, guideValidator);
	}
}
