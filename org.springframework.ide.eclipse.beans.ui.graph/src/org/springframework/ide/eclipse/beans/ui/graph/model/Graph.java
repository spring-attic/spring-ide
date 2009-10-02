/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Font;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.beans.ui.graph.figures.BeanFigure;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class builds the graphical representation of the model data (given as {@link GraphEditorInput}) via GEF's
 * {@link DirectedGraphLayout}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class Graph implements IAdaptable {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String GRAPH_CONTENT_EXTENDER_EXTENSION_POINT = BeansGraphPlugin.PLUGIN_ID
			+ ".graphContentExtender";

	/*
	 * Max width of rows with orphan beans (unconnected beans) if no subgraph is available
	 */
	private static final int MAX_ORPHAN_ROW_WIDTH = 600;

	/* Default amount of empty space to be left around a node */
	private static final Insets DEFAULT_PADDING = new Insets(16);

	private static final String ERROR_TITLE = "Graph.error.title";

	private GraphEditorInput input;

	private DirectedGraph graph;

	private Map<String, Bean> beans = new HashMap<String, Bean>();

	private List<Reference> beanReferences = new ArrayList<Reference>();

	private String elementId;

	private String contextId;

	public Graph() {
		graph = new DirectedGraph();
	}

	public Graph(GraphEditorInput input) {
		this.input = input;
		this.elementId = input.getElementId();
		this.contextId = input.getContextId();
	}

	/**
	 * Initializes the embedded graph with nodes from GraphEditorInput's beans and edges from GraphEditorInput's bean
	 * references.
	 */
	@SuppressWarnings("unchecked")
	public void init() {

		createBeansMap();
		createReferences();
		extendGraphContent();

		graph = new DirectedGraph();

		for (Bean bean : beans.values()) {
			graph.nodes.add(bean);
		}

		for (Reference reference : beanReferences) {
			graph.edges.add(reference);
		}
	}

	public Object getAdapter(Class adapter) {
		return input.getAdapter(adapter);
	}

	protected Collection getBeans() {
		return beans.values();
	}

	protected Bean getBean(String name) {
		return (Bean) beans.get(name);
	}

	public List getNodes() {
		return graph.nodes;
	}

	@SuppressWarnings( { "unchecked", "deprecation" })
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
			}
			else {
				Reference reference = new Reference(BeanType.STANDARD, root, bean, false);
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
			int maxY = 0; // max height of graph
			int maxX = 0; // max width of graph
			int ranks = graph.ranks.size();
			if (ranks > 1) {
				int deltaY = graph.ranks.getRank(1).getNode(0).y;
				Iterator nodes = graph.nodes.iterator();
				while (nodes.hasNext()) {
					Bean node = (Bean) nodes.next();

					// Move node vertically and update max height
					node.y -= deltaY;
					if ((node.y + node.height) > maxY) {
						maxY = node.y + node.height;
					}

					// Update max width
					if ((node.x + node.width) > maxX) {
						maxX = node.x + node.width;
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
			int x = 0; // current horizontal position in current row
			int y = maxY; // current row
			if (maxY > 0) {
				y += DEFAULT_PADDING.getHeight();
			}
			if (maxX < MAX_ORPHAN_ROW_WIDTH) {
				maxX = MAX_ORPHAN_ROW_WIDTH;
			}
			maxY = 0; // max height of all figures in current row
			beans = orphanBeans.iterator();
			while (beans.hasNext()) {
				Bean bean = (Bean) beans.next();

				// If current row is filled then start new row
				if ((x + bean.width) > maxX) {
					bean.x = x = 0;
					bean.y = y += maxY + DEFAULT_PADDING.getHeight();
					maxY = bean.height;
				}
				else {
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
		catch (RuntimeException e) {

			// If an error occured during layouting (graph contains cylces,
			// graph not fully connected, ...) then clear graph, invalidate
			// editor input (not saved when Eclipse is closed) and display an
			// error message
			graph = new DirectedGraph();
			input.setHasError(true);
			MessageDialog.openError(BeansGraphPlugin.getActiveWorkbenchWindow().getShell(), BeansGraphPlugin
					.getResourceString(ERROR_TITLE), e.getMessage());

		}
	}

	@SuppressWarnings("deprecation")
	protected void extendGraphContent() {
		if (BeansUIPlugin.getDefault().getPluginPreferences().getBoolean(
				BeansUIPlugin.SHOULD_SHOW_EXTENDED_CONTENT_PREFERENCE_ID)) {
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
					GRAPH_CONTENT_EXTENDER_EXTENSION_POINT);
			if (point != null) {
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						if (config.getAttribute(CLASS_ATTRIBUTE) != null) {
							try {
								Object provider = config.createExecutableExtension(CLASS_ATTRIBUTE);
								if (provider instanceof IGraphContentExtender) {
									((IGraphContentExtender) provider).addAdditionalBeans(beans, beanReferences,
											(IBeansModelElement) getElement(elementId),
											(IBeansModelElement) getElement(contextId));
								}
							}
							catch (CoreException e) {
								BeansGraphPlugin.log(e);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a list with all beans belonging to the specified config / config set or being connected with the
	 * specified bean.
	 */
	protected void createBeansMap() {
		Set<IBean> list = new LinkedHashSet<IBean>();
		if (getElement(elementId) instanceof IBeansConfig) {
			IBeansConfig bc = (IBeansConfig) getElement(elementId);
			list.addAll(bc.getBeans());
			// add component registered beans
			addBeansFromComponents(list, bc.getComponents());
		}
		else if (getElement(elementId) instanceof IBeansConfigSet) {
			IBeansConfigSet bcs = (IBeansConfigSet) getElement(elementId);
			list.addAll(bcs.getBeans());
			// add component registered beans
			addBeansFromComponents(list, bcs.getComponents());
		}
		else if (getElement(elementId) instanceof IBean) {
			list.add((IBean) getElement(elementId));
			for (BeansConnection beanRef : BeansModelUtils.getBeanReferences(getElement(elementId),
					getElement(contextId), true)) {
				if (beanRef.getType() != BeanType.INNER) {
					list.add(beanRef.getTarget());
				}
			}
		}

		// Marshall all beans into a graph bean node
		beans = new LinkedHashMap<String, Bean>();
		for (IBean bean : list) {
			if (shouldAddBean(bean)) {
				beans.put(bean.getElementName(), new Bean(bean));
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean shouldAddBean(IBean bean) {
		return !bean.isInfrastructure()
				|| (bean.isInfrastructure() && BeansUIPlugin.getDefault().getPluginPreferences().getBoolean(
						BeansUIPlugin.SHOULD_SHOW_INFRASTRUCTURE_BEANS_PREFERENCE_ID));
	}

	private void addBeansFromComponents(Set<IBean> beans, Set<IBeansComponent> components) {
		for (IBeansComponent component : components) {
			Set<IBean> nestedBeans = component.getBeans();
			for (IBean nestedBean : nestedBeans) {
				if (shouldAddBean(nestedBean)) {
					beans.add(nestedBean);
				}
			}
			addBeansFromComponents(beans, component.getComponents());
		}
	}

	protected void createReferences() {
		beanReferences = new ArrayList<Reference>();
		// Add all beans defined in GraphEditorInput as nodes to the graph
		Iterator beans = this.beans.values().iterator();
		while (beans.hasNext()) {
			Bean bean = (Bean) beans.next();

			// Add all beans references from bean (parent, factory or
			// depends-on beans) to list of graph edges
			Iterator beanRefs = BeansModelUtils.getBeanReferences(bean.getBean(),
					BeansCorePlugin.getModel().getElement(contextId), false).iterator();
			while (beanRefs.hasNext()) {
				BeansConnection beanRef = (BeansConnection) beanRefs.next();
				Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
				if (targetBean != null && targetBean != bean && beanRef.getSource() instanceof IBean) {
					beanReferences.add(new Reference(beanRef.getType(), bean, targetBean, bean, beanRef.isInner()));
				}
			}

			// Add all bean references in bean's constructor arguments to list
			// of graph edges
			ConstructorArgument[] cargs = bean.getConstructorArguments();
			for (ConstructorArgument carg : cargs) {
				Iterator cargRefs = BeansModelUtils.getBeanReferences(carg.getBeanConstructorArgument(),
						BeansCorePlugin.getModel().getElement(contextId), false).iterator();
				while (cargRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) cargRefs.next();
					Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						beanReferences.add(new Reference(beanRef.getType(), bean, targetBean, carg, beanRef.isInner()));
					}
				}
			}

			// Add all bean references in properties to list of graph edges
			Property[] properties = bean.getProperties();
			for (Property property : properties) {
				Iterator propRefs = BeansModelUtils.getBeanReferences(property.getBeanProperty(),
						BeansCorePlugin.getModel().getElement(contextId), false).iterator();
				while (propRefs.hasNext()) {
					BeansConnection beanRef = (BeansConnection) propRefs.next();
					Bean targetBean = this.beans.get(beanRef.getTarget().getElementName());
					if (targetBean != null && targetBean != bean) {
						beanReferences.add(new Reference(beanRef.getType(), bean, targetBean, property, beanRef
								.isInner()));
					}
				}
			}
		}
	}

	private IModelElement getElement(String elementId) {
		return BeansCorePlugin.getModel().getElement(elementId);
	}

}
