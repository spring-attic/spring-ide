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
package org.springframework.ide.eclipse.beans.ui.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.beans.ui.actions.AbstractBeansConfigEditorAction;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Starts a basic refactoring action for nested ref and value tags
 * 
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class RefactorPropertyElementAction extends
		AbstractBeansConfigEditorAction {

	void processAction(IDocument document, ITextSelection textSelection) {
		IStructuredModel model = StructuredModelManager.getModelManager()
				.getExistingModelForEdit(document);
		if (model != null) {
			IndexedRegion region = model.getIndexedRegion(textSelection
					.getOffset());
			if (region instanceof Element) {
				Element elem = (Element) region;

				if (elem.getOwnerDocument() instanceof IDOMDocument) {

					if ("property".equals(elem.getTagName())
							|| "constructor-arg".equals(elem.getTagName())) {
						processNode(model, elem);
					}
					else if ("ref".equals(elem.getTagName())
							|| "value".equals(elem.getTagName())) {
						processNode(model, (Element) elem.getParentNode());
					}
				}
			}
		}
	}

	private void processNode(IStructuredModel model, Element elem) {
		model.beginRecording(this);
		model.aboutToChangeModel();
		try {
			NodeList children = elem.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {
				Node valueElement = children.item(i);
				if (valueElement != null) {
					if ("ref".equals(valueElement.getNodeName())) {
						String beanRef = null;
						NamedNodeMap attributes = valueElement.getAttributes();
						if (attributes != null) {
							if (attributes.getNamedItem("bean") != null) {
								beanRef = attributes.getNamedItem("bean")
										.getNodeValue();
							}
							if (attributes.getNamedItem("parent") != null) {
								beanRef = attributes.getNamedItem("parent")
										.getNodeValue();
							}
							if (attributes.getNamedItem("local") != null) {
								beanRef = attributes.getNamedItem("local")
										.getNodeValue();
							}
							if (beanRef != null) {
								elem.setAttribute("ref", beanRef);
								elem.removeChild(valueElement);
								removeTextChildren(elem);
								formatElement(elem);
							}
						}

					}
					else if ("value".equals(valueElement.getNodeName())) {

						if (valueElement.getFirstChild() != null) {
							String value = valueElement.getFirstChild()
									.getNodeValue();
							if (value != null) {
								elem.setAttribute("value", value);
								elem.removeChild(valueElement);
								removeTextChildren(elem);
								formatElement(elem);
							}
						}
					}
				}
			}

			// do the other way around
			if (!model.isDirty()) {
				if (elem.hasAttribute("ref")) {
					String beanRef = elem.getAttribute("ref");
					if (beanRef != null) {
						Element refElem = elem.getOwnerDocument()
								.createElement("ref");
						refElem.setAttribute("bean", beanRef);
						elem.appendChild(refElem);
						elem.removeAttribute("ref");
						removeTextChildren(elem);
						formatElement(elem);
					}
				}
				else if (elem.hasAttribute("value")) {

				}
			}

		}
		finally {
			model.changedModel();
			model.endRecording(this);
			model.releaseFromEdit();
		}
	}

	private void removeTextChildren(Element elem) {
		NodeList children = elem.getChildNodes();

		List<Node> textElements = new ArrayList<Node>();
		for (int j = 0; j < children.getLength(); j++) {
			Node nodetest = children.item(j);
			if (nodetest instanceof TextImpl) {
				textElements.add(nodetest);
			}
		}

		for (int k = 0; k < textElements.size(); k++) {
			elem.removeChild(textElements.get(k));
		}
		elem.normalize();
	}

	@Override
	public void run(IAction action) {
		IDocument document = getTextEditor().getDocumentProvider().getDocument(
				getTextEditor().getEditorInput());
		if (document != null) {
			// get current text selection
			ITextSelection textSelection = getCurrentSelection();
			if (textSelection.isEmpty())
				return;

			processAction(document, textSelection);
		}
	}

	private ITextSelection getCurrentSelection() {
		ISelectionProvider provider = getTextEditor().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}
		return TextSelection.emptySelection();
	}

	private void formatElement(Element element) {
		FormatProcessorXML formatProcessor = new FormatProcessorXML();
		formatProcessor.getFormatPreferences().setClearAllBlankLines(true);
		formatProcessor.formatNode(element);
	}

	/**
	 * The <code>ActionDelegate</code> implementation of this
	 * <code>IActionDelegate</code> method does nothing. Subclasses may
	 * reimplement.
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structSelection = (IStructuredSelection) selection;

				Object obj = structSelection.getFirstElement();
				if (obj instanceof Element) {
					Element elem = (Element) obj;
					if ("property".equals(elem.getTagName())
							|| "constructor-arg".equals(elem.getTagName())) {
						NodeList children = elem.getChildNodes();

						for (int i = 0; i < children.getLength(); i++) {
							Node child = children.item(i);
							if (child != null) {
								if ("value".equals(child.getNodeName())
										|| "ref".equals(child.getNodeName())) {
									enabled = true;
								}
							}
							else {
								enabled = true;
							}
						}

						if (children.getLength() == 0) {
							enabled = true;
						}
					}
					else if (("ref".equals(elem.getTagName()) || "value"
							.equals(elem.getTagName()))
							&& ("property".equals(elem.getParentNode()
									.getNodeName()) || "constructor-arg"
									.equals(elem.getParentNode().getNodeName()))) {
						enabled = true;
					}
				}
			}
		}
		action.setEnabled(enabled);
	}
}
