/*******************************************************************************
 * Copyright (c) 2007, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.springframework.ide.eclipse.beans.ui.actions.AbstractBeansConfigEditorHandler;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract base class for XML related refactor triggers 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @author Tomasz Zarna
 * @since 2.0
 */
@SuppressWarnings( { "restriction" })
public abstract class AbstractBeansRefactorAction extends
		AbstractBeansConfigEditorHandler {

	protected String getSelectedAttributeName(ITextSelection textSelection) {
		String attributeName = null;
		if (textSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();
			if (obj instanceof Element) {
				int offset = textSelection.getOffset()
						- ((IDOMNode) obj).getStartOffset();
				IStructuredDocumentRegion open = ((IDOMNode) obj)
						.getFirstStructuredDocumentRegion();
				ITextRegionList openRegions = open.getRegions();
				ITextRegion nameRegion = null;

				for (int i = open.getNumberOfRegions() - 1; i >= 0; i--) {
					ITextRegion region = openRegions.get(i);
					if (region.getStart() <= offset
							&& region.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
						nameRegion = region;
						break;
					}
				}

				// the name region is REQUIRED to do anything useful
				if (nameRegion != null) {
					attributeName = open.getText(nameRegion);
				}
			}
		}
		return attributeName;
	}

	protected void processAction(ExecutionEvent event, IDocument document, ITextSelection textSelection) {
		if (textSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) textSelection)
					.getFirstElement();
			
			Element node = null;
			String attributeName = null;
			
			if (obj instanceof Attr) {
				Attr attribute = (Attr) obj;
				node = attribute.getOwnerElement();
				attributeName = attribute.getName();
			}
			else if (obj instanceof Element) {
				node = (Element) obj;
				attributeName = getSelectedAttributeName(textSelection);
			}
			
			if (node != null && attributeName != null) {
				// check if bean class is selected
				String className = BeansEditorUtils.getAttribute(node, "class");
				String propertyName = BeansEditorUtils.getAttribute(node,
						"name");
				IJavaElement je = null;
				if ("bean".equals(node.getLocalName())
						&& StringUtils.hasText(className)
						&& "class".equals(attributeName)) {
					je = JdtUtils.getJavaType(BeansEditorUtils
							.getProject(document), className);
				}
				else if ("property".equals(node.getLocalName())
						&& StringUtils.hasText(propertyName)
						&& "name".equals(attributeName)) {
					Node beanNode = node.getParentNode();
					List<IType> types = BeansEditorUtils.getClassNamesOfBean(
							getConfigFile(event), beanNode);

					if (types != null && types.size() > 0) {
						je = types.get(0).getField(propertyName);
						if (je == null || !je.exists()) {
							List<String> path = new ArrayList<String>();
							path.add(propertyName);
							je = BeansEditorUtils
									.extractMethodFromPropertyPathElements(
											path, types, getConfigFile(event), 0);
						}
					}
				}

				try {
					if (je != null) {
						run(je);
					}
				}
				catch (CoreException e) {
				}
			}
		}
	}

	protected abstract void run(IJavaElement element) throws CoreException;

	public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
		IDocument document = getTextEditor(executionEvent).getDocumentProvider().getDocument(
				getTextEditor(executionEvent).getEditorInput());
		if (document != null) {
			// get current text selection
			ITextSelection textSelection = getCurrentSelection(executionEvent);
			if (textSelection.isEmpty())
				return null;
			processAction(executionEvent, document, textSelection);
		}
		return null;
	}

}
