/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.model.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

/**
 * Factory that creates {@link IDOMDocument} instances.
 * @author Christian Dupuis
 * @since 2.3.1
 */
@SuppressWarnings("restriction")
public interface IDocumentFactory {

	/**
	 * Returns a IDomDocument instance for the given <code>file</code>.
	 * <p>
	 * This may return <code>null</code> if the given <code>file</code> cannot be loaded into an XML model.
	 */
	IDOMDocument createDocument(IFile file);

}
