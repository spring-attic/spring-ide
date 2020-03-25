/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrlBuilders;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 *
 * Model for the add starters wizard that requires  a local project that will be used to:
 *
 * <p/>
 *
 * 1. Build a primary "initializr project model" that contains BOTH local project information, like boot version, as well  as information downloaded
 * from initializr, like dependencies specific for that boot version. Wizard  dependency selections are also stored in this model. This  model also
 * performs comparison between local project and downloaded project
 *
 * <p/>
 *
 * 2. Download Starter information from initializr like  dependencies, and add this information to the initializr project model.
 *
 * <p/>
 *
 * The  wizard  model is essentially a wrapper  around the initializr project model that  contains additional "wizard" functionality like dealing with "OK pressed"
 *
 */
public class AddStartersWizardModel implements OkButtonHandler {
	private static final long MODEL_CREATION_TIMEOUT = 30000;

	// Factory that creates the model.
	private final InitializrFactoryModel<InitializrModel> initializrFactory;

	private final LiveVariable<ValidationResult> modelLoadingValidator = new LiveVariable<ValidationResult>();

	private final FieldModel<String> bootVersion = new StringFieldModel("Spring Boot Version:", "");

	private Runnable okRunnable;

	public AddStartersWizardModel(IProject project, IPreferenceStore preferenceStore) throws Exception {

		initializrFactory = new InitializrFactoryModel<>((url) -> {
			if (url != null) {
				URLConnectionFactory urlConnectionFactory = BootActivator.getUrlConnectionFactory();
				String initializrUrl = BootPreferences.getInitializrUrl();

				InitializrService initializr = InitializrService.create(urlConnectionFactory, () -> url);
				SpringBootCore core = new SpringBootCore(initializr);

				InitializrUrlBuilders urlBuilders = new InitializrUrlBuilders();
				InitializrProjectDownloader projectDownloader = new InitializrProjectDownloader(urlConnectionFactory,
						initializrUrl, urlBuilders);

				ISpringBootProject bootProject = core.project(project);

				this.bootVersion.setValue(bootProject.getBootVersion());

				InitializrModel model = new InitializrModel(bootProject, projectDownloader, preferenceStore);

				return model;
			} else {
				return null;
			}
		});
	}

	public FieldModel<String> getBootVersion() {
		return bootVersion;
	}

	@Override
	public void performOk() {
		LiveExpression<InitializrModel> modelLiveExpression = this.initializrFactory.getModel();
		InitializrModel model = modelLiveExpression.getValue();
		if (model != null) {
			model.updateDependencyCount();
		}

		if (this.okRunnable != null) {
			this.okRunnable.run();
		}
	}

	public void onOkPressed(Runnable okRunnable) {
		this.okRunnable = okRunnable;
	}

	/**
	 * Download information  from  initializr, like available dependencies for a particular boot version.
	 * Use {@link #getValidator()} to be  notified  when  information becomes available.
	 */
	public void downloadStarterInfos() {
		InitializrModel model = this.initializrFactory.getModel().getValue();
		if (model != null) {
			try {
				this.modelLoadingValidator
						.setValue(ValidationResult.info("Fetching starter information from Spring Boot Initializr"));

				model.downloadStarterInfos();

				this.modelLoadingValidator.setValue(ValidationResult.OK);

			} catch (Exception e) {
				ValidationResult parseError = parseError(model, e);
				this.modelLoadingValidator.setValue(parseError);
			}
		}
	}

	/**
	 *
	 * @return factory that creates add starter model which contains project information and dependencies
	 */
	public InitializrFactoryModel<InitializrModel> getInitializrFactoryModel() {
		return this.initializrFactory;
	}

	/**
	 *
	 * @return validator that notifies when model creation completes successfully, as well as information download from initializr, as well as any errors that occur
	 */
	public LiveVariable<ValidationResult> getValidator() {
		return this.modelLoadingValidator;
	}

	private ValidationResult parseError(InitializrModel model, Exception e) {
		String shortMessage = null;
		StringBuffer detailsBuffer = new StringBuffer();

		if (ExceptionUtil.getDeepestCause(e) instanceof FileNotFoundException) {
			shortMessage = "Error encountered while resolving content";
			detailsBuffer.append(
					"Initializr content for the project's boot version is not available. Considering updating to a newer boot version.");
		} else {
			shortMessage = "Unknown problem occured while loading content for: " + model.getProject().getProject().getName();
			detailsBuffer.append(shortMessage);
		}

		String exceptionMsg = ExceptionUtil.getMessage(e);
		if (exceptionMsg != null) {
			detailsBuffer.append('\n');
			detailsBuffer.append('\n');
			detailsBuffer.append("Full Error:");
			detailsBuffer.append('\n');
			detailsBuffer.append(exceptionMsg);
		}
		return AddStartersError.from(shortMessage, detailsBuffer.toString());
	}

	/**
	 * Creates the initializr model for the local project. This does NOT download anything from initializr. It just builds the model based on the local
	 * project
	 */
	public void createInitializrModelForProject() {
		LiveExpression<InitializrModel> modelLiveExpression = getInitializrFactoryModel().getModel();

		// Gradle project may need to take a bit of time to extract boot version from the model
		long startTime = System.currentTimeMillis();
		while (modelLiveExpression.getValue() == null && System.currentTimeMillis() - startTime < MODEL_CREATION_TIMEOUT) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

		if (modelLiveExpression.getValue() == null) {
			modelLoadingValidator.setValue(ValidationResult.error("Timed out creating Spring Boot project's model"));
		}
	}

	public void dispose() {
		InitializrModel model = getInitializrFactoryModel().getModel().getValue();
		if (model != null) {
			model.dispose();
		}
	}
}