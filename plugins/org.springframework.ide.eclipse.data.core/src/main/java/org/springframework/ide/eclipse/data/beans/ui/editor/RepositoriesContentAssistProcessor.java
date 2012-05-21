/*
 * Copyright 2011 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
