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
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.TransitionEditPolicy;

/**
 * 
 */
public class StateTransitionPart extends AbstractConnectionEditPart implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final Color COLOR = new Color(null, 255, 255, 206);

	/**
	 * 
	 */
	private PolylineConnection conn;

	/**
	 * 
	 */
	private Label label;

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
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
		conn = (PolylineConnection) getConnectionFigure();
		conn.setTargetDecoration(new PolygonDecoration());
	}

	/**
	 * 
	 * 
	 * @param graph 
	 * @param map 
	 */
	@SuppressWarnings("unchecked")
	public void contributeToGraph(CompoundDirectedGraph graph, Map map) {
		GraphAnimation.recordInitialState(getConnectionFigure());
		Node source = (Node) map.get(getSource());
		Node target = (Node) map.get(getTarget());
		Edge e = null;
		if (target.data != null && target.data instanceof AbstractStatePart) {
			IState startState = ((IWebflowState) ((AbstractStatePart) target.data)
					.getState().getElementParent()).getStartState();
			IState sourceState = ((AbstractStatePart) source.data).getState();
			IState targetState = ((AbstractStatePart) target.data).getState();
			if (startState.getId().equals(targetState.getId())) {
				e = new Edge(this, target, source);
			}
			else {
				List children = ((IWebflowState) ((AbstractStatePart) target.data)
						.getState().getElementParent()).getStates();
				int sourceIndex = children.indexOf(sourceState);
				int targetIndex = children.indexOf(targetState);
				if (targetIndex < sourceIndex) {
					e = new Edge(this, target, source);
				}
				else {
					e = new Edge(this, source, target);
				}
			}
		}
		else {
			List children = ((IWebflowState) ((AbstractStatePart) target.data)
					.getState().getElementParent()).getStates();
			int sourceIndex = children.indexOf(source);
			int targetIndex = children.indexOf(target);
			if (targetIndex < sourceIndex) {
				e = new Edge(this, target, source);
			}
			else {
				e = new Edge(this, source, target);
			}
		}
		if (getTransitionModel().getActions().size() > 0) {
			// e.weight = 3;
		}
		else {
			// e.weight = 3;
		}
		graph.edges.add(e);
		map.put(this, e);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE,
				new TransitionEditPolicy());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		PolylineConnection conn = (PolylineConnection) super.createFigure();
		if (getTransitionModel().getActions().size() > 0) {
			label = new Label();
			label.setBorder(new LineBorder());
			label.setBackgroundColor(COLOR);
			label.setOpaque(true);
			label.setIcon(WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_ACTION));
			conn.add(label, new ConnectionLocator(conn));
		}

		conn.setTargetDecoration(new PolygonDecoration());
		return conn;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	public void deactivate() {
		super.deactivate();
		((IWebflowModelElement) getModel()).removePropertyChangeListener(this);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public IStateTransition getTransitionModel() {
		return (IStateTransition) getModel();
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (IWebflowModelElement.ADD_CHILDREN.equals(prop)
				|| IWebflowModelElement.REMOVE_CHILDREN.equals(prop)
				|| IWebflowModelElement.MOVE_CHILDREN.equals(prop)) {
			refreshVisualsInternal();
		}
	}

	/**
	 * 
	 */
	public void refreshVisualsInternal() {
		if (getTransitionModel().getActions().size() > 0) {
			if (label != null && getFigure().getChildren().contains(label)) {
				getFigure().remove(label);
				label = null;
			}
			label = new Label();
			label.setBorder(new LineBorder());
			label.setBackgroundColor(COLOR);
			label.setOpaque(true);
			label.setIcon(WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_ACTION));
			/*
			 * if (getSelected() != EditPart.SELECTED_NONE) { ((LineBorder)
			 * label.getBorder()).setWidth(2); }
			 */
			if (conn != null) {
				conn.add(label, new ConnectionLocator(conn));
			}
		}
		else {
			if (label != null && getFigure().getChildren().contains(label)) {
				getFigure().remove(label);
				label = null;
			}
		}
		// TODO revalidate the viewer
		((GraphicalEditPart) (getViewer().getContents())).getFigure()
				.revalidate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#performRequest(org.eclipse.gef.Request)
	 */
	public void performRequest(Request request) {
		if (request.getType().equals(RequestConstants.REQ_OPEN)) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#setSelected(int)
	 */
	public void setSelected(int value) {
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE) {
			((PolylineConnection) getFigure()).setLineWidth(2);
			if (label != null) {
				((LineBorder) label.getBorder()).setWidth(2);
			}
		}
		else {
			((PolylineConnection) getFigure()).setLineWidth(1);
			if (label != null) {
				((LineBorder) label.getBorder()).setWidth(1);
			}
		}
	}
}