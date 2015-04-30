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
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import java.beans.Expression;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.DependencyGroup;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Option;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Type;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectLocationValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectNameValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.UrlValidator;
import org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection;

/**
 * A ZipUrlImportWizard is a simple wizard in which one can paste a url
 * pointing to a zip file. The zip file is supposed to contain a maven (or gradle)
 * project in the root of the zip.
 */
public class NewSpringBootWizardModel {

	private static final String JSON_CONTENT_TYPE_HEADER = "application/vnd.initializr.v2.1+json";
	private static final Map<String,BuildType> KNOWN_TYPES = new HashMap<String, BuildType>();
	static {
		KNOWN_TYPES.put("gradle-project", BuildType.GRADLE); // New version of initialzr app
		KNOWN_TYPES.put("maven-project", BuildType.MAVEN); // New versions of initialzr app

		KNOWN_TYPES.put("gradle.zip", BuildType.GRADLE); //Legacy, can remove when new initializr app uses "gradle-project" definitively
		KNOWN_TYPES.put("starter.zip", BuildType.MAVEN); //Legacy, can remove when initializr app uses "maven-project" definitively
	}

	/**
	 * Lists known query parameters that map onto a String input field. The default values for these
	 * parameters will be pulled from the json spec document.
	 */
	private static final Map<String,String> KNOWN_STRING_INPUTS = new LinkedHashMap<String, String>();
	static {
		KNOWN_STRING_INPUTS.put("name", "Name");
		KNOWN_STRING_INPUTS.put("groupId", "Group");
		KNOWN_STRING_INPUTS.put("artifactId", "Artifact");
		KNOWN_STRING_INPUTS.put("version", "Version");
		KNOWN_STRING_INPUTS.put("description", "Description");
		KNOWN_STRING_INPUTS.put("packageName", "Package");
	};

	private static final Map<String, String> KNOWN_SINGLE_SELECTS = new LinkedHashMap<String, String>();
	static {
		KNOWN_SINGLE_SELECTS.put("packaging", "Packaging:");
		KNOWN_SINGLE_SELECTS.put("javaVersion", "Java Version:");
		KNOWN_SINGLE_SELECTS.put("language", "Language:");
		KNOWN_SINGLE_SELECTS.put("bootVersion", "Boot Version:");
	}

	private final URLConnectionFactory urlConnectionFactory;
	private final String JSON_URL;
	private final String CONTENT_TYPE;

	private final Map<String, LiveExpression<Boolean>> dependencyEnablement = new HashMap<String, LiveExpression<Boolean>>();

	public NewSpringBootWizardModel() throws Exception {
		this(new URLConnectionFactory(), StsProperties.getInstance(new NullProgressMonitor()));
	}

	public NewSpringBootWizardModel(URLConnectionFactory urlConnectionFactory, StsProperties stsProps) throws Exception {
		this(urlConnectionFactory, stsProps.get("spring.initializr.json.url"), JSON_CONTENT_TYPE_HEADER);
	}

