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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportUtils;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadableItem;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.frameworks.core.util.XmlUtils;
import org.springsource.ide.eclipse.commons.livexp.core.FieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.StringFieldModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectLocationValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.NewProjectNameValidator;
import org.springsource.ide.eclipse.commons.livexp.core.validators.UrlValidator;
import org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.xml.sax.SAXException;

/**
 * A ZipUrlImportWizard is a simple wizard in which one can paste a url
 * pointing to a zip file. The zip file is supposed to contain a maven (or gradle)
 * project in the root of the zip.
 */
public class NewSpringBootWizardModel {

	private static final Map<String,String> KNOWN_GROUP_LABELS = new HashMap<String, String>();
	static {
		KNOWN_GROUP_LABELS.put("type", "Type:");
		KNOWN_GROUP_LABELS.put("packaging", "Packaging:");
		KNOWN_GROUP_LABELS.put("javaVersion", "Java Version:");
		KNOWN_GROUP_LABELS.put("language", "Language:");
		KNOWN_GROUP_LABELS.put("bootVersion", "Boot Version:");
	}

	private static final Map<String,BuildType> KNOWN_TYPES = new HashMap<String, BuildType>();
	static {
		KNOWN_TYPES.put("gradle-project", BuildType.GRADLE); // New version of initialzr app
		KNOWN_TYPES.put("maven-project", BuildType.MAVEN); // New versions of initialzr app

		KNOWN_TYPES.put("gradle.zip", BuildType.GRADLE); //Legacy, can remove when new initializr app uses "gradle-project" definitively
		KNOWN_TYPES.put("starter.zip", BuildType.MAVEN); //Legacy, can remove when initializr app uses "maven-project" definitively
	}

	private final URLConnectionFactory urlConnectionFactory;
	private final String FORM_URL;
	private String DOWNLOAD_URL; //Derived from 'FORM_URL' by downloading and parsing the form and finding the 'action' in the <form> element

	public NewSpringBootWizardModel() throws Exception {
		this(new URLConnectionFactory(), StsProperties.getInstance(new NullProgressMonitor()));
	}

	public NewSpringBootWizardModel(URLConnectionFactory urlConnectionFactory, StsProperties stsProps) throws Exception {
		this(urlConnectionFactory, stsProps.get("spring.initializr.form.url"));
	}

