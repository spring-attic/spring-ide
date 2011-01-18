/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.w3c.dom.Node;

/**
 * Implementations of this interface are able to calculate content assist proposals based on a given annotation element.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public interface IAnnotationBasedContentAssistProcessor {

	/**
	 * Init this content assist processor.
	 */
	void init();

	/**
	 * Add content assist proposals for attribute values.
	 */
	void addAttributeValueProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder,
			Node annotation);

}
