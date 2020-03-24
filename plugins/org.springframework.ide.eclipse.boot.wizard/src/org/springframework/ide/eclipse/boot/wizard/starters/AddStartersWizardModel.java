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
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class AddStartersWizardModel {
	private static final long MODEL_CREATION_TIMEOUT = 30000;

	// Factory that creates the model.
	private final InitializrFactoryModel<AddStartersModel> initializrFactory;

	private final LiveVariable<ValidationResult> modelLoadingValidator = new LiveVariable<ValidationResult>();

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

				AddStartersCompareModel compareModel = new AddStartersCompareModel(projectDownloader, bootProject);
				AddStartersModel model = new AddStartersModel(compareModel, bootProject, preferenceStore);

				return model;
			} else {
				return null;
			}
		});
	}

	public void loadFromInitializr() {

		LiveExpression<AddStartersModel> modelLiveExpression = this.initializrFactory.getModel();

		// Gradle project may need to take a bit of time to extract boot version from the model
		long startTime = System.currentTimeMillis();
		while (modelLiveExpression.getValue() == null && System.currentTimeMillis() - startTime < MODEL_CREATION_TIMEOUT) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// Ignore
			}
		}

		AddStartersModel model = modelLiveExpression.getValue();
		if (model != null) {
			try {
				this.modelLoadingValidator
						.setValue(ValidationResult.info("Fetching starter information from Spring Boot Initializr"));

				model.loadFromInitializr();

				this.modelLoadingValidator.setValue(ValidationResult.OK);

			} catch (Exception e) {
				ValidationResult parseError = parseError(model, e);
				this.modelLoadingValidator.setValue(parseError);
			}
		} else {
			this.modelLoadingValidator.setValue(ValidationResult.error("Timeed out creating Spring Boot project's model"));
		}
	}

	/**
	 *
	 * @return factory that creates add starter model which contains project information and dependencies
	 */
	public InitializrFactoryModel<AddStartersModel> getInitializrFactoryModel() {
		return this.initializrFactory;
	}

	public LiveVariable<ValidationResult> getValidator() {
		return this.modelLoadingValidator;
	}

	private ValidationResult parseError(AddStartersModel model, Exception e) {
		if (ExceptionUtil.getDeepestCause(e) instanceof FileNotFoundException) {
			// Crude way to interpret that project boot version is not available in
			// initializr
			StringBuffer message = new StringBuffer();

			// Get the boot version from project directly as opposed from the starters info,
			// as it may
			// not be available.
			String bootVersionFromProject = model.getProject().getBootVersion();
			message.append("Unable to download content for boot version: ");
			message.append(bootVersionFromProject);
			message.append(". The version may not be available.  Consider updating to a newer version.");

			// Log the full message
			Log.log(e);

			return ValidationResult.error(message.toString());
		}
		return ValidationResult.from(ExceptionUtil.status(e));
	}

	public boolean performFinish() {
		getInitializrFactoryModel().getModel().getValue().performOk();
		return true;
	}

	public boolean canFinish() {
		return this.modelLoadingValidator.getValue() != null && this.modelLoadingValidator.getValue().isOk();
	}
}