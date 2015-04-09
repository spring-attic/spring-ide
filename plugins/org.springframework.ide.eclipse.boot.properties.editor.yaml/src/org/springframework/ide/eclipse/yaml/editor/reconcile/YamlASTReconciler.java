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
package org.springframework.ide.eclipse.yaml.editor.reconcile;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlFileAST;

import static org.springframework.ide.eclipse.yaml.editor.ast.YamlFileAST.*;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import static org.springframework.ide.eclipse.yaml.editor.ast.NodeUtil.*;

/**
 * @author Kris De Volder
 */
public class YamlASTReconciler {

	private IProblemCollector problems;

	public YamlASTReconciler(IProblemCollector problems) {
		this.problems = problems;
	}

	public void reconcile(YamlFileAST ast, IndexNavigator nav, IProgressMonitor mon) {
		List<Node> nodes = ast.getNodes();
		if (nodes!=null && !nodes.isEmpty()) {
			mon.beginTask("Reconcile", nodes.size());
			try {
				for (Node node : nodes) {
					reconcile(node, nav);
					mon.worked(1);
				}
			} finally {
				mon.done();
			}
		}
	}

	protected void reconcile(Node node, IndexNavigator nav) {
		switch (node.getNodeId()) {
		case mapping:
			for (NodeTuple entry : ((MappingNode)node).getValue()) {
				reconcile(entry, nav);
			}
			break;
		default:
			expectMapping(node);
			break;
		}
	}

	private void reconcile(NodeTuple entry, IndexNavigator nav) {
		Node keyNode = entry.getKeyNode();
		String key = asScalar(keyNode);
		if (key==null) {
			expectScalar(keyNode);
		} else {
			IndexNavigator subNav = nav.selectSubProperty(key);
			PropertyInfo match = subNav.getExactMatch();
			PropertyInfo extension = subNav.getExtensionCandidate();
			if (match!=null && extension!=null) {
				//This is an odd situation, the current prefix lands on a propery
				//but there are also other properties that have it as a prefix.
				//This ambiguity is hard to deal with and we choose not to do so for now
				return;
			} else if (match!=null) {
				//TODO: from here on down we should reconcile based on the type of
				// the selected property.
			} else if (extension!=null) {
				//We don't really care about the extension only about the fact that it
				// exists and so it is meaningful to continue checking...
				Node valueNode = entry.getValueNode();
				reconcile(valueNode, subNav);
			} else {
				//both are null, this means there's no valid property with the current prefix
				//whether exact or extending it with further navigation
				unkownProperty(keyNode, subNav.getPrefix());
			}
		}
	}

	private void unkownProperty(Node node, String name) {
		warning(node, "Unkown property '"+name+"'");
	}

	private void expectScalar(Node node) {
		error(node, "Expecting a 'Scalar' node but got "+describe(node));
	}

	protected void expectMapping(Node node) {
		error(node, "Expecting a 'Mapping' node but got "+describe(node));
	}

	protected void warning(Node node, String msg) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		problems.accept(SpringPropertyProblem.warning(msg, start, end-start));
	}

	protected void error(Node node, String msg) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		problems.accept(SpringPropertyProblem.error(msg, start, end-start));
	}

	private String describe(Node node) {
		switch (node.getNodeId()) {
		case scalar:
			return "'"+((ScalarNode)node).getValue()+"'";
		case mapping:
			return "a 'Mapping' node";
		case sequence:
			return "a 'Sequence' node";
		case anchor:
			return "a 'Anchor' node";
		default:
			throw new IllegalStateException("Missing switch case");
		}
	}

}
