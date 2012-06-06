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
package org.springframework.ide.eclipse.roo.core.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.internal.model.DefaultRooInstall;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class RooInstallManager {

	private final Map<String, IRooInstall> installs = new ConcurrentHashMap<String, IRooInstall>();

	private final List<IRooInstallListener> listeners = new ArrayList<IRooInstallListener>();

	public void addRooInstallListener(IRooInstallListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public String[] getAllInstallNames() {
		Set<String> newInstalls = new HashSet<String>();
		for (IRooInstall install : installs.values()) {
			newInstalls.add(install.getName());
		}
		return newInstalls.toArray(new String[newInstalls.size()]);
	}

	public Collection<IRooInstall> getAllInstalls() {
		Set<IRooInstall> newInstalls = new HashSet<IRooInstall>();
		for (IRooInstall install : installs.values()) {
			newInstalls.add(new DefaultRooInstall(install.getHome(), install.getName(), install.isDefault()));
		}
		return newInstalls;
	}

	public IRooInstall getDefaultRooInstall() {
		for (IRooInstall install : installs.values()) {
			if (install.isDefault()) {
				return install;
			}
		}
		if (installs.size() > 0) {
			return installs.values().iterator().next();
		}
		return null;
	}

	public IRooInstall getRooInstall(IProject project) {
		if (project == null) {
			return null;
		}
		if (SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.PLUGIN_ID).getBoolean(
				RooCoreActivator.PROJECT_PROPERTY_ID, true)) {
			return getDefaultRooInstall();
		}
		else if (SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.PLUGIN_ID).getString(
				RooCoreActivator.ROO_INSTALL_PROPERTY, null) != null) {
			return getRooInstall(SpringCorePreferences.getProjectPreferences(project, RooCoreActivator.PLUGIN_ID)
					.getString(RooCoreActivator.ROO_INSTALL_PROPERTY, null));
		}
		return null;
	}

	public IRooInstall getRooInstall(String name) {
		return installs.get(name);
	}

	public void removeRooInstallListener(IRooInstallListener listener) {
		listeners.remove(listener);
	}

	public void setRooInstalls(Set<IRooInstall> newInstalls) {
		installs.clear();

		try {

			DocumentBuilder documentBuilder = SpringCoreUtils.getDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("installs");
			document.appendChild(root);

			for (IRooInstall install : newInstalls) {
				if (install.validate().isOK()) {
					installs.put(install.getName(), install);

					Element installNode = document.createElement("install");
					root.appendChild(installNode);

					// add is-default attribute
					Attr isDefaultAttribute = document.createAttribute("is-default");
					installNode.setAttributeNode(isDefaultAttribute);
					isDefaultAttribute.setValue(Boolean.toString(install.isDefault()));

					// add home element
					Element homeNode = document.createElement("home");
					installNode.appendChild(homeNode);
					homeNode.appendChild(document.createTextNode(install.getHome()));

					Element nameNode = document.createElement("name");
					installNode.appendChild(nameNode);
					nameNode.appendChild(document.createTextNode(install.getName()));
				}
			}

			IPath rooInstallFile = RooCoreActivator.getDefault().getStateLocation().append("roo.installs");

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");

			Writer out = new OutputStreamWriter(new FileOutputStream(rooInstallFile.toFile()), "ISO-8859-1");
			StreamResult result = new StreamResult(out);
			DOMSource source = new DOMSource(document);
			transformer.transform(source, result);
			out.close();

		}
		catch (IOException e) {
			RooCoreActivator.log(e);
		}
		catch (TransformerException e) {
			RooCoreActivator.log(e);
		}

		for (IRooInstallListener listener : listeners) {
			listener.installChanged(newInstalls);
		}

	}

	public void start() {
		try {
			boolean readFromLegacyLocation = false;
			DocumentBuilder docBuilder = SpringCoreUtils.getDocumentBuilder();
			IPath rooInstallFile = RooCoreActivator.getDefault().getStateLocation().append("roo.installs");
			if (!rooInstallFile.toFile().exists()) {
				// Look for legacy install file at <workspace>/.metadata/.plugins/com.springsource.sts.roo.core/roo.installs
				rooInstallFile = rooInstallFile.removeLastSegments(2).append("com.springsource.sts.roo.core/roo.installs");
				readFromLegacyLocation = true;
			}
			if (rooInstallFile.toFile().exists()) {
				Document doc = docBuilder.parse(rooInstallFile.toFile());
				NodeList installNodes = doc.getElementsByTagName("install");
				for (int i = 0; i < installNodes.getLength(); i++) {
					Node installNode = installNodes.item(i);
					String name = null;
					String home = null;
					boolean isDefault = false;

					NodeList installChildren = installNode.getChildNodes();
					for (int j = 0; j < installChildren.getLength(); j++) {
						Node installChild = installChildren.item(j);
						if ("name".equals(installChild.getNodeName())) {
							name = installChild.getTextContent();
						}
						else if ("home".equals(installChild.getNodeName())) {
							home = installChild.getTextContent();
						}
					}

					Node defaultNode = installNode.getAttributes().getNamedItem("is-default");
					if (defaultNode != null && defaultNode.getNodeValue().equalsIgnoreCase("true")) {
						isDefault = true;
					}

					if (name != null && home != null) {
						DefaultRooInstall install = new DefaultRooInstall(home, name, isDefault);
						installs.put(name, install);
					}
					else {
						RooCoreActivator.log("Discarding Roo install [" + home + "] with name [" + name + "]", null);
					}
					if (readFromLegacyLocation) {
						save(doc);
					}
				}
			}
		}
		catch (SAXException e) {
			RooCoreActivator.log(e);
		}
		catch (IOException e) {
			RooCoreActivator.log(e);
		}
	}

	private void save(Document document) {
		try {
			IPath grailsInstallFile = RooCoreActivator.getDefault().getStateLocation().append("roo.installs");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");

			Writer out = new OutputStreamWriter(new FileOutputStream(grailsInstallFile.toFile()), "ISO-8859-1");
			StreamResult result = new StreamResult(out);
			DOMSource source = new DOMSource(document);
			transformer.transform(source, result);
			out.close();
		} catch (IOException e) {
			RooCoreActivator.log(e);
		}
		catch (TransformerException e) {
			RooCoreActivator.log(e);
		}
	}
	
}
