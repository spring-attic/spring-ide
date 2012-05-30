/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.ContentUtil;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Terry Denney
 */
public class TemplateProjectData implements ITemplateProjectData {

	private final File path;

	private final String id;

	private Descriptor descriptor;

	private File zippedProject;

	private File jsonDescriptor;

	public TemplateProjectData(File path) {
		this.path = path;
		this.id = path.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.wizard.template.infrastructure.ITemplateProjectData
	 * #getDescriptor()
	 */
	public Descriptor getDescriptor() {
		return descriptor;
	}

	private File getFile() {
		return new File(path, IContentConstants.TEMPLATE_DATA_FILE_NAME);
	}

	private File getFileFromPath(Node node) throws SAXException {
		String filePath = ContentUtil.getAttributeValue(node, ATTRIBUTE_PATH);
		if (filePath == null) {
			throw new SAXException("The proejct record is invalid");
		}

		return new File(path.getAbsolutePath() + File.separator + filePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.wizard.template.infrastructure.ITemplateProjectData
	 * #getJsonDescriptor()
	 */
	public File getJsonDescriptor() {
		return jsonDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.wizard.template.infrastructure.ITemplateProjectData
	 * #getTemplateDirectory()
	 */
	public File getTemplateDirectory() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.wizard.template.infrastructure.ITemplateProjectData
	 * #getZippedProject()
	 */
	public File getZippedProject() {
		return zippedProject;
	}

	public void read() throws CoreException {
		File file = getFile();
		DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();
		Document document = null;

		try {
			document = documentBuilder.parse(file);
			Element rootNode = document.getDocumentElement();
			if (rootNode == null) {
				throw new SAXException("No root node");
			}
			if (!TAG_TEMPLATE.equals(rootNode.getNodeName())) {
				throw new SAXException("Not a valid template");
			}
			NodeList children = rootNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node childNode = children.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (TAG_DESCRIPTOR.equals(childNode.getNodeName())) {
						descriptor = Descriptor.read(childNode);
					}
					else if (TAG_PROJECT.equals(childNode.getNodeName())) {
						zippedProject = getFileFromPath(childNode);
					}
					else if (TAG_JSON.equals(childNode.getNodeName())) {
						jsonDescriptor = getFileFromPath(childNode);
					}
				}
			}
		}
		catch (SAXException e) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Could not read initialization data for template \"" + id + "\"", e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
					"Could not read initialization data for template \"" + id + "\"", e));
		}
	}

}
