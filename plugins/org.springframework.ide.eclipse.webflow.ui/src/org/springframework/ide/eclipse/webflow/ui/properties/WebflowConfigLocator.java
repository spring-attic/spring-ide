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
package org.springframework.ide.eclipse.webflow.ui.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;
import org.springframework.ide.eclipse.beans.core.model.locate.ProjectScanningBeansConfigLocator;

/**
 * Basic {@link IBeansConfigLocator} that is capable for scanning an
 * {@link IProject} or {@link IJavaProject} for Spring Web Flow XML
 * configuration files.
 * <p>
 * Only those XML files that have the Spring Web Flow namespace uri at the root
 * element level are being considered to be a suitable candidate.
 * 
 * @author Leo Dos Santos
 * @since 2.6.0
 */
public class WebflowConfigLocator extends ProjectScanningBeansConfigLocator {

	private static final String WEBFLOW_URI = "http://www.springframework.org/schema/webflow"; //$NON-NLS-1$

	public WebflowConfigLocator(String configuredFileSuffixes) {
		super(configuredFileSuffixes);
	}

	@Override
	protected boolean applyNamespaceFilter(IFile file, String namespaceUri) {
		return (namespaceUri != null && WEBFLOW_URI.equals(namespaceUri));
	}

}
