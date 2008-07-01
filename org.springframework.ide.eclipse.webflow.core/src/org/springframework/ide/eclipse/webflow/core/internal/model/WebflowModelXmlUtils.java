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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowModelXmlUtils {

	/**
	 * The Constant PRIORITIES.
	 */
	private static final Map<String, Integer> PRIORITIES;

	static {
		PRIORITIES = new HashMap<String, Integer>();

		// flow state
		PRIORITIES.put("flow.attribute", 1);
		PRIORITIES.put("flow.secured", 2);
		PRIORITIES.put("flow.persistence-context", 3);
		PRIORITIES.put("flow.var", 4);
		PRIORITIES.put("flow.input-mapper", 5);
		PRIORITIES.put("flow.input", 6);
		PRIORITIES.put("flow.start-actions", 7);
		PRIORITIES.put("flow.on-start", 8);
		PRIORITIES.put("flow.start-state", 9);
		PRIORITIES.put("flow.set", 10);
		PRIORITIES.put("flow.action-state", 11);
		PRIORITIES.put("flow.view-state", 12);
		PRIORITIES.put("flow.decision-state", 12);
		PRIORITIES.put("flow.subflow-state", 12);
		PRIORITIES.put("flow.end-state", 13);
		PRIORITIES.put("flow.global-transitions", 14);
		PRIORITIES.put("flow.end-actions", 15);
		PRIORITIES.put("flow.on-end", 16);
		PRIORITIES.put("flow.output-mapper", 17);
		PRIORITIES.put("flow.output", 18);
		PRIORITIES.put("flow.exception-handler", 19);
		PRIORITIES.put("flow.import", 20);
		PRIORITIES.put("flow.bean-import", 21);
		PRIORITIES.put("flow.inline-flow", 22);

		// action state
		PRIORITIES.put("action-state.attribute", 1);
		PRIORITIES.put("action-state.secured", 2);
		PRIORITIES.put("action-state.entry-actions", 3);
		PRIORITIES.put("action-state.on-entry", 4);
		PRIORITIES.put("action-state.action", 5);
		PRIORITIES.put("action-state.bean-action", 6);
		PRIORITIES.put("action-state.evaluate-action", 7);
		PRIORITIES.put("action-state.evaluate", 7);
		PRIORITIES.put("action-state.render", 7);
		PRIORITIES.put("action-state.set", 7);
		PRIORITIES.put("action-state.transition", 8);
		PRIORITIES.put("action-state.exit-actions", 9);
		PRIORITIES.put("action-state.on-exit", 10);
		PRIORITIES.put("action-state.exception-handler", 11);

		// view state
		PRIORITIES.put("view-state.attribute", 1);
		PRIORITIES.put("view-state.secured", 2);
		PRIORITIES.put("view-state.var", 3);
		PRIORITIES.put("view-state.entry-actions", 4);
		PRIORITIES.put("view-state.on-entry", 5);
		PRIORITIES.put("view-state.render-actions", 6);
		PRIORITIES.put("view-state.on-render", 7);
		PRIORITIES.put("view-state.transition", 8);
		PRIORITIES.put("view-state.exit-actions", 9);
		PRIORITIES.put("view-state.on-exit", 10);
		PRIORITIES.put("view-state.exception-handler", 12);

		// decision state
		PRIORITIES.put("decision-state.attribute", 1);
		PRIORITIES.put("decision-state.secured", 2);
		PRIORITIES.put("decision-state.entry-actions", 3);
		PRIORITIES.put("decision-state.on-entry", 4);
		PRIORITIES.put("decision-state.if", 5);
		PRIORITIES.put("decision-state.exit-actions", 6);
		PRIORITIES.put("decision-state.on-exit", 7);
		PRIORITIES.put("decision-state.exception-handler", 8);

		// sub flow state
		PRIORITIES.put("subflow-state.attribute", 1);
		PRIORITIES.put("subflow-state.secured", 2);
		PRIORITIES.put("subflow-state.entry-actions", 3);
		PRIORITIES.put("subflow-state.on-entry", 4);
		PRIORITIES.put("subflow-state.attribute-mapper", 5);
		PRIORITIES.put("subflow-state.input", 6);
		PRIORITIES.put("subflow-state.output", 7);
		PRIORITIES.put("subflow-state.transition", 8);
		PRIORITIES.put("subflow-state.exit-actions", 9);
		PRIORITIES.put("subflow-state.on-exit", 10);
		PRIORITIES.put("subflow-state.exception-handler", 11);

		// end state
		PRIORITIES.put("end-state.attribute", 1);
		PRIORITIES.put("end-state.secured", 2);
		PRIORITIES.put("end-state.entry-actions", 3);
		PRIORITIES.put("end-state.on-entry", 4);
		PRIORITIES.put("end-state.output-mapper", 5);
		PRIORITIES.put("end-state.output", 6);
		PRIORITIES.put("end-state.exception-handler", 7);

		// bean action
		PRIORITIES.put("bean-action.attribute", 1);
		PRIORITIES.put("bean-action.method-arguments", 2);
		PRIORITIES.put("bean-action.method-result", 3);

		// evaludate action
		PRIORITIES.put("evaluate-action.attribute", 1);
		PRIORITIES.put("evaluate-action.evaluation-result", 2);
		PRIORITIES.put("evaluate.attribute", 1);

		// input mapper
		PRIORITIES.put("input-mapper.input-attribute", 1);
		PRIORITIES.put("input-mapper.mapping", 2);

		// output mapper
		PRIORITIES.put("output-mapper.output-attribute", 1);
		PRIORITIES.put("output-mapper.mapping", 2);

		// action
		PRIORITIES.put("action.attribute", 1);

		// transition
		PRIORITIES.put("transition.attribute", 1);
		PRIORITIES.put("transition.secured", 2);
		PRIORITIES.put("transition.action", 3);
		PRIORITIES.put("transition.bean-action", 4);
		PRIORITIES.put("transition.evaluate-action", 5);
		PRIORITIES.put("transition.evaluate", 6);
		PRIORITIES.put("transition.render", 7);
		PRIORITIES.put("transition.set", 8);

	}

	/**
	 * Gets the state by id.
	 * @param webflowState the webflow state
	 * @param id the id
	 * @return the state by id
	 */
	public static IState getStateById(IWebflowState webflowState, String id) {
		if (webflowState.getStates() != null && webflowState.getStates().size() > 0) {
			for (IState state : webflowState.getStates()) {
				if (state.getId().equals(id)) {
					return state;
				}
			}
		}
		return null;
	}

	/**
	 * Insert node.
	 * @param parentNode the parent node
	 * @param nodeToInsert the node to insert
	 */
	public static void insertNode(Node nodeToInsert, Node parentNode) {
		Node node = determineNodeToInsert(nodeToInsert, parentNode);
		if (node != null) {
			parentNode.insertBefore(nodeToInsert, node);
		}
		else {
			parentNode.appendChild(nodeToInsert);
		}
		if (parentNode instanceof ElementImpl) {
			((ElementImpl) parentNode).setEmptyTag(false);
		}

		if (!"flow".equals(parentNode.getLocalName())) {
			// remove unrequired blank lines
			removeTextChildren(parentNode);
		}
	}

	/**
	 * @param refNode
	 * @param nodeToInsert
	 */
	public static void insertBefore(Node nodeToInsert, Node refNode) {
		Node parentNode = refNode.getParentNode();
		if (parentNode != null) {
			parentNode.insertBefore(nodeToInsert, refNode);
		}
		if (parentNode instanceof ElementImpl) {
			((ElementImpl) parentNode).setEmptyTag(false);
		}
		if (!"flow".equals(parentNode.getLocalName())) {
			// remove unrequired blank lines
			removeTextChildren(parentNode);
		}
	}

	/**
	 * Removes the text children.
	 * @param elem the elem
	 */
	public static void removeTextChildren(Node elem) {
		NodeList children = elem.getChildNodes();

		List<Node> textElements = new ArrayList<Node>();
		for (int j = 0; j < children.getLength(); j++) {
			Node nodetest = children.item(j);
			if (nodetest instanceof TextImpl) {
				textElements.add(nodetest);
			}
			else {
				removeTextChildren(nodetest);
			}
		}

		for (int k = 0; k < textElements.size(); k++) {
			elem.removeChild(textElements.get(k));
		}
		elem.normalize();
	}

	/**
	 * Determine node to insert.
	 * @param node the node
	 * @param nodeToInsert the node to insert
	 * @return the node
	 */
	public static Node determineNodeToInsert(Node nodeToInsert, Node node) {
		return determineNodeToInsert(nodeToInsert.getLocalName(), node);
	}

	/**
	 * Determine node to insert.
	 * @param elementName the element name
	 * @param node the node
	 * @return the node
	 */
	public static Node determineNodeToInsert(String elementName, Node node) {
		Node insertNode = null;
		Node lastChild = null;
		String key = node.getLocalName() + "." + elementName;
		if (!PRIORITIES.containsKey(key)) {
			return insertNode;
		}

		int prio = PRIORITIES.get(node.getLocalName() + "." + elementName);
		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getLocalName() != null) {
					key = node.getLocalName() + "." + children.item(i).getLocalName();
					if (PRIORITIES.containsKey(key)) {
						int p = PRIORITIES.get(key);
						if (prio < p) {
							insertNode = children.item(i);
							break;
						}
						else if (prio == p && children.item(i).getNextSibling() != null) {
							insertNode = children.item(i).getNextSibling();
							break;
						}
						else {
							lastChild = children.item(i);
						}
					}
				}
			}
		}

		if (insertNode == null && lastChild != null) {
			if (lastChild.getNextSibling() != null) {
				insertNode = lastChild.getNextSibling();
			}
			else {
				Text text = node.getOwnerDocument().createTextNode("");
				insertNode = node.appendChild(text);
			}
		}

		return insertNode;
	}

	/**
	 * Gets the states.
	 * @param includeSelf the include self
	 * @param state the state
	 * @return the states
	 */
	public static List<IState> getStates(IWebflowModelElement state, boolean includeSelf) {
		List<IState> states = new ArrayList<IState>();
		if (state instanceof IWebflowState) {
			states.addAll(((IWebflowState) state).getStates());
		}
		else if (state.getElementParent() instanceof IWebflowState) {
			IWebflowState sws = (IWebflowState) state.getElementParent();
			if (includeSelf) {
				states.addAll(sws.getStates());
			}
			else {
				for (IState s : sws.getStates()) {
					if (!s.equals(state)) {
						states.add(s);
					}
				}
			}
		}
		return states;
	}

	public static Map<IDOMNode, Integer> getNodeLineNumbers(IDOMNode root, IDOMNode clone) {
		Map<IDOMNode, Integer> nodesToLineNumbers = new HashMap<IDOMNode, Integer>();
		calculateNodeLineNumbers(root, clone, nodesToLineNumbers);
		return nodesToLineNumbers;
	}

	private static void calculateNodeLineNumbers(IDOMNode root, IDOMNode clone,
			Map<IDOMNode, Integer> nodesToLineNumbers) {
		if (root.getNodeType() == Node.ELEMENT_NODE) {
			int line = root.getStructuredDocument().getLineOfOffset(root.getStartOffset()) + 1;
			nodesToLineNumbers.put(clone, line);
		}
		NodeList rootChilds = root.getChildNodes();
		NodeList cloneChilds = clone.getChildNodes();
		for (int i = 0; i < rootChilds.getLength(); i++) {
			calculateNodeLineNumbers((IDOMNode) rootChilds.item(i), (IDOMNode) cloneChilds.item(i),
					nodesToLineNumbers);
		}
	}

	public static boolean isVersion1Flow(IWebflowModelElement element) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(
					element.getElementResource());
			if (model == null) {
				model = StructuredModelManager.getModelManager().getModelForRead(
						(IFile) element.getElementResource());

			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				NamedNodeMap attributes = document.getDocumentElement().getAttributes();
				IDOMAttr schemaLocationNode = (IDOMAttr) attributes.getNamedItemNS(
						"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
				String content = schemaLocationNode.getValue();
				return !content
						.contains("http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd");
			}
		}
		catch (Exception e) {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return false;
	}
}
