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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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


public class RuntimeTemplateProjectData implements ITemplateProjectData {

	private final IProject project;

	private Descriptor descriptor;

	private File zippedProject;

	private File jsonDescriptor;

	public RuntimeTemplateProjectData(IProject project) {
		this.project = project;
		initialize();
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}

	private void initialize() {
		IFile descriptorFile = project.getFile(IContentConstants.TEMPLATE_DATA_FILE_NAME);
		try {
			DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();

			try {
				Document document = documentBuilder.parse(descriptorFile.getContents());
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
						else if (TAG_JSON.equals(childNode.getNodeName())) {
							String filePath = ContentUtil.getAttributeValue(childNode, ATTRIBUTE_PATH);
							if (filePath == null) {
								throw new SAXException("The json descriptor is invalid");
							}
							jsonDescriptor = new File(project.getLocation().toOSString(), filePath);
						}
					}
				}
				if (descriptor != null) {
					zippedProject = createZippedProject(project.getName());
				}

			}
			catch (SAXException e) {
				throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
						"Could not read initialization data for template \"" + project.getName() + "\"", e));
			}
			catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
						"Could not read initialization data for template \"" + project.getName() + "\"", e));
			}

		}
		catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File getJsonDescriptor() {
		return jsonDescriptor;
	}

	public File getZippedProject() {
		return zippedProject;
	}

	private File createZippedProject(String name) throws IOException, CoreException {
		File destFile = File.createTempFile(name, ".zip");
		FileOutputStream dest = new FileOutputStream(destFile);
		ZipOutputStream out = null;

		try {
			out = new ZipOutputStream(new BufferedOutputStream(dest));

			project.accept(new RuntimeTemplateProjectZipCreatorVisitor(out, null), IResource.DEPTH_INFINITE, false);
		}
		finally {
			if (out != null) {
				out.close();
			}
		}

		return destFile;
	}
}
