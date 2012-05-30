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
package org.springframework.ide.eclipse.wizard.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.quickfix.AbstractContentAssistConverter;
import org.w3c.dom.Attr;


/**
 * Content assist converter that is used to validate bean attributes and
 * property attributes.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WizardContentAssistConverter extends AbstractContentAssistConverter {

	private final IDOMDocument originalDocument;

	public WizardContentAssistConverter(IDOMNode node, Attr attribute, IFile file, IDOMDocument originalDocument) {
		super(node, attribute.getName(), file);
		this.originalDocument = originalDocument;
	}

	@Override
	protected IContentAssistContext createContext(IDOMNode node, String attributeName, IFile file, String prefix) {
		return new WizardContentAssistContext(node, attributeName, file, prefix, originalDocument);
	}

}
