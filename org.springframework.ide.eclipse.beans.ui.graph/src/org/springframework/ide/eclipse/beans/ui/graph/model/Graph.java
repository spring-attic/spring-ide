/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.graph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.swt.graphics.Font;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.ide.eclipse.beans.ui.graph.figures.BeanFigure;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;

public class Graph implements IAdaptable {

	/* Max width of rows with orphan beans (unconnected beans) if no subgraph
	 * is available */
	private static final int MAX_ORPHAN_ROW_WIDTH = 600;

	/* Default amount of empty space to be left around a node */
	private static final Insets DEFAULT_PADDING = new Insets(16);

	private INode node;
	private	List beans;
	private Map beanNames;
	private	DirectedGraph graph;

	public Graph(INode node) {
		this.node = node;
		this.graph = new DirectedGraph();
		createListOfBeans();
	}

	/**
	 * Creates a list with all beans belonging to the graph's config / config
	 * set or being referenced from the graph's node.
	 */
	protected void createListOfBeans() {
		BeanNode[] nodes;
		if (this.node instanceof ConfigNode) {
			nodes = ((ConfigNode) this.node).getBeans(false);
		} else if (this.node instanceof ConfigSetNode) {
			nodes = ((ConfigSetNode) this.node).getBeans(false);
		} else {
			BeanNode bean = (BeanNode) this.node;
			ArrayList list = new ArrayList();
			list.add(this.node);
			list.addAll(bean.getReferencedBeans());
			nodes = (BeanNode[]) list.toArray(new BeanNode[list.size()]);
		}

		// Clone all beans found
		this.beans = new ArrayList();
		this.beanNames = new HashMap();
		for (int i = 0; i < nodes.length; i++) {
			Bean bean = new Bean(nodes[i]);
			this.beans.add(bean);
			this.beanNames.put(bean.getName(), bean);
		}
	}

	public List getBeans() {
		return beans;
	}

	public Bean getBean(String name) {
		return (Bean) beanNames.get(name);
	}

	public List getNodes() {
		return graph.nodes;
	}

	public void layout(Font font) {

		// Connect all unreferenced beans with a temporary root bean
		Bean root = new Bean(new BeanNode(null, "root"));
		graph.nodes.add(root);

		// Iterate through all beans to calculate label width and add bean
		// references to list of graph edges
		Iterator iter = getBeans().iterator();
		while (iter.hasNext()) {
			Bean bean = (Bean) iter.next();
			BeanDefinition beanDef = bean.getDefinition();

			// Calculate bean's dimension with a temporary bean figure 
			BeanFigure dummy = new BeanFigure(bean);
			dummy.setFont(font);
			Dimension size = dummy.getPreferredSize();
			bean.width = size.width;
			bean.height = size.height;
			bean.preferredHeight = size.height;
			graph.nodes.add(bean);

			// If child bean then add reference from parent bean to list of
			// graph edges
			if (beanDef instanceof ChildBeanDefinition) {
				Bean parentBean = getBean(((ChildBeanDefinition)
													  beanDef).getParentName());
				if (parentBean != null) {
					graph.edges.add(new Reference(bean, parentBean));
				}
			}

			// Add all bean references in construcor arguments to list of graph
			// edges
			ConstructorArgument[] cargs = bean.getConstructorArguments();
			for (int i = 0; i < cargs.length; i++) {
				ConstructorArgument carg = cargs[i];
				Iterator iter2 = carg.getBeanReferences().iterator();
				while (iter2.hasNext()) {
					RuntimeBeanReference beanRef = (RuntimeBeanReference)
																   iter2.next();
					Bean targetBean = getBean(beanRef.getBeanName());
					if (targetBean != null) {
						graph.edges.add(new Reference(bean, targetBean,
													  carg));
					}
				}
			}

			// Add all bean references in properties to list of graph edges
			Property[] properties = bean.getProperties();
			for (int i = 0; i < properties.length; i++) {
				Property property = properties[i];
				Iterator iter2 = property.getBeanReferences().iterator();
				while (iter2.hasNext()) {
					RuntimeBeanReference beanRef = (RuntimeBeanReference)
																   iter2.next();
					Bean targetBean = getBean(beanRef.getBeanName());
					if (targetBean != null) {
						graph.edges.add(new Reference(bean, targetBean,
													  property));
					}
				}
			}
		}

		// Connect all unreferenced beans with a temporary root bean and collect
		// subgraph root beans
		EdgeList rootEdges = new EdgeList();
		List orphanBeans = new ArrayList();
		iter = getBeans().iterator();
		while (iter.hasNext()) {
			Bean bean = (Bean) iter.next();
			if (bean.incoming.isEmpty()) {
				if (bean.outgoing.isEmpty()) {
					orphanBeans.add(bean);
				} else {
					Reference reference = new Reference(root, bean);
					reference.weight = 0;
					rootEdges.add(reference);
					graph.edges.add(reference);
				}
			}
		}

		// Remove all subgraph root beans from graph
		iter = orphanBeans.iterator();
		while (iter.hasNext()) {
			graph.nodes.remove(iter.next());
		}

		// Calculate position of all beans in graph
		new DirectedGraphLayout().visit(graph);

		// Remove temporary root and root edges
		for (int i = 0; i < rootEdges.size(); i++) {
			Edge e = (Edge) rootEdges.getEdge(i);
			e.source.outgoing.remove(e);
			e.target.incoming.remove(e);
			graph.edges.remove(e);
		}
		graph.nodes.remove(root);

		// Re-align nodes and edges' bend points topmost vertical position
		int maxY = 0;  // max height of graph
		int maxX = 0;  // max width of graph
		int ranks = graph.ranks.size();
		if (ranks > 1) {
			int deltaY = graph.ranks.getRank(1).getNode(0).y;
			Iterator nodes = graph.nodes.iterator();
			while (nodes.hasNext()) {
				Bean node = (Bean) nodes.next();

				// Move node vertically and update max height 
				node.y -= deltaY;
				if ((node.y + node.height) > maxY) {
					maxY =  node.y + node.height;
				}

				// Update max width
				if ((node.x + node.width) > maxX) {
					maxX =  node.x + node.width;
				}
			}
			Iterator edges = graph.edges.iterator();
			while (edges.hasNext()) {
				Edge edge = (Edge) edges.next();
				if (edge.vNodes != null) {
					Iterator points = edge.vNodes.iterator();
					while (points.hasNext()) {
						Node node = (Node) points.next();
						node.y -= deltaY;
					}
				}
			}
		}

		// Re-add all unconnected beans to the bottom of the graph
		int x = 0;   // current horizontal position in current row
		int y = maxY;  // current row
		if (maxY > 0) {
			y += DEFAULT_PADDING.getHeight();
		}
		if (maxX < MAX_ORPHAN_ROW_WIDTH) {
			maxX = MAX_ORPHAN_ROW_WIDTH;
		}
		maxY = 0;  // max height of all figures in current row
		iter = orphanBeans.iterator();
		while (iter.hasNext()) {
			Bean bean = (Bean) iter.next();

			// If current row is filled then start new row
			if ((x + bean.width) > maxX) {
				bean.x = x = 0;
				bean.y = y += maxY + DEFAULT_PADDING.getHeight();
				maxY = bean.height; 
			} else {
				bean.y = y;
				bean.x = x;
				if (bean.height > maxY) {
					maxY = bean.height;
				}
			}
			x += bean.width + DEFAULT_PADDING.getWidth();
			graph.nodes.add(bean);
		}
	}

	public Object getAdapter(Class adapter) {
		return node.getAdapter(adapter);
	}
}
