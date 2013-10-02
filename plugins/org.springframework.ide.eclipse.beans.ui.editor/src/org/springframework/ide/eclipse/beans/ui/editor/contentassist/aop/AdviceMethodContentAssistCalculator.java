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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Extension of the {@link MethodContentAssistCalculator} that looks for public, non-constructor and
 * non-interface methods on the aspect backing bean (ref attribute on the aspect element).
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class AdviceMethodContentAssistCalculator extends MethodContentAssistCalculator {

	public AdviceMethodContentAssistCalculator() {
		super(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC | FlagsMethodFilter.NOT_CONSTRUCTOR
				| FlagsMethodFilter.NOT_INTERFACE));
	}

	@Override
	protected IType calculateType(IContentAssistContext context) {
		if (context.getParentNode() != null
				&& "aspect".equals(context.getParentNode().getLocalName())) {
			String ref = BeansEditorUtils.getAttribute(context.getParentNode(), "ref");
			if (ref != null) {
				IFile file = context.getFile();
				String className = BeansEditorUtils.getClassNameForBean(file,
						context.getDocument(), ref);
				if (file != null && file.exists()) {
					return JdtUtils.getJavaType(file.getProject(), className);
				}
			}
		}
		return null;
	}

}