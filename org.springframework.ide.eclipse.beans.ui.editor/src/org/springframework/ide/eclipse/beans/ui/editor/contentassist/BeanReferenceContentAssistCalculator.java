/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;

/**
 * {@link IContentAssistCalculator} that can be used to located references to {@link IBean}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanReferenceContentAssistCalculator implements IContentAssistCalculator {

	protected boolean showExternal;

	/**
	 * Default constructor
	 */
	public BeanReferenceContentAssistCalculator() {
		this(true);
	}

	/**
	 * Constructor
	 * @param showExternal true if those beans should be displayed that are not located in the
	 * current file
	 */
	public BeanReferenceContentAssistCalculator(boolean showExternal) {
		this.showExternal = showExternal;
	}

	/**
	 * Compute the proposals. This implementation simply delegates to
	 * {@link BeansCompletionUtils#addBeanReferenceProposals()}.
	 */
	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		BeansCompletionUtils.addBeanReferenceProposals(context, recorder, showExternal);
	}
}
