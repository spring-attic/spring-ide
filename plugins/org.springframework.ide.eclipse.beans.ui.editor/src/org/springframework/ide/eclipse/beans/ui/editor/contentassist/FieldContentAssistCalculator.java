/*******************************************************************************
 * Copyright (c) 2007 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link ClassContentAssistCalculator} extension that proposes static fields.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class FieldContentAssistCalculator extends ClassContentAssistCalculator {

	private final JavaElementImageProvider imageProvider = new JavaElementImageProvider();

	public FieldContentAssistCalculator() {
		super();
	}

	public void computeProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		String matchString = context.getMatchString();
		int ix = matchString.lastIndexOf('.');
		if (ix > 0) {
			String typeName = matchString.substring(0, ix);
			IFile file = context.getFile();
			if (file != null && file.exists()) {
				IType type = JdtUtils.getJavaType(file.getProject(), typeName);
				if (type != null) {
					try {
						IField[] fields = type.getFields();
						for (IField field : fields) {
							if (Flags.isStatic(field.getFlags()) && Flags.isPublic(field.getFlags())) {
								acceptSearchMatch(recorder, field, typeName);
							}
						}
					}
					catch (JavaModelException e) {
					}
					return;
				}
			}
		}
		super.computeProposals(context, recorder);

	}

	private void acceptSearchMatch(IContentAssistProposalRecorder recorder, IField field, String typeName) {
		try {
			String replaceText = typeName + "." + field.getElementName();
			String displayText = field.getElementName();
			Image image = imageProvider.getImageLabel(field, field.getFlags() | JavaElementImageProvider.SMALL_ICONS);
			recorder.recordProposal(image, 10, displayText, replaceText, field);

		}
		catch (JavaModelException e) {
		}
	}
}
