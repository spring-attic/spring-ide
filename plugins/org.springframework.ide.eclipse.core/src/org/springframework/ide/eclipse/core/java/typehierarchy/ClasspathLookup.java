/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (https://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (https://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.io.InputStream;

/**
 * @author Martin Lippert
 */
public interface ClasspathLookup {

	InputStream getStream(String fullyQualifiedClassFileName, String packageName, String className);
	void close();

}
