/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Implementations of this interface can provide a {@link Map} of {@link Node}that can be referenced.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IReferenceableElementsLocator {

	Map<String, Node> getReferenceableElements(Document document, IFile file);

}
