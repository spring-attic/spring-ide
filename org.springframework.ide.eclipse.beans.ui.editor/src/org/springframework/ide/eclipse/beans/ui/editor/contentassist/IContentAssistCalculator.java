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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * Implementations of this interface are fine-grained objects responsible for
 * calculating content assist proposals.
 * <p>
 * Normally a content assist processor would extend the
 * {@link NamespaceContentAssistProcessorSupport} class and register these
 * {@link IContentAssistCalculator} instances in the
 * {@link NamespaceContentAssistProcessorSupport#init()} method.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public interface IContentAssistCalculator {

	/**
	 * Calculate content assist proposals under the given context.
	 * @param request the content assist request to add the proposals to
	 * @param matchString the string the user has already entered prior to
	 * invoking the content assist
	 * @param attributeName the name of the attribute
	 * @param namespace the namespace of the attribute
	 * @param namepacePrefix the namespace prefix of the attribute
	 */
	void computeProposals(ContentAssistRequest request, String matchString,
			String attributeName, String namespace, String namepacePrefix);
}
