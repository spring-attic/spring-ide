/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Extension of the {@link MethodContentAssistCalculator} that looks for public,
 * non-constructor and non-interface methods on the aspect backing bean (ref
 * attribute on the aspect element).
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class AdiviceMethodContentAssistCalculator extends
		MethodContentAssistCalculator {

	public AdiviceMethodContentAssistCalculator() {
		super(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC
				| FlagsMethodFilter.NOT_CONSTRUCTOR
				| FlagsMethodFilter.NOT_INTERFACE));
	}

	@Override
	protected IType calculateType(ContentAssistRequest request,
			String attributeName) {
		if (request.getParent() != null
				&& request.getParent().getParentNode() != null
				&& "aspect".equals(request.getParent().getParentNode()
						.getLocalName())) {
			String ref = BeansEditorUtils.getAttribute(request.getParent()
					.getParentNode(), "ref");
			if (ref != null) {
				IFile file = BeansEditorUtils.getFile(request);
				String className = BeansEditorUtils.getClassNameForBean(file,
						request.getNode().getOwnerDocument(), ref);
				return JdtUtils.getJavaType(file.getProject(), className);
			}
		}
		return null;
	}
}