/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.editor.support.reconcile.SeverityProvider;

/**
 * Provides a method to find context information for IDocument instances.
 * <p>
 * In production there's only one instance, but unit testing it is
 * convenient to be able to mock it up rather than have to
 * instantiate a lot of eclipse editor UI machinery.
 *
 * @author Kris De Volder
 */
public interface DocumentContextFinder {

	IJavaProject getJavaProject(IDocument doc);
	SeverityProvider getSeverityProvider(IDocument doc);

}
