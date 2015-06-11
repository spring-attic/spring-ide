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
package org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile;

import static org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.NodeUtil.asScalar;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.ValueParser;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * @author Kris De Volder
 */
public class YamlASTReconciler {

	private IProblemCollector problems;
	private TypeUtil typeUtil;

	public YamlASTReconciler(IProblemCollector problems, TypeUtil typeUtil) {
		this.problems = problems;
		this.typeUtil = typeUtil;
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
				Type type = TypeParser.parse(match.getType());
				reconcile(entry.getValueNode(), type);
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

	/**
	 * Reconcile a node given the type that we expect the node to be.
	 */
	private void reconcile(Node node, Type type) {
		if (type!=null) {
			switch (node.getNodeId()) {
			case scalar:
				reconcile((ScalarNode)node, type);
				break;
			case sequence:
				reconcile((SequenceNode)node, type);
				break;
			case mapping:
				reconcile((MappingNode)node, type);
				break;
			case anchor:
				//TODO: what should we do with anchor nodes
				break;
			default:
				throw new IllegalStateException("Missing switch case");
			}
		}
	}

	private void reconcile(MappingNode mapping, Type type) {
		if (typeUtil.isAtomic(type)) {
			expectType(type, mapping);
		} else if (TypeUtil.isMap(type) || TypeUtil.isSequencable(type)) {
			Type keyType = TypeUtil.getKeyType(type);
			Type valueType = TypeUtil.getDomainType(type);
			if (keyType!=null) {
				for (NodeTuple entry : mapping.getValue()) {
					reconcile(entry.getKeyNode(), keyType);
				}
			}
			if (valueType!=null) {
				for (NodeTuple entry : mapping.getValue()) {
					reconcile(entry.getValueNode(), valueType);
				}
			}
		} else {
			// Neither atomic, map or sequence-like => bean-like
			Map<String, Type> props = typeUtil.getPropertiesMap(type, EnumCaseMode.ALIASED);
			if (props!=null) {
				for (NodeTuple entry : mapping.getValue()) {
					Node keyNode = entry.getKeyNode();
					String key = NodeUtil.asScalar(keyNode);
					if (key==null) {
						expectBeanPropertyName(keyNode, type);
					} else if (!props.containsKey(key)) {
						unknownBeanProperty(keyNode, type, key);
					}

					if (key!=null) {
						Node valNode = entry.getValueNode();
						reconcile(valNode, props.get(key));
					}
				}
			}
		}
	}

	private void reconcile(SequenceNode seq, Type type) {
		if (typeUtil.isAtomic(type)) {
			expectType(type, seq);
		} else if (TypeUtil.isSequencable(type)) {
			Type domainType = TypeUtil.getDomainType(type);
			if (domainType!=null) {
				for (Node element : seq.getValue()) {
					reconcile(element, domainType);
				}
			}
		} else {
			expectType(type, seq);
		}
	}

	private void reconcile(ScalarNode scalar, Type type) {
		String stringValue = scalar.getValue();
		if (!stringValue.contains("${")) { //don't check anything with ${} expressions in it as we
											// don't know its actual value
			ValueParser valueParser = typeUtil.getValueParser(type);
			if (valueParser!=null) {
				// Tag tag = scalar.getTag(); //use the tag? Actually, boot tolerates String values
				//  even if integeger etc are expected. It has its ways of parsing the String to the
				//  expected type
				try {
					valueParser.parse(stringValue);
				} catch (Exception e) {
					//Couldn't parse
					expectType(type, scalar);
				}
			}
		}
	}

	private void unkownProperty(Node node, String name) {
		warning(node, "Unknown property '"+name+"'");
	}

	private void expectType(Type type, Node node) {
		error(node, "Expecting a '"+typeUtil.niceTypeName(type)+"' but got "+describe(node));
	}

	private void expectScalar(Node node) {
		error(node, "Expecting a 'Scalar' node but got "+describe(node));
	}

	protected void expectMapping(Node node) {
		error(node, "Expecting a 'Mapping' node but got "+describe(node));
	}

	private void expectBeanPropertyName(Node keyNode, Type type) {
		error(keyNode, "Expecting a bean-property name for object of type '"+typeUtil.niceTypeName(type)+"' "
				+ "but got "+describe(keyNode));
	}

	private void unknownBeanProperty(Node keyNode, Type type, String name) {
		error(keyNode, "Unknown property '"+name+"' for type '"+typeUtil.niceTypeName(type)+"'");
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
