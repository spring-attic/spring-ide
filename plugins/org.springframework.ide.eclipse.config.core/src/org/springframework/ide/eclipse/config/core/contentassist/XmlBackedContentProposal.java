/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.contentassist;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * This interface extends {@link IContentProposal} in order to make it behave
 * more like {@link ICompletionProposal}.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public interface XmlBackedContentProposal extends IContentProposal {

	/**
	 * Returns the image to be displayed in the list of completion proposals.
	 * The image would typically be shown to the left of the display string.
	 * 
	 * @return the image to be shown or null if no image is desired
	 */
	public Image getImage();

}
