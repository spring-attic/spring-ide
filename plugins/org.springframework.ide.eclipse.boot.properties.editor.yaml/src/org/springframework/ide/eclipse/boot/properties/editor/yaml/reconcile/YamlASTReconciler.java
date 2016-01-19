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

import static org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil.asScalar;
import static org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST.getChildren;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.ValueParser;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef.Kind;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef.TupleValueRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
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
		case scalar:
			if (!isIgnoreScalarAssignmentTo(nav.getPrefix())) {
				expectMapping(node);
			}
			break;
		default:
			expectMapping(node);
			break;
		}
	}

	protected boolean isIgnoreScalarAssignmentTo(String propName) {
		//See https://issuetracker.springsource.com/browse/STS-4144
		return propName!=null && propName.equals("spring.profiles");
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
			if (match==null && extension==null) {
				//nothing found for this key. Maybe user is using camelCase variation of the key?
				String keyAlias = StringUtil.camelCaseToHyphens(key);
				IndexNavigator subNavAlias = nav.selectSubProperty(keyAlias);
				match = subNavAlias.getExactMatch();
				extension = subNavAlias.getExtensionCandidate();
				if (match!=null || extension!=null) {
					//Got something for the alias, so use that instead.
					//Note: do not swap for alias unless we actually found something.
					// This gives more logical errors (in terms of user's key, not its canonical alias)
					subNav = subNavAlias;
				}
			}
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
				unkownProperty(keyNode, subNav.getPrefix(), entry);
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
			expectTypeFoundMapping(type, mapping);
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
					Node value = entry.getValueNode();
					Type nestedValueType = valueType;
					if (value.getNodeId()==NodeId.mapping) {
						if (TypeUtil.isObject(valueType) || TypeUtil.isString(keyType)) {
							//See https://issuetracker.springsource.com/browse/STS-4254
							nestedValueType = type;
						}
					}
					reconcile(entry.getValueNode(), nestedValueType);
				}
			}
		} else {
			// Neither atomic, map or sequence-like => bean-like
			Map<String, Type> props = typeUtil.getPropertiesMap(type, EnumCaseMode.ALIASED, BeanPropertyNameMode.ALIASED);
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
			expectTypeFoundSequence(type, seq);
		} else if (TypeUtil.isSequencable(type)) {
			Type domainType = TypeUtil.getDomainType(type);
			if (domainType!=null) {
				for (Node element : seq.getValue()) {
					reconcile(element, domainType);
				}
			}
		} else {
			expectTypeFoundSequence(type, seq);
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
					valueTypeMismatch(type, scalar);
				}
			}
		}
	}

	private void expectTypeFoundMapping(Type type, MappingNode node) {
		expectType(ProblemType.YAML_EXPECT_TYPE_FOUND_MAPPING, type, node);
	}

	private void expectTypeFoundSequence(Type type, SequenceNode seq) {
		expectType(ProblemType.YAML_EXPECT_TYPE_FOUND_SEQUENCE, type, seq);
	}

	private void valueTypeMismatch(Type type, ScalarNode scalar) {
		expectType(ProblemType.YAML_VALUE_TYPE_MISMATCH, type, scalar);
	}

	private void unkownProperty(Node node, String name, NodeTuple entry) {
		SpringPropertyProblem p = problem(ProblemType.YAML_UNKNOWN_PROPERTY, node, "Unknown property '"+name+"'");
		p.setPropertyName(extendForQuickfix(StringUtil.camelCaseToHyphens(name), entry.getValueNode()));
		problems.accept(p);
	}

	private String extendForQuickfix(String name, Node node) {
		if (node!=null) {
			TupleValueRef child = getFirstTupleValue(getChildren(node));
			if (child!=null) {
				String extra = NodeUtil.asScalar(child.getKey());
				if (extra!=null) {
					return extendForQuickfix(name + "." + StringUtil.camelCaseToHyphens(extra),
							child.get());
				}
			}
		}
		//couldn't extend name any further
		return name;
	}

	private TupleValueRef getFirstTupleValue(List<NodeRef<?>> children) {
		for (NodeRef<?> nodeRef : children) {
			if (nodeRef.getKind()==Kind.VAL) {
				return (TupleValueRef) nodeRef;
			}
		}
		return null;
	}

	private void expectScalar(Node node) {
		problems.accept(problem(ProblemType.YAML_EXPECT_SCALAR, node, "Expecting a 'Scalar' node but got "+describe(node)));
	}

	protected void expectMapping(Node node) {
		problems.accept(problem(ProblemType.YAML_EXPECT_MAPPING, node, "Expecting a 'Mapping' node but got "+describe(node)));
	}

	private void expectBeanPropertyName(Node keyNode, Type type) {
		problems.accept(problem(ProblemType.YAML_EXPECT_BEAN_PROPERTY_NAME, keyNode, "Expecting a bean-property name for object of type '"+typeUtil.niceTypeName(type)+"' "
				+ "but got "+describe(keyNode)));
	}

	private void unknownBeanProperty(Node keyNode, Type type, String name) {
		problems.accept(problem(ProblemType.YAML_INVALID_BEAN_PROPERTY, keyNode, "Unknown property '"+name+"' for type '"+typeUtil.niceTypeName(type)+"'"));
	}

	private void expectType(ProblemType problemType, Type type, Node node) {
		problems.accept(problem(problemType, node, "Expecting a '"+typeUtil.niceTypeName(type)+"' but got "+describe(node)));
	}

	protected SpringPropertyProblem problem(ProblemType type, Node node, String msg) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return SpringPropertyProblem.problem(type, msg, start, end-start);
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
