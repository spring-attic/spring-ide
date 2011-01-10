/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
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
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.TransitionEditPolicy;

/**
 * @author Christian Dupuis
 */
public class IfTransitionPart extends AbstractConnectionEditPart implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private PolylineConnection conn;

	/**
	 * 
	 */
	private Label label;

	/**
	 * 
	 * 
	 * @see org.eclipse.gef.EditPart#activate()
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
			IIf sourceState = (IIf) ((AbstractStatePart) source.data)
					.getModel();
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

		graph.edges.add(e);
		map.put(this, e);
	}

	/**
	 * 
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE,
				new TransitionEditPolicy());
	}

	/**
	 * 
	 * 
	 * @return 
	 * 
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		PolylineConnection conn = (PolylineConnection) super.createFigure();
		conn.setTargetDecoration(new PolygonDecoration());
		if (!getTransitionModel().isThen()) {
			conn.setLineStyle(Graphics.LINE_DASH);
			conn.setBackgroundColor(ColorConstants.lightGray);
			conn.setForegroundColor(ColorConstants.lightGray);
		}
		return conn;
	}

	/**
	 * 
	 * 
	 * @see org.eclipse.gef.EditPart#deactivate()
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
	public IIfTransition getTransitionModel() {
		return (IIfTransition) getModel();
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	public void refreshVisuals() {
		if (!getTransitionModel().isThen() && conn != null) {
			conn.setLineStyle(Graphics.LINE_DOT);
		}
	}

	/**
	 * 
	 * 
	 * @param value 
	 * 
	 * @see org.eclipse.gef.EditPart#setSelected(int)
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

}
