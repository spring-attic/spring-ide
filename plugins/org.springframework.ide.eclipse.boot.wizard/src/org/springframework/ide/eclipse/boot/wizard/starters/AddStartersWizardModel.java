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
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.HttpRedirectionException;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Option;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrlBuilders;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
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



	private Runnable okRunnable;
	private IPreferenceStore preferenceStore;
	private IProject project;

	// Use separate validators, and add them to a general model validator. The reason to have separate validators
	// is to be more precise which validator to use in certain model components, for example, using a specific
	// validator just for boot version that just notifies the boot version field model, but not any other unrelated
	// model that shouldn't be notified to changes to boot version.
	private final LiveVariable<ValidationResult> modelValidator = new LiveVariable<ValidationResult>();
	private final LiveVariable<ValidationResult> bootVersionValidator = new LiveVariable<ValidationResult>();

	private final FieldModel<String> bootVersion = new StringFieldModel("Spring Boot Version:", "").validator(bootVersionValidator);
	private final StringFieldModel serviceUrlField = new StringFieldModel("Service URL", null);

	private final LiveVariable<InitializrModel> modelExp = new LiveVariable<InitializrModel>(null);


	private final CompositeValidator validator = new CompositeValidator();

	{
		validator.addChild(modelValidator);
		validator.addChild(bootVersionValidator);

		modelExp.dependsOn(serviceUrlField.getVariable());
	}

	public AddStartersWizardModel(IProject project, IPreferenceStore preferenceStore) throws Exception {
		this.project = project;
		this.preferenceStore = preferenceStore;
		String initializrUrl = BootPreferences.getInitializrUrl();
		serviceUrlField.getVariable().setValue(initializrUrl);
	}

	public FieldModel<String> getBootVersion() {
		return bootVersion;
	}

	@Override
	public void performOk() {
		InitializrModel model = modelExp.getValue();
		if (model != null) {
			model.updateDependencyCount();
		}

		if (serviceUrlField.getValue() != null) {
			BootPreferences.addInitializrUrl(serviceUrlField.getValue());
		}

		if (this.okRunnable != null) {
			this.okRunnable.run();
		}
	}

	public void onOkPressed(Runnable okRunnable) {
		this.okRunnable = okRunnable;
	}

	private void handleError(InitializrModel model, Exception e) {
		String shortMessage = null;
		StringBuffer detailsBuffer = new StringBuffer();

		// This error is specific to boot version validator, so inform the boot version
		// validator
		if (ExceptionUtil.getDeepestCause(e) instanceof FileNotFoundException) {
			shortMessage = "Error encountered while resolving content";
			detailsBuffer.append(
					"Initializr content for the project's boot version is not available. Considering updating the project to a newer supported boot version:");
			Option[] availableBootVersions = model.getAvailableBootVersions();
			if (availableBootVersions != null) {
				detailsBuffer.append('\n');
				for (Option option : availableBootVersions) {
					detailsBuffer.append('\n');
					detailsBuffer.append(option.getId());
				}
			}

			AddStartersError result = AddStartersErrorUtil.getError(shortMessage, detailsBuffer.toString(), e);

			// Notify the boot version validator only as the associated field model should
			// only react
			// when boot version problems are encountered,but not any other problem
			this.bootVersionValidator.setValue(result);

		} else {
			this.modelValidator.setValue(AddStartersErrorUtil.getError(model, e));
		}
	}


	/**
	 * Triggers the initializr model creation and downloading of data into the model from initializr service
	 *
	 * This is a long-running synchronous operation
	 */
	public void load() {

		modelValidator.setValue(ValidationResult.info("Creating Boot project model..."));

		InitializrModel  model = createModel();
		if (model != null) {
			try {
				modelValidator.setValue(ValidationResult.info("Fetching starter information from Spring Boot Initializr..."));
				model.downloadDependencies();
				modelValidator.setValue(ValidationResult.OK);
			} catch (Exception e) {
				handleError(model, e);
			}
			modelExp.setValue(model);
		}
	}

	private InitializrModel createModel()  {

		try {
			String url = serviceUrlField.getValue();
			if (url != null) {
				URLConnectionFactory urlConnectionFactory = BootActivator.getUrlConnectionFactory();

				InitializrService initializr = InitializrService.create(urlConnectionFactory, () -> url);
				SpringBootCore core = new SpringBootCore(initializr);

				InitializrUrlBuilders urlBuilders = new InitializrUrlBuilders();
				InitializrProjectDownloader projectDownloader = new InitializrProjectDownloader(urlConnectionFactory,
						url, urlBuilders);
				InitializrServiceSpec serviceSpec = InitializrServiceSpec.parseFrom(urlConnectionFactory, new URL(url));

				ISpringBootProject bootProject = core.project(project);

				this.bootVersion.setValue(bootProject.getBootVersion());

				InitializrModel model = new InitializrModel(bootProject, projectDownloader, serviceSpec, preferenceStore);
				modelValidator.setValue(ValidationResult.OK);

				return model;
			} else {
				return null;
			}
		} catch (Exception _e) {
			Throwable e = ExceptionUtil.getDeepestCause(_e);
			if (e instanceof HttpRedirectionException) {
				serviceUrlField.getVariable().setValue(((HttpRedirectionException)e).redirectedTo);
			} else {
				modelValidator.setValue(ValidationResult.error(ExceptionUtil.getMessage(e)));
			}
		}
		return null;
	}


	public void dispose() {
		InitializrModel model = modelExp.getValue();
		if (model != null) {
			model.dispose();
		}
	}

	public LiveExpression<InitializrModel> getModel() {
		return modelExp;
	}

	/**
	 *
	 * @return Validator that notifies when different stages of initializr model creation and data download complete, or any errors that may occur
	 */
	public LiveExpression<ValidationResult> getValidator() {
		return this.validator;
	}
}