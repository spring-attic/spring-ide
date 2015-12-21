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
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;

/**
 * Quick fix proposal for renaming attribute value to a similar class/property
 * name
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class RenameToSimilarNameQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final String suggestedName;

	private final String suggestedDescription;

	public RenameToSimilarNameQuickFixProposal(String suggestedName, int offset, int length, boolean missingEndQuote) {
		this(suggestedName, offset, length, missingEndQuote, null);
	}

	public RenameToSimilarNameQuickFixProposal(String suggestedName, int offset, int length, boolean missingEndQuote,
			String suggestedDescription) {
		super(offset, length, missingEndQuote);
		this.suggestedName = suggestedName;
		this.suggestedDescription = suggestedDescription;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		try {
			document.replace(getOffset(), getLength(), suggestedName);
		}
		catch (BadLocationException e) {
		}
	}

	public String getDisplayString() {
		if (suggestedDescription != null) {
			return "Change to " + suggestedDescription;
		}

		int lastDotPos = suggestedName.lastIndexOf(".");
		String className, packageName;
		if (lastDotPos < 0) {
			className = suggestedName;
			packageName = "";
		}
		else {
			className = suggestedName.substring(lastDotPos + 1);
			packageName = " (" + suggestedName.substring(0, lastDotPos) + ")";
		}
		return "Change to " + className + packageName;
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
	}

}
