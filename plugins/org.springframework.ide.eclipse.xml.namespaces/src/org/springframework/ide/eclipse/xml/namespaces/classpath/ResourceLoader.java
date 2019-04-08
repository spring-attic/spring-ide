/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.classpath;

import java.io.InputStream;
import java.net.URL;
import java.util.stream.Stream;

/**
 * Subset of {@link ClassLoader} interface. Mimicks behavior of
 * classloaders but only can be used to load 'resources' as data.
 * 
 * @author Kris De Volder
 */
public interface ResourceLoader {

	ResourceLoader NULL = new ResourceLoader() {
		@Override
		public URL getResource(String resourceId) {
			return null;
		}

		@Override
		public Stream<URL> getResources(String resourceName) {
			return Stream.empty();
		}

		@Override
		public InputStream getResourceAsStream(String icon) {
			return null;
		}
		@Override
		public String toString() {
			return "ResourceLoader.NULL";
		}
	};

	URL getResource(String resourceId);
	Stream<URL> getResources(String resourceName);
	InputStream getResourceAsStream(String icon);
}
