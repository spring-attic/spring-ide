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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
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

	public static class CodeSetValidator extends LiveExpression<ValidationResult> {

		private LiveVariable<GettingStartedGuide> codesetProvider;
		private LiveSet<String> selectedNames;

		public CodeSetValidator(LiveVariable<GettingStartedGuide> guide, LiveSet<String> codesets) {
			this.codesetProvider = guide;
			this.selectedNames = codesets;
			this.dependsOn(guide);
			this.dependsOn(codesets);
		}

		@Override
		protected ValidationResult compute() {
			GettingStartedGuide g = codesetProvider.getValue();
			if (g!=null) { //Don't check or produce errors unless a content provider has been selected.
				Set<String> names = selectedNames.getValue();
				if (names == null || names.isEmpty()) {
					return ValidationResult.error("No codeset selected");
				}
			}
			return ValidationResult.OK;
		}

	}

	/**
	 * The chosen guide to import stuff from.
	 */
	private LiveVariable<GettingStartedGuide> guide = new LiveVariable<GettingStartedGuide>();
	
	/**
	 * The names of the codesets selected for import.
	 */
	private LiveSet<String> codesets = new LiveSet<String>(new HashSet<String>());
	
	/**
	 * The build type chosen by user
	 */
	private LiveVariable<BuildType> buildType = new LiveVariable<BuildType>(BuildType.DEFAULT);
	
	private LiveExpression<ValidationResult> guideValidator = Validator.notNull(guide, "A Guide must be selected");
	private LiveExpression<ValidationResult> codesetValidator = new CodeSetValidator(guide, codesets);
	private LiveExpression<ValidationResult> buildTypeValidator = new Validator() {
		@Override
		protected ValidationResult compute() {
			GettingStartedGuide g = guide.getValue();
			if (g!=null) {
				BuildType bt = buildType.getValue();
				if (bt!=null) {
					//Careful... check if downloaded before doing checks that require access to downloaded content.
					// otherwise will end up blocking UI thread waiting for download.
					if (!g.isDownloaded()) {
						return ValidationResult.error(guide.getValue().getName()+" needs to be downloaded. Click download button to proceed.");
					}
					return g.validateBuildType(bt);
				}
				return ValidationResult.error("No build type selected");
			}
			return ValidationResult.OK;
		}
	};
	
	public LiveExpression<Boolean> isDownloaded = new LiveExpression<Boolean>(false) {
		@Override
		protected Boolean compute() {
			GettingStartedGuide g = guide.getValue();
			return g == null || g.isDownloaded(); 
		}
	};

	/**
	 * The description of the current guide.
	 */
	public final LiveExpression<String> description = new LiveExpression<String>("<no description>") {
		@Override
		protected String compute() {
			GettingStartedGuide g = guide.getValue();
			if (g!=null) {
				return g.getDescription();
			}
			return "<no guide selected>";
		}
	};
	
	{
		buildTypeValidator.dependsOn(guide);
		buildTypeValidator.dependsOn(isDownloaded);
		buildTypeValidator.dependsOn(buildType);
		
		isDownloaded.dependsOn(guide);
		
		description.dependsOn(guide);
	}
	
	/**
	 * Downloads currently selected guide content (if it is not already cached locally.
	 */
	public void performDownload(IProgressMonitor mon) throws IOException {
		mon.beginTask("Downloading", 1);
		try {
			GettingStartedGuide g = guide.getValue();
			if (g!=null) {
				g.getZip().getFile();
			}
		} finally {
			isDownloaded.refresh();
			mon.done();
		}
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
	
	public MultiSelectionModel<String> getCodeSetModel() {
		return new MultiSelectionModel<String>(codesets, codesetValidator);
	}
	
	public LiveExpression<Boolean> isDownloaded() {
		return isDownloaded;
	}
}
