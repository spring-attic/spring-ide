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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

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
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
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
 * performs comparison between local project and downloaded project.
 *
 * <p/>
 *
 * 2. The model is designed such that the actual model is performed MANUALLY by an external caller. The external caller can register
 * a initializr service URL change listener, that gets notified when service URL changes in the model. But it is up to the
 * external caller to decide if, when service URL changes, whether to load the model. The reason for this delegation of model loading
 * control to the external caller is that it allows the caller to invoke model loading within that external caller's progress service or mechanism.
 * <p/>
 *
 * The  wizard  model is essentially a wrapper  around the initializr project model that  contains additional "wizard" functionality like dealing with "OK pressed"
 *
 */
public class AddStartersWizardModel implements OkButtonHandler {



	private static final String MISSING_INITIALIZR_SERVICE_URL = "Missing Initializr Service URL.";
	private Runnable okRunnable;
	private IPreferenceStore preferenceStore;
	private IProject project;

	// Use separate validators, and add them to a general model validator. The reason to have separate validators
	// is to be more precise which validator to use in certain model components, for example, using a specific
	// validator just for boot version that just notifies the boot version field model, but not any other unrelated
	// model that shouldn't be notified to changes to boot version.
	private final LiveVariable<ValidationResult> modelValidator = new LiveVariable<ValidationResult>();
	private final LiveVariable<ValidationResult> bootVersionValidator = new LiveVariable<ValidationResult>();
	private final LiveVariable<ValidationResult> serviceUrlValidator = new LiveVariable<ValidationResult>();

	// Separate "validator" used only for tracking INFO progress. Should not be used for error handling, as this is used
	// to show progress in the wizard
	private final LiveVariable<ValidationResult> modelLoadingProgress = new LiveVariable<ValidationResult>();

	private final FieldModel<String> bootVersion = new StringFieldModel("Spring Boot Version:", "").validator(bootVersionValidator);
	private final FieldModel<String> serviceUrlField = new StringFieldModel("Service URL:", null).validator(serviceUrlValidator);

	private final LiveVariable<InitializrModel> modelExp = new LiveVariable<InitializrModel>(null);

	private String[] serviceUrlOptions;

	private ValueListener<String> serviceUrlChangeListener;

	public AddStartersWizardModel(IProject project, IPreferenceStore preferenceStore) throws Exception {
		this.project = project;
		this.preferenceStore = preferenceStore;

		String[] urls = BootPreferences.getInitializrUrls();
		this.serviceUrlOptions = urls != null ? urls : new String[0];

		String initializrUrl = BootPreferences.getInitializrUrl();
		serviceUrlField.getVariable().setValue(initializrUrl);
	}

	public FieldModel<String> getBootVersion() {
		return bootVersion;
	}

	public FieldModel<String> getServiceUrl() {
		return serviceUrlField;
	}

	public LiveExpression<InitializrModel> getModel() {
		return modelExp;
	}

	/**
	 *
	 * @return Validator that notifies when errors occur in any of the components of the  model.
	 */
	public LiveExpression<ValidationResult> getValidator() {
		return this.modelValidator;
	}

	/**
	 * Models the "progress bar" in the wizard. Progress messages that need to be
	 * displayed in a wizards progress bar are available through this live expression.
	 */
	public LiveExpression<ValidationResult> getProgress() {
		return this.modelLoadingProgress;
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

	private void handleError(InitializrModel model, Throwable e) {
		StringBuffer detailsBuffer = new StringBuffer();
		String shortMessage = "Error encountered while resolving initializr content for: " + project.getName();

		// This error is specific to boot version validator, so inform the boot version
		// validator
		Throwable deepestCause = ExceptionUtil.getDeepestCause(e);
		ValidationResult addStartersError = AddStartersErrorUtil.getError(shortMessage, deepestCause);

		if (deepestCause instanceof FileNotFoundException) {
			detailsBuffer.append(
					"Initializr content for the project's boot version is not available. Consider updating the project to a newer version of Spring Boot:");

			if (model != null) {
				Option[] availableBootVersions = model.getAvailableBootVersions();
				if (availableBootVersions != null) {
					detailsBuffer.append('\n');
					for (Option option : availableBootVersions) {
						detailsBuffer.append('\n');
						detailsBuffer.append(option.getId());
					}
				}
			}

			addStartersError = AddStartersErrorUtil.getError(shortMessage, detailsBuffer.toString(), e);

			// Notify the boot version validator to mark the boot version field with an error
			this.bootVersionValidator.setValue(addStartersError);

		} else if (deepestCause instanceof UnknownHostException || deepestCause instanceof MalformedURLException
				|| deepestCause.getMessage().equals(MISSING_INITIALIZR_SERVICE_URL)) {
			// Notify the service URL validator to mark the service URL field with an error
			this.serviceUrlValidator.setValue(addStartersError);
		}

		// regardless of which component is associated with the error, set the error in the
		// model validator with tracks all model errors
		this.modelValidator.setValue(addStartersError);
	}

	/**
	 * Starts the project model loading process. The given change listener will be invoked
	 * upon subsequent service URL changes
	 *
	 */
	public void startModelLoading(ValueListener<String> _serviceUrlChangeListener) {

		LiveVariable<String> serviceUrlVariable = serviceUrlField.getVariable();

		if (this.serviceUrlChangeListener != null) {
			serviceUrlVariable.removeListener(this.serviceUrlChangeListener);
		}

		this.serviceUrlChangeListener = _serviceUrlChangeListener;

		// model loading is triggered by the service URL field. Adding a listener
		// will trigger initial loading as that is how Liveexp are designed.
		serviceUrlVariable.addListener(_serviceUrlChangeListener);
	}

	/**
	 * Synchronous model loading. Allows external trigger of model loading.
	 */
	public void loadModel() {
		InitializrModel model = null;

		// Clear the model first
		modelExp.setValue(null);

		// Clear or reset validators
		modelValidator.setValue(null);
		bootVersionValidator.setValue(null);
		serviceUrlValidator.setValue(null);

		Exception error = null;
		try {
			modelLoadingProgress.setValue(ValidationResult.info("Creating Boot project model..."));
			model = createModel();
			modelLoadingProgress.setValue(ValidationResult.info("Fetching starter information from Spring Boot Initializr..."));
			model.downloadDependencies();
		} catch (Exception _e) {
			error = _e;
		}

		// FIRST set the model even if there are errors
		if (model != null) {
			modelExp.setValue(model);
		}

		// Lastly, set the validator as this triggers external participants registered as listeners
		// to react to model being valid (or having errors)
		if (error != null) {
			Throwable e = ExceptionUtil.getDeepestCause(error);
			if (e instanceof HttpRedirectionException) {
				serviceUrlField.getVariable().setValue(((HttpRedirectionException)e).redirectedTo);
			} else {
				handleError(model, e);
			}
		} else {
			modelValidator.setValue(ValidationResult.OK);
		}

		// Set OK regardless of errors as this just indicates that loading process is over
		modelLoadingProgress.setValue(ValidationResult.OK);
	}

	private InitializrModel createModel() throws Exception {

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

			return model;
		} else {
			throw ExceptionUtil.coreException(MISSING_INITIALIZR_SERVICE_URL);
		}
	}


	public void dispose() {
		InitializrModel model = modelExp.getValue();
		if (model != null) {
			model.dispose();
		}
		if (this.serviceUrlChangeListener != null) {
			serviceUrlField.getVariable().removeListener(this.serviceUrlChangeListener);
		}
	}

	public String[] getServiceUrlOptions() {
		return serviceUrlOptions;
	}
}