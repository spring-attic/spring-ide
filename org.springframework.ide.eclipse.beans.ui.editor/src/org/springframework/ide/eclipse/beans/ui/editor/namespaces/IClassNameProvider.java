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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces;

import org.w3c.dom.Element;

/**
 * Implementations of this interface return full qualified class names for
 * Spring 2.0 namespace elements.
 * <p>
 * E.g. the <pre><tx:transaction-manager /></pre> element would qualify to
 * TransactionManager.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IClassNameProvider {

	String getClassNameForElement(Element elem);

}
