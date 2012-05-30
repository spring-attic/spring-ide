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
package org.springframework.ide.eclipse.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;

/**
 * Default content assist converter for generating IContentAssistContext
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class QuickfixContentAssistConverter extends AbstractContentAssistConverter {

	public QuickfixContentAssistConverter(IDOMNode node, String attributeName, IFile file) {
		super(node, attributeName, file);
	}

	@Override
	protected IContentAssistContext createContext(IDOMNode node, String attributeName, IFile file, String prefix) {
		return new QuickFixContentAssistContext(attributeName, file, node, prefix);
	}

}
