/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlASTReconciler;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * Detects mutually exclusive properties in Deployment Manifest YAML and reports errors.
 * 
 * @author Alex Boyko
 *
 */
public class ManifestYamlExclusivePropertiesReconciler implements YamlASTReconciler {
	
	@FunctionalInterface
	public interface ProblemFactory {
		ReconcileProblem problem(String key, int start, int end);
	}
	
	private static final String APPLICATIONS = "applications";
	
	final private IProblemCollector problems;
	final private Set<String> keys1;
	final private Set<String> keys2;
	final private ProblemFactory key1MessageFactory;
	final private ProblemFactory key2MessageFactory;
	
	public ManifestYamlExclusivePropertiesReconciler(IProblemCollector problems, Set<String> keys1, ProblemFactory key1MessageFactory, Set<String> keys2, ProblemFactory key2MessageFactory) {
		Assert.notNull(problems);
		Assert.notNull(keys1);
		Assert.notNull(keys2);
		this.problems = problems;
		this.keys1 = keys1;
		this.keys2 = keys2;
		this.key1MessageFactory = key1MessageFactory;
		this.key2MessageFactory = key2MessageFactory;
	}
	
	@Override
	public void reconcile(YamlFileAST ast, IProgressMonitor mon) {
		List<ScalarNode> key1Nodes = new ArrayList<>();
		List<ScalarNode> key2Nodes = new ArrayList<>();
		ast.getNodes().stream()
			.filter(n -> n instanceof MappingNode)
			.map(n -> (MappingNode) n)
			.forEach(n -> reconcileManifestNode(n, key1Nodes, key2Nodes, mon));
		
		if (!key1Nodes.isEmpty() && !key2Nodes.isEmpty()) {
			if (key1MessageFactory != null) {
				key1Nodes.forEach(keyNode -> problems.accept(key1MessageFactory.problem(keyNode.getValue(),
						keyNode.getStartMark().getIndex(), keyNode.getEndMark().getIndex())));
			}
			if (key2MessageFactory != null) {
				key2Nodes.forEach(keyNode -> problems.accept(key2MessageFactory.problem(keyNode.getValue(),
						keyNode.getStartMark().getIndex(), keyNode.getEndMark().getIndex())));
			}
		}
	}
	
	private void reconcileManifestNode(MappingNode n, Collection<ScalarNode> key1Nodes, Collection<ScalarNode> key2Nodes, IProgressMonitor mon) {
		NodeTuple applicationsTuple = findValueNode(n, APPLICATIONS);
		if (applicationsTuple.getValueNode() instanceof SequenceNode) {
			List<Node> appNodes = ((SequenceNode)applicationsTuple.getValueNode()).getValue();
			appNodes.stream()
				.filter(a -> a instanceof MappingNode)
				.map(a -> (MappingNode) a)
				.forEach(a -> reconcileNode(a, key1Nodes, key2Nodes));
		}
		reconcileNode(n, key1Nodes, key2Nodes);
	}
	
	private void reconcileNode(MappingNode n, Collection<ScalarNode> key1Nodes, Collection<ScalarNode> key2Nodes) {
		key1Nodes.addAll(findKeyNodes(n, keys1).collect(Collectors.toList()));
		key2Nodes.addAll(findKeyNodes(n, keys2).collect(Collectors.toList()));
	}
	
	private Stream<ScalarNode> findKeyNodes(MappingNode n, Set<String> keys) {
		return n.getValue().stream()
			.filter(tuple -> tuple.getKeyNode() instanceof ScalarNode)
			.map(tuple -> (ScalarNode) tuple.getKeyNode())
			.filter(keyNode -> keys.contains(keyNode.getValue()));
	}

	private static NodeTuple findValueNode(MappingNode node, String key) {
		for (NodeTuple tuple : node.getValue()) {
			if (tuple.getKeyNode() instanceof ScalarNode) {
				ScalarNode scalar = (ScalarNode) tuple.getKeyNode();
				if (key.equals(scalar.getValue())) {
					return tuple;
				}
			}
		}
		return null;
	}
	
}
