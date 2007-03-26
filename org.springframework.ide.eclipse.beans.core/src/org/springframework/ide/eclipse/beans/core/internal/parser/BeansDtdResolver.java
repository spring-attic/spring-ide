/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.parser;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * EntityResolver implementation for the Spring beans DTD,
 * to load the DTD from the Spring class path (or JAR file).
 *
 * <p>Fetches "spring-beans-2.0.dtd" from the class path resource
 * "/org/springframework/beans/factory/xml/spring-beans-2.0.dtd",
 * no matter whether specified as some local URL that includes "spring-beans"
 * in the DTD name or as
 * "http://www.springframework.org/dtd/spring-beans-2.0.dtd".
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Torsten Juergeleit
 */
public class BeansDtdResolver implements EntityResolver {

	private static final String DTD_EXTENSION = ".dtd";
	private static final String[] DTD_NAMES = { "spring-beans-2.0",
			"spring-beans" };
	private static final String SEARCH_PACKAGE =
			"/org/springframework/beans/factory/xml/";

	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException {
		if (systemId != null && systemId.endsWith(DTD_EXTENSION)) {
			int lastPathSeparator = systemId.lastIndexOf("/");
			for (String dtdName : DTD_NAMES) {
				int dtdNameStart = systemId.indexOf(dtdName);
				if (dtdNameStart > lastPathSeparator) {
					String dtdFile = systemId.substring(dtdNameStart);
					try {
						Resource resource = new ClassPathResource(
								SEARCH_PACKAGE + dtdFile, getClass());
						InputSource source = new InputSource(resource
								.getInputStream());
						source.setPublicId(publicId);
						source.setSystemId(systemId);
						return source;
					} catch (IOException e) {
						BeansCorePlugin.log("Could not resolve beans DTD ["
								+ systemId + "]: not found in class path", e);
					}
				}
			}
		}

		// Use the default behavior -> download from website or wherever.
		return null;
	}
}
