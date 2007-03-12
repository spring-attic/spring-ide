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

package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableFrom;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateNodeEditPolicy;

/**
 * 
 */
public abstract class AbstractStatePart extends AbstractGraphicalEditPart
		implements PropertyChangeListener, NodeEditPart {

	/**
	 * 
	 */
	protected static class TopOrBottomAnchor extends AbstractConnectionAnchor {

		/**
		 * 
		 */
		public static final int SOURCE_ANCHOR = 0;

		/**
		 * 
		 */
		public static final int TARGET_ANCHOR = 1;

		/**
		 * 
		 */
		private int offset;

		/**
		 * 
		 * 
		 * @param source 
		 * @param offset 
		 */
		TopOrBottomAnchor(IFigure source, int offset) {
			super(source);
			this.offset = offset;
		}

		/**
		 * 
		 * 
		 * @param reference 
		 * 
		 * @return 
		 */
		public Point getLocation(Point reference) {
			Rectangle r = getOwner().getBounds().getCopy();
			getOwner().translateToAbsolute(r);
			int off = offset;
			if (off == -1)
				off = r.width / 2;
			if (r.contains(reference) || r.y < reference.y)
				return r.getBottom().translate(off, -1);
			else
				return r.getTop().translate(off, 0);
		}
	}

	/**
	 * 
	 */
	private ConnectionAnchor connectionAnchor;

	/**
	 * 
	 */
	protected DirectEditManager manager;

	/**
	 * 
	 */
	public void activate() {
		super.activate();
		((IWebflowModelElement) getModel()).addPropertyChangeListener(this);
	}

	/**
	 * 
	 * 
	 * @param graph 
	 * @param map 
	 */
	protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
		Node n = (Node) map.get(this);
		getFigure().setBounds(
				new Rectangle(n.x, n.y, getFigure().getPreferredSize().width,
						getFigure().getPreferredSize().height));

		for (int i = 0; i < getSourceConnections().size(); i++) {
			StateTransitionPart trans = (StateTransitionPart) getSourceConnections()
					.get(i);
			trans.applyGraphResults(graph, map);
		}
	}

	/**
	 * 
	 * 
	 * @param graph 
	 * @param map 
	 */
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			StateTransitionPart part = (StateTransitionPart) getSourceConnections()
					.get(i);
			part.contributeToGraph(graph, map);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart child = (AbstractStatePart) children.get(i);
			child.contributeEdgesToGraph(graph, map);
		}
	}

	/**
	 * 
	 * 
	 * @param graph 
	 * @param map 
	 * @param s 
	 */
	public abstract void contributeNodesToGraph(CompoundDirectedGraph graph,
			Subgraph s, Map map);

	/**
	 * 
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new StateNodeEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, null);
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new StateEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
				null);
	}

	/**
	 * 
	 */
	public void deactivate() {
		super.deactivate();
		((IWebflowModelElement) getModel()).removePropertyChangeListener(this);
	}

	/**
	 * 
	 * 
	 * @param key 
	 * 
	 * @return 
	 */
	public Object getAdapter(Class key) {
		if (IPropertySource.class == key) {
			if (getModel() instanceof IAdaptable) {
				return ((IAdaptable) getModel()).getAdapter(key);
			}
		}
		return super.getAdapter(key);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected List getModelSourceConnections() {
		if (getModel() instanceof ITransitionableFrom)
			return ((ITransitionableFrom) getModel()).getOutputTransitions();
		else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected List getModelTargetConnections() {
		if (getModel() instanceof ITransitionableTo)
			return ((ITransitionableTo) getModel()).getInputTransitions();
		else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * 
	 * 
	 * @param connection 
	 * 
	 * @return 
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		if (connectionAnchor == null)
			connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
		return connectionAnchor;
	}

	/**
	 * 
	 * 
	 * @param request 
	 * 
	 * @return 
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if (connectionAnchor == null)
			connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
		return connectionAnchor;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public IState getState() {
		return (IState) getModel();
	}

	/**
	 * 
	 * 
	 * @param connection 
	 * 
	 * @return 
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		if (connectionAnchor == null)
			connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
		return connectionAnchor;
	}

	/**
	 * 
	 * 
	 * @param request 
	 * 
	 * @return 
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		if (connectionAnchor == null)
			connectionAnchor = new TopOrBottomAnchor(getFigure(), 0);
		return connectionAnchor;
	}

	/**
	 * 
	 */
	protected void performDirectEdit() {
	}

	/**
	 * 
	 * 
	 * @param request 
	 */
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			performDirectEdit();
		}
		else if (request.getType().equals(RequestConstants.REQ_OPEN)) {
			IEditorPart editor = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			ActionRegistry actionRegistry = (ActionRegistry) editor
					.getAdapter(ActionRegistry.class);
			IAction action = actionRegistry
					.getAction(EditPropertiesAction.EDITPROPERTIES);
			if (action != null && action.isEnabled()) {
				action.run();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param evt 
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (IWebflowModelElement.ADD_CHILDREN.equals(prop)
				|| IWebflowModelElement.REMOVE_CHILDREN.equals(prop)) {
			refreshChildren();
		}
		else if (IWebflowModelElement.MOVE_CHILDREN.equals(prop)) {
			refreshChildren();
		}
		else if (IWebflowModelElement.INPUTS.equals(prop)) {
			refreshTargetConnections();
		}
		else if (IWebflowModelElement.OUTPUTS.equals(prop)) {
			refreshSourceConnections();
		}
		else if (IWebflowModelElement.PROPS.equals(prop)) {
			refreshVisuals();
		}
		// Causes Graph to re-layout
		((GraphicalEditPart) (getViewer().getContents())).getFigure()
				.revalidate();
	}

	/**
	 * 
	 * 
	 * @param figure 
	 */
	protected void setFigure(IFigure figure) {
		figure.getBounds().setSize(0, 0);
		super.setFigure(figure);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	protected List getModelChildren() {
		List children = new ArrayList();
		if (getModel() instanceof IState) {
			if (((IState) getState()).getEntryActions() != null) {
				children.addAll(((IState) getState()).getEntryActions()
						.getEntryActions());
			}

			onGetModelChildren(children);

			if (((IState) getState()).getExitActions() != null) {
				children.addAll(((IState) getState()).getExitActions()
						.getExitActions());
			}
			if (((IState) getState()).getExceptionHandlers() != null) {
				children.addAll(((IState) getState()).getExceptionHandlers());
			}
		}
		return children;
	}

	/**
	 * 
	 * 
	 * @param children 
	 */
	protected void onGetModelChildren(List children) {

	}
}