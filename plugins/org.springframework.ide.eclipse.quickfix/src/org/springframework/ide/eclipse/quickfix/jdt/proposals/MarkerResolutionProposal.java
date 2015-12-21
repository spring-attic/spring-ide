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
package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * Abstract class for combining correction proposal with marker resolution
 * 
 * @author Terry Denney
 * @since 2.6
 */
public abstract class MarkerResolutionProposal extends AnnotationCompletionProposal implements IMarkerResolution {

	public MarkerResolutionProposal(String name, ICompilationUnit cu, Image image) {
		super(name, cu, image);
	}

	public void run(IMarker marker) {
		IDocument document = QuickfixUtils.getDocument(marker);
		if (document != null) {
			apply(document);
		}
	}

	public String getLabel() {
		return getName();
	}
}
