/*
 * Copyright 2002-2007 the original author or authors.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Font;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.beans.ui.graph.figures.BeanFigure;

/**
 * This class builds the graphical representation of the model data (given as
 * {@link GraphEditorInput}) via GEF's {@link DirectedGraphLayout}.
 * 
 * @author Torsten Juergeleit
 */
public class Graph implements IAdaptable {

	/* Max width of rows with orphan beans (unconnected beans) if no subgraph
	 * is available */
	private static final int MAX_ORPHAN_ROW_WIDTH = 600;

	/* Default amount of empty space to be left around a node */
	private static final Insets DEFAULT_PADDING = new Insets(16);

	private static final String ERROR_TITLE = "Graph.error.title";

	private GraphEditorInput input;
	private	DirectedGraph graph;

	public Graph(GraphEditorInput input) {
		this.input = input;
		init();
	}

	/**
	 * Initializes the embedded graph with nodes from GraphEditorInput's beans
	 * and edges from GraphEditorInput's bean references.
	 */
	private void init() {
		graph = new DirectedGraph();

		// Add all beans defined in GraphEditorInput as nodes to the graph
		Iterator beans = getBeans().iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();
			graph.nodes.add(bean);

			// Add all beans references from bean (parent, factory or
			// depends-on beans) to list of graph edges
			Iterator beanRefs = BeansModelUtils.getBeanReferences(
						 bean.getBean(), input.getContext(), false).iterator();
			while (beanRefs.hasNext()) {
				BeansConnection beanRef = (BeansConnection) beanRefs.next();
				Bean targetBean = getBean(beanRef.getTarget().getElementName());
				if (targetBean != null && targetBean != bean &&
										beanRef.getSource() instanceof IBean) {
					graph.edges.add(new Reference(beanRef.getType(), bean,
												  targetBean));
				}
			}

			// Add all bean references in bean's constructor arguments to list
			// of graph edges
			ConstructorArgument[] cargs = bean.getConstructorArguments();
			for (ConstructorArgument carg : cargs) {
				Iterator cargRefs = BeansModelUtils.getBeanReferences(
										 carg.getBeanConstructorArgument(),
										 input.getContext(), false).iterator();
				while (cargRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) cargRefs.next();
					Bean targetBean = getBean(
										 beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						graph.edges.add(new Reference(beanRef.getType(), bean,
													  targetBean, carg));
					}
				}
			}

			// Add all bean references in properties to list of graph edges
			Property[] properties = bean.getProperties();
			for (Property property : properties) {
				Iterator propRefs = BeansModelUtils.getBeanReferences(
										 property.getBeanProperty(),
										 input.getContext(), false).iterator();
				while (propRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) propRefs.next();
					Bean targetBean = getBean(
										 beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						graph.edges.add(new Reference(beanRef.getType(), bean,
													  targetBean, property));
					}
				}
			}
		}
	}

	public Object getAdapter(Class adapter) {
		return input.getAdapter(adapter);
	}

	protected Collection getBeans() {
		return input.getBeans().values();
	}

	protected Bean getBean(String name) {
		return (Bean) input.getBeans().get(name);
	}

	public List getNodes() {
		return graph.nodes;
	}

	public void layout(Font font) {

		// Iterate through all graph nodes (beans) to calculate label width
		Iterator beans = graph.nodes.iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();

			// Calculate bean's dimension with a temporary bean figure 
			BeanFigure dummy = new BeanFigure(bean);
			dummy.setFont(font);
			Dimension size = dummy.getPreferredSize();
			bean.width = size.width;
			bean.height = size.height;
			bean.preferredHeight = size.height;
		}

		// Remove all unreferenced single beans and connect all unreferenced
		// subgraphs with a temporary root bean
		Bean root = new Bean();
		graph.nodes.add(root);

		EdgeList rootEdges = new EdgeList();
		List<Bean> orphanBeans = new ArrayList<Bean>();
		beans = getBeans().iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();
			if (bean.incoming.isEmpty() && bean.outgoing.isEmpty()) {
				orphanBeans.add(bean);
				graph.nodes.remove(bean);
			} else {
				Reference reference = new Reference(BeanType.STANDARD, root,
						bean);
				reference.weight = 0;
				rootEdges.add(reference);
				graph.edges.add(reference);
			}
		}

		// Calculate position of all beans in graph
		try {
			new DirectedGraphLayout().visit(graph);

			// Re-invert edges inverted while breaking cycles
			for (int i = 0; i < graph.edges.size(); i++) {
				Edge e = graph.edges.getEdge(i);
				if (e.isFeedback()) {
					e.invert();
				}
			}
	
			// Remove temporary root and root edges
			for (int i = 0; i < rootEdges.size(); i++) {
				Edge e = rootEdges.getEdge(i);
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
			beans = orphanBeans.iterator();
			while (beans.hasNext()) {
				Bean bean = (Bean) beans.next();
	
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
		} catch (RuntimeException e) {

			// If an error occured during layouting (graph contains cylces,
			// graph not fully connected, ...) then clear graph, invalidate
			// editor input (not saved when Eclipse is closed) and display an
			// error message
			graph = new DirectedGraph();
			input.setHasError(true);

			MessageDialog.openError(
					BeansGraphPlugin.getActiveWorkbenchWindow().getShell(),
					BeansGraphPlugin.getResourceString(ERROR_TITLE),
					e.getMessage());

		}
	}
}
