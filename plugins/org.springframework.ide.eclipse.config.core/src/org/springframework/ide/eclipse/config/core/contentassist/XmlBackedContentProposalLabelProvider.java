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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for all content proposal providers extending
 * {@link XmlBackedContentProposalProvider}.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class XmlBackedContentProposalLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof XmlBackedContentProposal) {
			return ((XmlBackedContentProposal) element).getImage();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof XmlBackedContentProposal) {
			return ((XmlBackedContentProposal) element).getLabel();
		}
		return super.getText(element);
	}

}
