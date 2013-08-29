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
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportUtils;
import org.springframework.ide.eclipse.wizard.gettingstarted.util.DownloadManager;
import org.springframework.ide.eclipse.wizard.gettingstarted.util.DownloadableItem;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.core.util.XmlUtils;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A ZipUrlImportWizard is a simple wizard in which one can paste a url
 * pointing to a zip file. The zip file is supposed to contain a maven (or gradle)
 * project in the root of the zip.
 */
public class NewSpringBootWizardModel {

	private final URLConnectionFactory urlConnectionFactory;
	private final String FORM_URL;
	private final String DOWNLOAD_URL;

	public NewSpringBootWizardModel() throws IOException, ParserConfigurationException, SAXException {
		this(new URLConnectionFactory(), StsProperties.getInstance(new NullProgressMonitor()));
	}

	public NewSpringBootWizardModel(URLConnectionFactory urlConnectionFactory, StsProperties stsProps) throws IOException, ParserConfigurationException, SAXException {
		this.urlConnectionFactory = urlConnectionFactory;
		this.FORM_URL = stsProps.get("spring.initializr.form.url");
		this.DOWNLOAD_URL = stsProps.get("spring.initializr.download.url");
		discoverOptions(stringInputs, style);

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
		computedUrl.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				downloadUrl.setValue(value);
			}
		});
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

	public void performFinish(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
		mon.beginTask("Importing "+baseUrl, 1);
		DownloadManager downloader = null;
		try {
			downloader = new DownloadManager().allowUIThread(allowUIThread);

			DownloadableItem zip = new DownloadableItem(newURL(downloadUrl .getValue()), downloader);
			CodeSet cs = CodeSet.fromZip(projectName.getValue(), zip, new Path("/"));

			IRunnableWithProgress oper = BuildType.MAVEN.getImportStrategy().createOperation(ImportUtils.importConfig(
					new Path(location.getValue()),
					projectName.getValue(),
					cs
			));
			oper.run(new SubProgressMonitor(mon, 1));

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
	 * Dynamically discover input fields and 'style' options by parsing initializr form.
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private void discoverOptions(FieldArrayModel<String> fields, MultiSelectionFieldModel<String> style) throws IOException, ParserConfigurationException, SAXException {
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

			NodeList inputs = doc.getElementsByTagName("input");
			for (int i = 0; i < inputs.getLength(); i++) {
				Node node = inputs.item(i);
				if (isCheckbox(node)) {
					discoverCheckboxOption(style, node);
				} else if (isStringInput(node)) {
					discoverStringField(fields, node);
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

	/**
	 * Tries to find a 'label text' associated with a given input node. May return null
	 * if the node is not found in the expected place or if it contains no text.
	 */
	private String getInputLabel(Node input) {
		Node labelNode = getParentLabel(input);
		if (labelNode==null) {
			labelNode = getSiblingsLabel(input);
		}
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
	 * Try to find a label node that is the parent of a given input node.
	 */
	private Node getParentLabel(Node input) {
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
	private Node getSiblingsLabel(Node input) {
		while (input!=null) {
			if (isLabel(input)) {
				return input;
			}
			input = input.getPreviousSibling();
		}
		return null;
	}

	private boolean isLabel(Node labelNode) {
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
		String name = getAttributeValue(checkbox, "name");
		if (style.getName().equals(name)) {
			String styleValue = getAttributeValue(checkbox, "value");
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
		String name = getAttributeValue(node, "name");
		if (name!=null) {
			String defaultValue = getAttributeValue(node, "value");
			StringFieldModel field = new StringFieldModel(name, defaultValue==null?"":defaultValue);
			String label = getInputLabel(node);
			if (label!=null) {
				field.label(label);
			}
			fields.add(field);
		}
	}

	private boolean isCheckbox(Node node) {
		String type = getAttributeValue(node, "type");
		return "checkbox".equals(type);
	}

	private boolean isStringInput(Node node) {
		String type = getAttributeValue(node, "type");
		return "text".equals(type);
	}

	private static String getAttributeValue(Node node, String attribName ) {
		NamedNodeMap attribs = node.getAttributes();
		if (attribs!=null) {
			Node value = attribs.getNamedItem(attribName);
			if (value!=null) {
				short nodeType = value.getNodeType();
				if (nodeType==Node.ATTRIBUTE_NODE) {
					return value.getNodeValue();
				}
			}
		}
		return null;
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

}
