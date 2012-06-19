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
package org.springframework.ide.eclipse.config.ui.contenttype;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.internal.content.ContentMessages;
import org.eclipse.core.internal.content.XMLRootHandler;
import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.ui.namespaces.NamespaceUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An {@link org.eclipse.core.runtime.content.XMLContentDescriber} extension
 * that reads the root element and checks if the namespace is configured with
 * Spring IDE's namespace definition infrastructure.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @since 2.3.0
 */
@SuppressWarnings("restriction")
public final class SpringElementContentDescriber extends XMLContentDescriber {

	private int checkCriteria(InputSource contents) throws IOException {
		XMLRootHandler xmlHandler = new XMLRootHandler(true);
		try {
			if (!xmlHandler.parseContents(contents)) {
				return INDETERMINATE;
			}
		}
		catch (SAXException e) {
			// we may be handed any kind of contents... it is normal we fail to
			// parse
			return INDETERMINATE;
		}
		catch (ParserConfigurationException e) {
			// some bad thing happened - force this describer to be disabled
			String message = ContentMessages.content_parserConfiguration;
			RuntimeLog.log(new Status(IStatus.ERROR, ContentMessages.OWNER_NAME, 0, message, e));
			throw new RuntimeException(message);
		}

		// Check to see if we matched our criteria.
		String ns = xmlHandler.getRootNamespace();
		if (ns.startsWith(org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils.DEFAULT_NAMESPACE_URI)) {
			return VALID;
		}

		if (ns != null) {
			for (INamespaceDefinition namespaceDefinition : NamespaceUtils.getNamespaceDefinitions()) {
				if (ns.equals(namespaceDefinition.getNamespaceURI())) {
					return VALID;
				}
			}
		}
		return INDETERMINATE;
	}

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		// super.describe will have consumed some chars, need to rewind
		contents.reset();
		// Check to see if we matched our criteria.
		return checkCriteria(new InputSource(contents));
	}

	@Override
	public int describe(Reader contents, IContentDescription description) throws IOException {
		// call the basic XML describer to do basic recognition
		if (super.describe(contents, description) == INVALID) {
			return INVALID;
		}
		// super.describe will have consumed some chars, need to rewind
		contents.reset();
		// Check to see if we matched our criteria.
		return checkCriteria(new InputSource(contents));
	}

}
