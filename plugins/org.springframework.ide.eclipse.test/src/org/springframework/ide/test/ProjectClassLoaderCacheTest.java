/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.ide.eclipse.core.java.ProjectClassLoaderCache;

/**
 * @author Martin Lippert
 */
public class ProjectClassLoaderCacheTest {

	@Test
	public void testFilter() {
		assertFalse(ProjectClassLoaderCache.shouldFilter(null));
		assertFalse(ProjectClassLoaderCache.shouldFilter("resource"));
		assertFalse(ProjectClassLoaderCache.shouldFilter("META-INF"));
		assertFalse(ProjectClassLoaderCache.shouldFilter("META-INF/services/javax/"));
		assertFalse(ProjectClassLoaderCache.shouldFilter("META-INF/services/org/test"));
		assertFalse(ProjectClassLoaderCache.shouldFilter("META-INF/service/tralaal"));

		assertTrue(ProjectClassLoaderCache.shouldFilter("commons-logging.properties"));
		assertTrue(ProjectClassLoaderCache.shouldFilter("META-INF/services/javax"));
	}

}
