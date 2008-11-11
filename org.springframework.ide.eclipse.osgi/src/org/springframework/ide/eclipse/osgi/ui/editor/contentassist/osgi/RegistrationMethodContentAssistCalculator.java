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
package org.springframework.ide.eclipse.osgi.ui.editor.contentassist.osgi;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link IContentAssistCalculator} for the registration-listener attribute.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
public class RegistrationMethodContentAssistCalculator extends MethodContentAssistCalculator {

	public RegistrationMethodContentAssistCalculator() {
		super(new FlagsMethodFilter(FlagsMethodFilter.NOT_INTERFACE
				| FlagsMethodFilter.NOT_CONSTRUCTOR));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IType calculateType(IContentAssistContext context) {
		if (context.getNode() != null
				&& "registration-listener".equals(context.getNode().getLocalName())) {
			String ref = BeansEditorUtils.getAttribute(context.getNode(), "ref");
			if (ref != null) {
				IFile file = context.getFile();
				String className = BeansEditorUtils.getClassNameForBean(file, context.getNode()
						.getOwnerDocument(), ref);
				return JdtUtils.getJavaType(file.getProject(), className);
			}
		}
		return null;
	}

}
