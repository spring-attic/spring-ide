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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * Extension to the {@link MethodContentAssistCalculator} that requires to
 * implement the {@link #getClassNode(ContentAssistRequest, String)} method in
 * order to return a {@link Node} that can be queried for a class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public abstract class NodeClassMethodContentAssistCalculator extends
		MethodContentAssistCalculator {

	public NodeClassMethodContentAssistCalculator(IMethodFilter filter) {
		super(filter);
	}

	@Override
	protected final IType calculateType(ContentAssistRequest request,
			String attributeName) {
		Node node = getClassNode(request, attributeName);
		if (node != null) {
			String className = BeansEditorUtils.getClassNameForBean(
					BeansEditorUtils.getFile(request), node.getOwnerDocument(),
					node);
			IFile file = BeansEditorUtils.getFile(request);
			return JdtUtils.getJavaType(file.getProject(), className);
		}
		return null;
	}

	protected abstract Node getClassNode(ContentAssistRequest request,
			String attributeName);
}