	public NewSpringBootWizardModel(URLConnectionFactory urlConnectionFactory, String formUrl) throws Exception {
		this.urlConnectionFactory = urlConnectionFactory;
		this.FORM_URL = formUrl;
		discoverOptions(stringInputs, style);
		style.sort();

		projectName = stringInputs.getField("name");
		projectName.validator(new NewProjectNameValidator(projectName.getVariable()));
		location = new LiveVariable<String>(ProjectLocationSection.getDefaultProjectLocation(projectName.getValue()));
		locationValidator = new NewProjectLocationValidator("Location", location, projectName.getVariable());
		Assert.isNotNull(projectName, "The form at "+FORM_URL+" doesn't have a 'name' text input");

		baseUrl = new LiveVariable<String>(DOWNLOAD_URL);
		baseUrlValidator = new UrlValidator("Base Url", baseUrl);

		UrlMaker computedUrl = new UrlMaker(baseUrl);
		for (FieldModel<String> param : stringInputs) {
			computedUrl.addField(param);
		}
		computedUrl.addField(style);
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

	public final MultiSelectionFieldModel<String> style = new MultiSelectionFieldModel<String>(String.class, "style")
			.label("Style");

	private final FieldModel<String> projectName; //an alias for stringFields.getField("name");
	private final LiveVariable<String> location;
	private final NewProjectLocationValidator locationValidator;

	private boolean allowUIThread = false;

	public final LiveVariable<String> baseUrl;
	public final LiveExpression<ValidationResult> baseUrlValidator;

	public final LiveVariable<String> downloadUrl = new LiveVariable<String>();
	private IWorkingSet[] workingSets = new IWorkingSet[0];
	private RadioGroups radioGroups = new RadioGroups();

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
	private void discoverOptions(FieldArrayModel<String> fields, MultiSelectionFieldModel<String> style) throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
		URLConnection conn = null;
		InputStream input = null;
		try {
			URL url = new URL(FORM_URL);
			conn = urlConnectionFactory.createConnection(url);
			conn.connect();
			input = conn.getInputStream();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(input);

			NodeList forms = doc.getElementsByTagName("form"); //Should really just be one form.
			Assert.isLegal(forms.getLength()==1, "Unexpected number of <form> elements at: "+FORM_URL);
			for (int i = 0; i < forms.getLength(); i++) {
				Node form = forms.item(i);
				discoverDownloadUrl(form);
			}

			NodeList inputs = doc.getElementsByTagName("input");
			for (int i = 0; i < inputs.getLength(); i++) {
				Node node = inputs.item(i);
				if (isCheckbox(node)) {
					discoverCheckboxOption(style, node);
				} else if (isStringInput(node)) {
					discoverStringField(fields, node);
				} else if (isRadio(node)) {
					disoverRadio(radioGroups, node);
				}
			}
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void disoverRadio(RadioGroups radioGroups, Node node) {
		//<label>Packaging</label>
		//<label class="radio">
		//     <input type="radio" name="packaging" value="jar" checked="true"/>   <--- node is here
		//     Jar
		//</label>

		String groupName = DomUtils.getAttributeValue(node, "name");
		String value = DomUtils.getAttributeValue(node, "value");
		boolean isChecked = DomUtils.getAttributeValue(node, "checked", false);
		RadioInfo radio = new RadioInfo(groupName, value, isChecked);
		RadioGroup group = radioGroups.ensureGroup(groupName);
		String groupLabel = getGroupLabel(groupName);
		if (groupLabel!=null) { //Skip unknown groups.
			group.label(groupLabel);
			if (include(radio)) {
				String label = getInputLabel(node);
				if (label!=null) {
					radio.setLabel(label);
				}
				group.add(radio);
			}
		}

	}

	/**
	 * Defines some special cases of radio buttons on the form that we should ignore.
	 */
	protected boolean include(RadioInfo radio) {
		if ("type".equals(radio.getGroupName())) {
			//We can only import specifically supported build types.
			String value = radio.getValue();
			return KNOWN_TYPES.containsKey(value);
		}
		//By default always include new radios added to the web wizard.
		return true;
	}

	/**
	 * Determine the download url from the submit button on the form.
	 * @param button
	 * @throws URISyntaxException
	 */
	private void discoverDownloadUrl(Node form) throws URISyntaxException {
		//<form id="form" action="/starter.zip" method="get">
		String action = DomUtils.getAttributeValue(form, "action");
		URI formUri = new URI(FORM_URL);
		this.DOWNLOAD_URL = formUri.resolve(action).toString();
	}

	/**
	 * Tries to find a 'label text' associated with a given input node. May return null
	 * if the node is not found in the expected place or if it contains no text.
	 */
	public static String getInputLabel(Node input) {
		Node labelNode = getParentLabel(input);
		if (labelNode==null) {
			labelNode = getSiblingsLabel(input);
		}
		if (labelNode!=null) {
			return getLabelText(labelNode);
		}
		return null;
	}

	private static String getLabelText(Node labelNode) {
		if (labelNode!=null) {
			NodeList children = labelNode.getChildNodes();
			StringBuilder text = new StringBuilder();
			for (int i = 0; i < children.getLength(); i++) {
				Node sibling = children.item(i);
				if (sibling.getNodeType()==Node.TEXT_NODE) {
					text.append(sibling.getNodeValue().trim());
				}
			}
			String result = text.toString();
			while (result.endsWith(":")) {
				result = result.substring(0, result.length()-1);
			}
			if (!"".equals(result)) {
				//only return a String if we actually found some text.
				return result;
			}
		}
		return null;
	}

	/**
	 * We only have labels for 'known' radio groups. Searchin for them in
	 * the html is too hard (and would be terribly fragile).
	 */
	protected String getGroupLabel(String groupName) {
		return KNOWN_GROUP_LABELS.get(groupName);
	}


	/**
	 * Try to find a label node that is the parent of a given input node.
	 */
	public static Node getParentLabel(Node input) {
		Node labelNode = input.getParentNode();
		if (labelNode!=null) {
			if (isLabel(labelNode)) {
				return labelNode;
			}
		}
		return null;
	}


	/**
	 * Try to find a label node that is sibling preceding a given input node.
	 */
	public static Node getSiblingsLabel(Node input) {
		while (input!=null) {
			if (isLabel(input)) {
				return input;
			}
			input = input.getPreviousSibling();
		}
		return null;
	}

	public static boolean isLabel(Node labelNode) {
		String tagName = XmlUtils.getTagName(labelNode);
		return "label".equals(tagName);
	}

	private void discoverCheckboxOption(MultiSelectionFieldModel<String> style, Node checkbox) {
		//We have found a 'checkbox' input node. For example:
		/* <label class="checkbox">
		        <input type="checkbox" name="style" value="jpa"/>
		        JPA
		   </label>
		 */
		String name = DomUtils.getAttributeValue(checkbox, "name");
		if (style.getName().equals(name)) {
			String styleValue = DomUtils.getAttributeValue(checkbox, "value");
			if (styleValue!=null) {
				String styleLabel = getInputLabel(checkbox);
				style.choice(styleLabel==null?styleValue:styleLabel, styleValue);
			}
		}
	}

	private void discoverStringField(FieldArrayModel<String> fields, Node node) {
		//We have found a 'text' input node. For example:
		/* <label for="name">Name:</label> <input id="name" type="text" value="demo" name="name"/>
		 */
		String name = DomUtils.getAttributeValue(node, "name");
		if (name!=null) {
			String defaultValue = DomUtils.getAttributeValue(node, "value");
			StringFieldModel field = new StringFieldModel(name, defaultValue==null?"":defaultValue);
			String label = getInputLabel(node);
			if (label!=null) {
				field.label(label);
			}
			fields.add(field);
		}
	}

	private boolean isCheckbox(Node node) {
		String type = DomUtils.getAttributeValue(node, "type");
		return "checkbox".equals(type);
	}

	private boolean isRadio(Node node) {
		String type = DomUtils.getAttributeValue(node, "type");
		return "radio".equals(type);
	}

	private boolean isStringInput(Node node) {
		String type = DomUtils.getAttributeValue(node, "type");
		return "text".equals(type);
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

}
