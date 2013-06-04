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
package org.springframework.ide.eclipse.gettingstarted.guides.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;

/**
 * Core counterpart of GuideImportWizard (essentially this is a 'model' for the wizard
 * UI.
 * 
 * @author Kris De Volder
 */
public class GuideImportWizardModel {
	
	//TODO: Validate build system choice against installed tooling. (warn if m2e / gradle tooling is
	// required but not installed.
	
	//TODO: Validation: shouldn't allow importing if something already exists where codeset content
	// will be downloaded. This will overwrite what's there. At the very least a warning should
	// appear in the wizard.
	
	//TODO: Make guides chooser section use a list/table widget instead of Combo. There are too many
	// items for a Combo to be nice. Make the list widget 'searchable'.

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
	{
		codesets.addAll(GettingStartedGuide.codesetNames); //Select both codesets by default.
	}
	
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
						scheduleDownloadJob();
						return ValidationResult.info(g.getName()+" is downloading...");
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
				g.getZip().getFile(); //This forces download
			}
		} finally {
			isDownloaded.refresh();
			mon.done();
		}
	}
	
	private void scheduleDownloadJob() {
		Job job = new Job("Downloading guide content") {
			protected IStatus run(IProgressMonitor mon) {
				try {
					performDownload(mon);
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	}
	
	
	/**
	 * Performs the final step of the wizard when user clicks on Finish button.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	public boolean performFinish(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
		//The import will be carried out with whatever the currently selected values are
		// in all the input fields / variables / widgets.
		GettingStartedGuide g = guide.getValue();
		BuildType bt = buildType.getValue();
		Set<String> codesetNames = codesets.getValue();
		
		mon.beginTask("Import guide codeset(s)", codesetNames.size());
		try {
			for (String name : codesetNames) {
				IRunnableWithProgress oper = bt.getImportStrategy().createOperation(ImportUtils.importConfig(
						g, 
						g.getCodeSet(name)
				));
				oper.run(new SubProgressMonitor(mon, 1));
			}
			return true;
		} finally {
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
