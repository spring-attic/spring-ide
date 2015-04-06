/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

import static org.springframework.ide.eclipse.yaml.editor.ast.NodeUtil.*;

/**
 * Represents a parsed yml file.
 *
 * @author Kris De Volder
 */
public class YamlFileAST {

	private static final List<Node> NO_CHILDREN = Collections.emptyList();
	private List<Node> nodes;

	public YamlFileAST(Iterable<Node> iter) {
		nodes = new ArrayList<>();
		for (Node node : iter) {
			nodes.add(node);
		}
	}

	public List<Node> findPath(int offset) {
		Collector<Node> path = new Collector<Node>();
		findPath(offset, path);
		return path.get();
	}

	/**
	 * Find 'smallest' ast node that contains offset. The pathRequestor will
	 * be called as the search progresses down the AST on all nodes on the
	 * path to the smallest node. If no node in the tree contains the offset
	 * the requestor will not be called at all.
	 */
	public void findPath(int offset, IRequestor<Node> pathRequestor) {
		for (Node node : nodes) {
			if (contains(node, offset)) {
				pathRequestor.accept(node);
				findPath(node, offset, pathRequestor);
				return;
			}
		}
	}

	/**
	 * Find smallest node that is a child of 'n' that contains 'offset'.
	 */
	private void findPath(Node n, int offset, IRequestor<Node> pathRequestor) {
		//TODO: avoid garbage production by not using 'getChildren'
		// but inling getChildren in here (i.e a switch-case that visits
		// the children without putting them into temporary collections.)
		for (Node c : getChildren(n)) {
			if (contains(c, offset)) {
				pathRequestor.accept(c);
				findPath(c, offset, pathRequestor);
				return;
			}
		}
	}

	private List<Node> getChildren(Node n) {
		switch (n.getNodeId()) {
		case scalar:
			return NO_CHILDREN;
		case sequence:
			return (((SequenceNode)n).getValue());
		case mapping:
			return getChildren(((MappingNode)n).getValue());
		case anchor:
			//TODO: is this right? maybe we should visit down into 'realnode'
			// but do we then potentially visit the same node twice?
			return NO_CHILDREN;
		}
		return null;
	}

	private List<Node> getChildren(List<NodeTuple> entries) {
		ArrayList<Node> children = new ArrayList<>(entries.size()*2);
		for (NodeTuple e : entries) {
			children.add(e.getKeyNode());
			children.add(e.getValueNode());
		}
		return children;
	}

	public Node findNode(int offset) {
		RememberLast<Node> lastNode = new RememberLast<>();
		findPath(offset, lastNode);
		return lastNode.get();
	}

}
