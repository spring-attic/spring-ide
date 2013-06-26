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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.gettingstarted.content.GSContent;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.content.GithubRepoContent;
import org.springframework.ide.eclipse.gettingstarted.dashboard.WebDashboardPage;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportConfiguration;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.gettingstarted.importing.ImportUtils;
import org.springframework.ide.eclipse.gettingstarted.util.UIThreadDownloadDisallowed;
import org.springframework.ide.eclipse.gettingstarted.wizard.LiveSet;
import org.springframework.ide.eclipse.gettingstarted.wizard.MultiSelectionModel;
import org.springframework.ide.eclipse.gettingstarted.wizard.SelectionModel;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * Core counterpart of GuideImportWizard (essentially this is a 'model' for the wizard
 * UI.
 * 
 * @author Kris De Volder
 */
public class GuideImportWizardModel {
	
	//TODO: when guide selection is changed and swithched back... elements get 
	//  codeset names may get deselected (i.e. if name is not valid it gets unselected
	// find a workaround.
	//
	//Idea: simply ignoring invalid 'selected names' should work. Then we can just 
	// retain the selected names across switches. When guide is selected again
	// the selected names from before will then be remembered.
	
	static final ValidationResult isDownloadingMessage(GithubRepoContent g) {
		return ValidationResult.info(g.getName()+" is downloading...");
	}

	public class CodeSetValidator extends LiveExpression<ValidationResult> {

		private LiveVariable<GettingStartedGuide> codesetProvider;
		private LiveSet<String> selectedNames;
		private LiveExpression<String[]> validCodesetNames;

		public CodeSetValidator(LiveVariable<GettingStartedGuide> guide, LiveSet<String> codesets, LiveExpression<String[]> validCodeSetNames) {
			this.codesetProvider = guide;
			this.selectedNames = codesets;
			this.validCodesetNames = validCodeSetNames;
			this.dependsOn(guide);
			this.dependsOn(codesets);
			this.dependsOn(validCodeSetNames);
		}

		@Override
		protected ValidationResult compute() {
			try {
				GithubRepoContent g = codesetProvider.getValue();
				if (g!=null) { //Don't check or produce errors unless a content provider has been selected.
					boolean codesetSelected = false;
					try {
						Set<String> names = selectedNames.getValue();
						if (names != null && !names.isEmpty()) {
							for (String name : names) {
								CodeSet cs = g.getCodeSet(name);
								if (cs!=null) {
									codesetSelected = true;
									ImportConfiguration conf = ImportUtils.importConfig(g, cs);
									ValidationResult valid = ImportUtils.validateImportConfiguration(conf);
									if (!valid.isOk()) {
										return valid;
									}
								}
							}
						}
						if (!codesetSelected) {
							//Selectiong nothing is only allowed if there is in fact nothing to select
							//otherwise at least on codeset must be selected for import.
							String[] validNames = validCodesetNames.getValue();
							if (validNames!=null && validNames.length>0) {
								return ValidationResult.error("At least one codeset should be selected");
							}
						}
					} catch (UIThreadDownloadDisallowed e) {
						scheduleDownloadJob();
						return isDownloadingMessage(g);
					}
				}
			} catch (Throwable e) {
				//Unexpected. So log it for more info but also try to create a sensible error message in
				// the wizard.
				GettingStartedActivator.log(e);
				return ValidationResult.error(ExceptionUtil.getMessage(e));
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
//	{ Note: its not needed anymore to preselect any names by default. This is now
//    done automatically by the wizard UI. When it populates checkboxes any new 
//    names will automatically be selected.
//  TODO: maybe this logic of selecting codesets automatically belong in the model rather than
//   the UI. But that is more complicated to implement (the checkboxes currently do not
//   listen to the model elements although they could.
	
//		codesets.addAll(GettingStartedGuide.defaultCodesetNames); //Select both codesets by default.
//	}
	
	/**
	 * The valid codeset names w.r.t. the currently selected guide
	 */
	public final LiveExpression<String[]> validCodesetNames = new LiveExpression<String[]>(null) {

		@Override
		protected String[] compute() {
			try {
				GithubRepoContent g = guide.getValue();
				if (g!=null) {
					List<CodeSet> validSets = g.getCodeSets();
					if (validSets!=null) {
						String[] names = new String[validSets.size()];
						for (int i = 0; i < names.length; i++) {
							names[i] = validSets.get(i).getName();
						}
						return names;
					}
				}
			} catch (UIThreadDownloadDisallowed e) {
				//Failed because content is not yet downloaded but this is ok... 
				//just schedule download to happen later and in the mean time return something sensible
				scheduleDownloadJob();
			} catch (Throwable e) {
				GettingStartedActivator.log(e);
			}
			return GettingStartedGuide.defaultCodesetNames;
		}
	};
	
	/**
	 * The build type chosen by user
	 */
	private LiveVariable<BuildType> buildType = new LiveVariable<BuildType>(BuildType.DEFAULT);
	
	private LiveExpression<ValidationResult> guideValidator = Validator.notNull(guide, "A Guide must be selected");
	private LiveExpression<ValidationResult> codesetValidator = new CodeSetValidator(guide, codesets, validCodesetNames);
	private LiveExpression<ValidationResult> buildTypeValidator = new Validator() {
		@Override
		protected ValidationResult compute() {
			try {
				GithubRepoContent g = guide.getValue();
				if (g!=null) {
					try {
						BuildType bt = buildType.getValue();
						if (bt==null) {
							return ValidationResult.error("No build type selected");
						} else {
							List<String> codesetNames = codesets.getValues();
							if (codesetNames!=null) {
								for (String csname : codesetNames) {
									CodeSet cs = g.getCodeSet(csname);
									if (cs!=null) {
										ValidationResult result = cs.validateBuildType(bt);
										if (!result.isOk()) {
											return result.withMessage("CodeSet '"+csname+"': "+result.msg);
										}
										ImportStrategy importStrategy = bt.getImportStrategy();
										if (!importStrategy.isSupported()) {
											//This means some required STS component like m2e or gradle tooling is not installed
											return ValidationResult.error(bt.getNotInstalledMessage());
										}
									}
								}
							}
						}
					} catch (UIThreadDownloadDisallowed e) {
						//Careful... check some of the validation will trigger downloads. This is not allowed in UI thread.
						scheduleDownloadJob();
						return isDownloadingMessage(g);
					}
				}
				return ValidationResult.OK;
			} catch (Throwable e) {
				GettingStartedActivator.log(e);
				return ValidationResult.error(ExceptionUtil.getMessage(e));
			}
		}

	};
	
	public LiveExpression<Boolean> isDownloaded = new LiveExpression<Boolean>(false) {
		@Override
		protected Boolean compute() {
			GithubRepoContent g = guide.getValue();
			return g == null || g.isDownloaded(); 
		}
	};
	
	public LiveExpression<ValidationResult> downloadStatus = new Validator() {
		@Override
		protected ValidationResult compute() {
			GSContent g = guide.getValue();
			if (g == null) {
				return ValidationResult.OK;
			} else {
				return g.getZip().getDownloadStatus();
			}
		}
	};

	/**
	 * The description of the current guide.
	 */
	public final LiveExpression<String> description = new LiveExpression<String>("<no description>") {
		@Override
		protected String compute() {
			GithubRepoContent g = guide.getValue();
			if (g!=null) {
				return g.getDescription();
			}
			return "<no guide selected>";
		}
	};
	
	public final LiveExpression<URL> homePage = new LiveExpression<URL>(null) {
		@Override
		protected URL compute() {
			GithubRepoContent g = guide.getValue();
			if (g!=null) {
				return g.getHomePage();
			}
			return null;
		}
	};

	/**
	 * Indicates whether the user has selected the option to open the home page.
	 */
	private LiveVariable<Boolean> enableOpenHomePage = new LiveVariable<Boolean>(true);
	
	{
		buildTypeValidator.dependsOn(guide);
		buildTypeValidator.dependsOn(isDownloaded);
		buildTypeValidator.dependsOn(buildType);
		buildTypeValidator.dependsOn(codesets);
		
		isDownloaded.dependsOn(guide);
		downloadStatus.dependsOn(guide);
		
		description.dependsOn(guide);
		
		homePage.dependsOn(guide);
		
		validCodesetNames.dependsOn(guide);
		validCodesetNames.dependsOn(isDownloaded);
		
		codesetValidator.dependsOn(isDownloaded);
		//Note: some other dependsOn are registered inside CodeSetValidator class itself. 
		// isDownloaded is an exception because is still null when CodeSetValidator class gets
		// instantiated.
	}
	
	/**
	 * Downloads currently selected guide content (if it is not already cached locally.
	 */
	public void performDownload(IProgressMonitor mon) throws Exception {
		mon.beginTask("Downloading", 1);
		try {
			GithubRepoContent g = guide.getValue();
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
		GithubRepoContent g = guide.getValue();
		BuildType bt = buildType.getValue();
		Set<String> codesetNames = codesets.getValue();
		
		mon.beginTask("Import guide content", codesetNames.size()+1);
		try {
			for (String name : codesetNames) {
				CodeSet cs = g.getCodeSet(name);
				if (cs==null) {
					//Ignore 'invalid' codesets. This is a bit of a hack so that we can retain selected codeset names
					//  across guide selection changes. To do that we remember 'selected' cs names even if they
					//  aren't valid for the current guide. That way the checkbox state stays consistent
					//  when switching between guides (otherwise 'invalid' names would have to be cleared when switching to
					//  a guide). 
					mon.worked(1);
				} else {
					IRunnableWithProgress oper = bt.getImportStrategy().createOperation(ImportUtils.importConfig(
							g, 
							cs
					));
					oper.run(new SubProgressMonitor(mon, 1));
				}
			}
			if (enableOpenHomePage.getValue()) {
				URL url = homePage.getValue();
				if (url!=null) {
					UiUtil.openUrl(url.toString());
				}
			}
			return true;
		} catch (UIThreadDownloadDisallowed e) {
			//This shouldn't be possible... Finish button won't be enabled unless all is validated. 
			//This implies the content has been downloaded (can't be validated otherwise).
			GettingStartedActivator.log(e);
			return false;
		} finally {
			mon.done();
		}
	}
	
	
	
	public void setGuide(GettingStartedGuide guide) {
		this.guide.setValue(guide);
	}
	
	public GithubRepoContent getGuide() {
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

	public LiveVariable<Boolean> getEnableOpenHomePage() {
		return enableOpenHomePage;
	}

}