	public NewSpringBootWizardModel(URLConnectionFactory urlConnectionFactory, String jsonUrl, String contentType) throws Exception {
		this.urlConnectionFactory = urlConnectionFactory;
		this.JSON_URL = jsonUrl;
		this.CONTENT_TYPE = contentType;

		baseUrl = new LiveVariable<String>("<computed>");
		baseUrlValidator = new UrlValidator("Base Url", baseUrl);

		discoverOptions(stringInputs, dependencies);
		dependencies.sort();

		projectName = stringInputs.getField("name");
		projectName.validator(new NewProjectNameValidator(projectName.getVariable()));
		location = new LiveVariable<String>(ProjectLocationSection.getDefaultProjectLocation(projectName.getValue()));
		locationValidator = new NewProjectLocationValidator("Location", location, projectName.getVariable());
		Assert.isNotNull(projectName, "The service at "+JSON_URL+" doesn't specify a 'name' text input");

		UrlMaker computedUrl = new UrlMaker(baseUrl);
		for (FieldModel<String> param : stringInputs) {
			computedUrl.addField(param);
		}
		computedUrl.addField(dependencies);
		for (RadioGroup group : radioGroups.getGroups()) {
			computedUrl.addField(group);
		}
		computedUrl.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				downloadUrl.setValue(value);
			}
		});

		addBuildTypeValidator();
	}

	/**
	 * If this wizard has a 'type' radioGroup to select the build type then add a validator to check if the
	 * build type is supported.
	 */
	private void addBuildTypeValidator() {
		RadioGroup buildTypeGroup = getRadioGroups().getGroup("type");
		if (buildTypeGroup!=null) {
			buildTypeGroup.validator(new Validator() {
				@Override
				protected ValidationResult compute() {
					BuildType bt = getBuildType();
					if (!bt.getImportStrategy().isSupported()) {
						//This means some required STS component like m2e or gradle tooling is not installed
						return ValidationResult.error(bt.getNotInstalledMessage());
					}
					return ValidationResult.OK;
				}
			}.dependsOn(buildTypeGroup.getVariable()));
		}
	}

	@SuppressWarnings("unchecked")
	public final FieldArrayModel<String> stringInputs = new FieldArrayModel<String>(
			//The fields need to be discovered by parsing web form.
	);

	public final HierarchicalMultiSelectionFieldModel<Dependency> dependencies = new HierarchicalMultiSelectionFieldModel<Dependency>(Dependency.class, "dependencies")
			.label("Dependencies:");

	private final FieldModel<String> projectName; //an alias for stringFields.getField("name");
	private final LiveVariable<String> location;
	private final NewProjectLocationValidator locationValidator;

	private boolean allowUIThread = false;

	public final LiveVariable<String> baseUrl;
	public final LiveExpression<ValidationResult> baseUrlValidator;

	public final LiveVariable<String> downloadUrl = new LiveVariable<String>();
	private IWorkingSet[] workingSets = new IWorkingSet[0];
	private RadioGroups radioGroups = new RadioGroups();
	private RadioGroup bootVersion;

	public void performFinish(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
		mon.beginTask("Importing "+baseUrl.getValue(), 4);
		DownloadManager downloader = null;
		try {
			downloader = new DownloadManager().allowUIThread(allowUIThread);

			DownloadableItem zip = new DownloadableItem(newURL(downloadUrl .getValue()), downloader);
			String projectNameValue = projectName.getValue();
			CodeSet cs = CodeSet.fromZip(projectNameValue, zip, new Path("/"));

			IRunnableWithProgress oper = getImportStrategy().createOperation(ImportUtils.importConfig(
					new Path(location.getValue()),
					projectNameValue,
					cs
			));
			oper.run(new SubProgressMonitor(mon, 3));

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameValue);
			addToWorkingSets(project, new SubProgressMonitor(mon, 1));

		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (downloader!=null) {
				downloader.dispose();
			}
			mon.done();
		}
	}

	/**
	 * Get currently selected import strategy.
	 * Never returns null (some default is returned in any case).
	 */
	public ImportStrategy getImportStrategy() {
		return getBuildType().getImportStrategy();
	}

	/**
	 * Gets the currently selected BuildType.
	 * Never returns null (some default is returned in any case).
	 */
	public BuildType getBuildType() {
		RadioGroup buildTypeRadios = getRadioGroups().getGroup("type");
		if (buildTypeRadios!=null) {
			RadioInfo selected = buildTypeRadios.getSelection().selection.getValue();
			if (selected!=null) {
				BuildType bt = KNOWN_TYPES.get(selected.getValue());
				if (bt!=null) {
					return bt;
				} else {
					//Uknown build type, import it as a general project which is better than nothing
					return BuildType.GENERAL;
				}
			}
		}
		//Old initialzr app doesn't have button to specify build type... it is always maven
		return BuildType.MAVEN;
	}

	private void addToWorkingSets(IProject project, IProgressMonitor monitor) {
		monitor.beginTask("Add '"+project.getName()+"' to working sets", 1);
		try {
			if (workingSets==null || workingSets.length==0) {
				return;
			}
			IWorkingSetManager wsm = PlatformUI.getWorkbench().getWorkingSetManager();
			wsm.addToWorkingSets(project, workingSets);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Dynamically discover input fields and 'style' options by parsing initializr form.
	 */
	private void discoverOptions(FieldArrayModel<String> fields, HierarchicalMultiSelectionFieldModel<Dependency> dependencies) throws Exception {
		InitializrServiceSpec serviceSpec = parseJsonFrom(new URL(JSON_URL));

		Map<String, String> textInputs = serviceSpec.getTextInputs();
		for (Entry<String, String> e : KNOWN_STRING_INPUTS.entrySet()) {
			String name = e.getKey();
			String defaultValue = textInputs.get(name);
			if (defaultValue!=null) {
				fields.add(new StringFieldModel(name, defaultValue).label(e.getValue()));
			}
		}

		{	//field: type
			String groupName = "type";
			RadioGroup group = radioGroups.ensureGroup(groupName);
			group.label("Type:");
			for (Type type : serviceSpec.getTypeOptions(groupName)) {
				if (KNOWN_TYPES.containsKey(type.getId())) {
					TypeRadioInfo radio = new TypeRadioInfo(groupName, type.getId(), type.isDefault(), type.getAction());
					radio.setLabel(type.getName());
					group.add(radio);
				}
			}
			//When a type is selected the 'baseUrl' should be update according to its action.
			group.getSelection().selection.addListener(new ValueListener<RadioInfo>() {
				public void gotValue(LiveExpression<RadioInfo> exp, RadioInfo value) {
					try {
						if (value!=null) {
							URI base = new URI(JSON_URL);
							URI resolved = base.resolve(((TypeRadioInfo)value).getAction());
							baseUrl.setValue(resolved.toString());
						}
					} catch (Exception e) {
						WizardPlugin.log(e);
					}
				}
			});
		}

		for (Entry<String, String> e : KNOWN_SINGLE_SELECTS.entrySet()) {
			String groupName = e.getKey();
			RadioGroup group = radioGroups.ensureGroup(groupName);
			group.label(e.getValue());
			addOptions(group, serviceSpec.getSingleSelectOptions(groupName));
			if (groupName.equals("bootVersion")) {
				this.bootVersion = group;
			}
		}

		//styles
		for (DependencyGroup dgroup : serviceSpec.getDependencies()) {
			String catName = dgroup.getName();
			for (Dependency dep : dgroup.getContent()) {
				dependencies.choice(catName, dep.getName(), dep, dep.getDescription(), createEnablementExp(bootVersion, dep.getVersionRange()));
			}
		}
	}

	private LiveExpression<Boolean> createEnablementExp(final RadioGroup bootVersion, String versionRange) {
		try {
			if (StringUtils.hasText(versionRange)) {
				final VersionRange range = new VersionRange(versionRange);
				return new LiveExpression<Boolean>() {
					{ dependsOn(bootVersion.getSelection().selection); }
					@Override
					protected Boolean compute() {
						try {
							String versionStr = bootVersion.getSelection().selection.getValue().getValue();
							if (versionStr!=null) {
								Version version = new Version(versionStr);
								return range.includes(version);
							}
						} catch (Exception e) {
							WizardPlugin.log(e);
						}
						return true;
					}
				};
			}
		} catch (Exception e) {
			WizardPlugin.log(e);
		}
		return LiveExpression.TRUE;
	}

	private void addOptions(RadioGroup group, Option[] options) {
		for (Option option : options) {
			RadioInfo radio = new RadioInfo(group.getName(), option.getId(), option.isDefault());
			radio.setLabel(option.getName());
			group.add(radio);
		}
	}

	private InitializrServiceSpec parseJsonFrom(URL url) throws Exception {
		URLConnection conn = null;
		InputStream input = null;
		try {
			conn = urlConnectionFactory.createConnection(url);
			conn.addRequestProperty("User-Agent", "STS "+WizardPlugin.getDefault().getBundle().getVersion());
			if (CONTENT_TYPE!=null) {
				conn.addRequestProperty("Accept", CONTENT_TYPE);
			}
			conn.connect();
			input = conn.getInputStream();
			return InitializrServiceSpec.parseFrom(input);
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private URL newURL(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			//This should be impossible because the URL syntax is validated beforehand.
			WizardPlugin.log(e);
			return null;
		}
	}

	/**
	 * This is mostly for testing purposes where it is just easier to run stuff in the UIThread (test do so
	 * by default). But in production we shouldn't allow downloading stuff in the UIThread.
	 */
	public void allowUIThread(boolean allow) {
		this.allowUIThread = allow;
	}

	public LiveExpression<ValidationResult> getLocationValidator() {
		return locationValidator;
	}

	public LiveVariable<String> getLocation() {
		return location;
	}

	public FieldModel<String> getProjectName() {
		return projectName;
	}

	public void setWorkingSets(IWorkingSet[] workingSets) {
		this.workingSets = workingSets;
	}

	public RadioGroups getRadioGroups() {
		return this.radioGroups;
	}

	public RadioGroup getBootVersion() {
		return bootVersion;
	}

}
