/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.beans.ui.editor;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.PackageContentAssistCalculator;

/**
 * {@link IContentAssistProcessor} to enable content assists for namespace XML attributes.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor#init()
	 */
	@Override
	public void init() {

		IContentAssistCalculator calculator = new PackageContentAssistCalculator();
		IContentAssistCalculator referenceLocator = new BeanReferenceContentAssistCalculator();

		registerContentAssistCalculator("base-package", calculator);
		registerContentAssistCalculator("auditing", "auditor-aware-ref", referenceLocator);
		registerContentAssistCalculator("repository", "custom-impl-ref", referenceLocator);
	}
}
