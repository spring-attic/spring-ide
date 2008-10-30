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

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * Extension to the {@link MethodContentAssistCalculator} that requires to implement the
 * {@link #getClassNode()} method in order to return a {@link Node} that
 * can be queried for a class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class NodeClassMethodContentAssistCalculator extends MethodContentAssistCalculator {

	public NodeClassMethodContentAssistCalculator(IMethodFilter filter) {
		super(filter);
	}

	@Override
	protected final IType calculateType(IContentAssistContext context) {
		Node node = getClassNode(context);
		if (node != null) {
			String className = BeansEditorUtils.getClassNameForBean(context.getFile(), node
					.getOwnerDocument(), node);
			return JdtUtils.getJavaType(context.getFile().getProject(), className);
		}
		return null;
	}

	protected abstract Node getClassNode(IContentAssistContext context);
}
