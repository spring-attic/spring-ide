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

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Image;

/**
 * Records calculated content assist proposals in an internal structure.
 * <p>
 * Implementations of this interface abstract from the creation of actual {@link IContentProposal}
 * instances and make sure the {@link IContentAssistCalculator}s do not depend on context specfic
 * classes.
 * @author Christian Dupuis
 * @since 2.2.1
 */
public interface IContentAssistProposalRecorder {
	
	/**
	 * Record a calculated content assist proposal
	 * @param image the image to show in the content assist
	 * @param relevance the sorting relevance
	 * @param displayText the text to display in the content assist UI widget
	 * @param replaceText the replace text added to the document
	 */
	void recordProposal(Image image, int relevance, String displayText, String replaceText);

	/**
	 * Record a calculated content assist proposal
	 * @param image the image to show in the content assist
	 * @param relevance the sorting relevance
	 * @param displayText the text to display in the content assist UI widget
	 * @param replaceText the replace text added to the document
	 * @param proposedObject the proposed object; can be used to calculate contextual information
	 */
	void recordProposal(Image image, int relevance, String displayText, String replaceText,
			Object proposedObject);

}
