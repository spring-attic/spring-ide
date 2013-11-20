/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.completions;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalType;

/**
 * Thin wrapper around a {@link MultiMap} which is interpreted as edges of
 * a directed graph.
 * 
 * Only operations that traverse edges from parent to child are supported.
 * Other operations would be too expensive to implement (require back pointers or
 * complete graph traversals).
 * 
 * Note using raw types because it is based on an implementation of
 * {@link MultiMap} that doesn't use generics.
 * 
 * @author Kris De Volder
 */
public class DirectedGraph {
	
	private MultiMap dgraph;

	public DirectedGraph() {
		this.dgraph = new MultiValueMap();
	}
	
	/**
	 * Retrieve all nodes in the graph that are reachable from a given starting node.
	 * The starting node itself is not included in the result unless there is cycle
	 * in the graph leading back to the starting node.
	 */
	@SuppressWarnings("rawtypes")
	public Set getDescendants(Object node) {
		Set descendants = new LinkedHashSet();
		return getDescendants(node, descendants);
	}

	/**
	 * Retrieve all nodes in the graph that are reachable from a given starting node.
	 * The starting node itself is not included in the result unless there is cycle
	 * in the graph leading back to the starting node.
	 * 
	 * @param descendants a (emtpy) collection that will be used to collect the
	 *     result into (this allows client to determine the type of collection used 
	 *     (e.g. HashSet versus LinkedHashSet).
	 */
	private Set getDescendants(Object node, Set descendants) {
		Assert.isLegal(descendants.isEmpty());
		collectDescendants(node, descendants);
		return descendants;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void collectDescendants(Object node, Set ancestors) {
		Collection parents = (Collection) dgraph.get(node);
		if (parents!=null && !parents.isEmpty()) {
			for (Object parent : parents) {
				boolean isNew = ancestors.add(parent);
				if (isNew) {
					collectDescendants(parent, ancestors);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Collection getSuccessors(ExternalType type) {
		return (Collection) dgraph.get(type);
	}

	public void addEdge(Object parent, Object child) {
		dgraph.put(parent, child);
	}

	/**
	 * Get all nodes in the graph that have at least one successor.
	 */
	@SuppressWarnings("rawtypes")
	public Set getNonLeafNodes() {
		return dgraph.keySet();
	}

}
