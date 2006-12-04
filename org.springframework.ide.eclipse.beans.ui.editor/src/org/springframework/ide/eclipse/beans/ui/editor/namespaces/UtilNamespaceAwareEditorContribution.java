/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.namespaces;


import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.INamespaceAwareEditorContribution;
import org.springframework.ide.eclipse.beans.ui.editor.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;

public class UtilNamespaceAwareEditorContribution implements
		INamespaceAwareEditorContribution {

	private IReferenceableElementsLocator referenceableElemenLocator;
	
	public INamespaceContentAssistProcessor getContentAssistProcessor() {
		return null;
	}

	public IHyperlinkDetector getHyperLinkDetector() {
		return null;
	}

	public JFaceNodeLabelProvider getLabelProvider(
			BeansContentOutlineConfiguration configuration,
			ILabelProvider parent) {
		return null;
	}

	public String getNamespaceURI() {
		return "http://www.springframework.org/schema/util";
	}
	

	public IReferenceableElementsLocator getReferenceableElementsLocator() {
		if (this.referenceableElemenLocator == null) {
			this.referenceableElemenLocator = new DefaultReferenceableElementsLocator(this);
		}
		return this.referenceableElemenLocator;
	}
}
