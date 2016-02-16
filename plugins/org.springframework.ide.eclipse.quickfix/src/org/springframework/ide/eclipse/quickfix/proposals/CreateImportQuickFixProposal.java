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
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Quick fix proposal for adding an import node
 * @author Terry Denney
 * @since 2.1
 */
public class CreateImportQuickFixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private final IDOMNode beanNode;

	private final IPath relativeImportPath;

	private final IBeansProject project;

	public CreateImportQuickFixProposal(int offset, int length, boolean missingEndQuote, IBean importBean,
			IDOMNode beanNode, IBeansProject project, IFile file) {
		super(offset, length, missingEndQuote);
		this.beanNode = beanNode;
		this.project = project;

		IResource resource = importBean.getElementResource();
		IPath importPath = resource.getLocation();

		IPath path = file.getParent().getLocation();

		relativeImportPath = QuickfixReflectionUtils.getRelativePath(path, importPath);
	}

	@Override
	public void applyQuickFix(IDocument document) {
		IDOMElement beansNode = (IDOMElement) beanNode.getOwnerDocument().getDocumentElement();

		NodeList children = beansNode.getChildNodes();

		Node child = children.item(0);

		Element importNode = beanNode.getOwnerDocument().createElement(BeansSchemaConstants.ELEM_IMPORT);
		importNode.setAttribute(BeansSchemaConstants.ATTR_RESOURCE, getResourcePath());

		beansNode.insertBefore(importNode, child);
		new FormatProcessorXML().formatNode(importNode);

		if (project instanceof BeansProject) {
			((BeansProject) project).setImportsEnabled(true);
		}
	}

	public String getDisplayString() {
		return "Import " + getResourcePath();
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_IMPDECL);
	}

	private String getResourcePath() {
		return relativeImportPath.toString();
	}

}
