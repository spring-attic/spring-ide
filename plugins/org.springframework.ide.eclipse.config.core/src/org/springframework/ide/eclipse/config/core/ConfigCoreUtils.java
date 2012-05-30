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
package org.springframework.ide.eclipse.config.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.internal.text.html.HTML2TextReader;
import org.eclipse.wst.xml.core.internal.contentmodel.util.NamespaceTable;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.config.core.contentassist.Messages;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Static utilities of use throughout the configuration editor.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 */
@SuppressWarnings("restriction")
public class ConfigCoreUtils {

	public static String ATTR_DEFAULT_NAMESPACE = "xmlns"; //$NON-NLS-1$

	public static String ATTR_NAMESPACE_PREFIX = "xmlns:"; //$NON-NLS-1$

	public static String ATTR_SCHEMA_LOCATION = "xsi:schemaLocation"; //$NON-NLS-1$

	private static final Pattern VERSION_PATTERN = Pattern.compile(".*-([0-9,.]*)\\.xsd"); //$NON-NLS-1$

	/**
	 * Returns the default namespace URI by searching the document for the
	 * namespace declaration, or null if none found.
	 * 
	 * @param doc document object model of the XML source file
	 * @return default namespace URI of the given document
	 */
	public static String getDefaultNamespaceUri(IDOMDocument doc) {
		if (doc != null) {
			Element root = doc.getDocumentElement();
			if (root != null) {
				NamedNodeMap attrs = root.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					Node item = attrs.item(i);
					String itemName = item.getLocalName();
					if (itemName.equals(ATTR_DEFAULT_NAMESPACE)) {
						return item.getNodeValue();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the namespace prefix for the given URI.
	 * 
	 * @param doc document object model of the XML source file
	 * @param namespaceUri namespace URI to examine
	 * @return namespace prefix for the given URI
	 */
	public static String getPrefixForNamespaceUri(IDOMDocument doc, String namespaceUri) {
		if (doc != null && namespaceUri != null) {
			NamespaceTable table = new NamespaceTable(doc);
			Element elem = doc.getDocumentElement();
			table.addElementLineage(elem);
			return table.getPrefixForURI(namespaceUri);
		}
		return null;
	}

	/**
	 * Returns the selected schema version for the given URI.
	 * 
	 * @param doc document object model of the XML source file
	 * @param namespaceUri namespace URI to examine
	 * @return selected schema version for the given URI
	 */
	public static Version getSchemaVersion(IDOMDocument doc, String namespaceUri) {
		String versLocation = getSelectedSchemaLocation(doc, namespaceUri);
		Matcher matcher = VERSION_PATTERN.matcher(versLocation);
		if (matcher.matches()) {
			return new Version(matcher.group(1));
		}
		else {
			List<INamespaceDefinition> defs = NamespaceUtils.getNamespaceDefinitions();
			for (INamespaceDefinition def : defs) {
				Version version = Version.emptyVersion;
				if (namespaceUri.equals(def.getNamespaceURI())) {
					Version tempVersion = Version.emptyVersion;
					for (String location : def.getSchemaLocations()) {
						matcher = VERSION_PATTERN.matcher(location);
						if (matcher.matches()) {
							tempVersion = new Version(matcher.group(1));
						}
						if (tempVersion.compareTo(version) >= 0) {
							version = tempVersion;
						}
					}
					return version;
				}
			}
			return Version.emptyVersion;
		}
	}

	/**
	 * Returns the selected schema version location for the given URI.
	 * 
	 * @param doc document object model of the XML source file
	 * @param namespaceUri namespace URI to examine
	 * @return selected schema version location for the given URI
	 */
	public static String getSelectedSchemaLocation(IDOMDocument doc, String namespaceUri) {
		List<String> schemaInfo = ConfigCoreUtils.parseSchemaLocationAttr(doc);
		if (schemaInfo != null) {
			Iterator<String> iter = schemaInfo.iterator();
			while (iter.hasNext()) {
				String currSchema = iter.next();
				if (currSchema.equals(namespaceUri) && iter.hasNext()) {
					// String after the schema is the version
					return iter.next();
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Parses the XML document for schema information and returns it as an
	 * array, paired by namespace URI and schema version.
	 * 
	 * @param doc document object model of the XML source file
	 * @return array of paired namespace URIs and schema versions for the given
	 * document
	 */
	public static List<String> parseSchemaLocationAttr(IDOMDocument doc) {
		if (doc != null) {
			Element root = doc.getDocumentElement();
			if (root != null) {
				String schemaLocationValue = root.getAttribute(ATTR_SCHEMA_LOCATION);
				if (schemaLocationValue != null) {
					// Remove all line breaks and tabs
					schemaLocationValue = schemaLocationValue.replaceAll("\\n|\\t|\\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
					// Remove any extra spaces
					schemaLocationValue = schemaLocationValue.replaceAll(" +", " "); //$NON-NLS-1$ //$NON-NLS-2$
					// Trim any remaining whitespace on the ends
					schemaLocationValue = schemaLocationValue.trim();
					// Split along spaces into just the schema content
					return Arrays.asList(schemaLocationValue.split(" ")); //$NON-NLS-1$
				}
			}
		}
		return null;
	}

	/**
	 * Strips text content of all HTML tags and formatting information.
	 * 
	 * @param html text content containing HTML tags
	 * @return the given content stripped of HTML tags
	 */
	public static String stripTags(String html) {
		if (html != null) {
			try {
				StringReader reader = new StringReader(html);
				HTML2TextReader parser = new HTML2TextReader(reader, null);
				return parser.getString().trim();
			}
			catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigCorePlugin.PLUGIN_ID,
						Messages.XmlBackedContentProposalProvider_ERROR_STRIP_TAGS, e));
			}
		}
		return html;
	}

}
