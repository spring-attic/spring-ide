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
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;

/**
 * {@link IContentAssistCalculator} that can be used to calculate proposals for
 * classes or interfaces.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class ClassContentAssistCalculator implements IContentAssistCalculator {

	private final boolean isInterfaceRequired;
	
	/**
	 * Default constructor
	 */
	public ClassContentAssistCalculator() {
		this(false);
	}

	/**
	 * Constructor
	 * @param isInterfaceRequired true if only looking for interfaces
	 */
	public ClassContentAssistCalculator(boolean isInterfaceRequired) {
		this.isInterfaceRequired = isInterfaceRequired;
	}

	/**
	 * Compute proposals. This implementation simply delegates to
	 * {@link BeansJavaCompletionUtils#addClassValueProposals()}
	 */
	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		if (isInterfaceRequired) {
			BeansJavaCompletionUtils.addInterfaceValueProposals(request, matchString);
		}
		else {
			BeansJavaCompletionUtils.addClassValueProposals(request, matchString);
		}
	}
}
