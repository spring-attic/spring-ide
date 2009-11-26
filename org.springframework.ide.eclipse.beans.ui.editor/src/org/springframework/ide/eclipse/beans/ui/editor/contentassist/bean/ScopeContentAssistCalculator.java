/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;

/**
 * {@link IContentAssistCalculator} for beans scope attribute.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public class ScopeContentAssistCalculator implements IContentAssistCalculator {

	private static final String[] SCOPES = new String[] { "singleton", "prototype", "request", "session" };

	public void computeProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		for (String scope : SCOPES) {
			if (scope.startsWith(context.getMatchString())) {
				recorder.recordProposal(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD), 0, scope, scope);
			}
		}
	}
}
