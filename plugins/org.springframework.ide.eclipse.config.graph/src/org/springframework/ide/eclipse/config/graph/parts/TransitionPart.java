/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.graph.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.springframework.ide.eclipse.config.graph.model.AbstractGefGraphModelElement;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.policies.TransitionEditPolicy;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class TransitionPart extends AbstractConnectionEditPart implements PropertyChangeListener {

	public TransitionPart(Transition model) {
		super();
		setModel(model);
	}

	@Override
	public void activate() {
		super.activate();
		((Transition) getModel()).addPropertyChangeListener(this);
	}

	protected void applyGraphResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		Edge e = (Edge) map.get(this);
		NodeList nodes = e.vNodes;
		PolylineConnection conn = (PolylineConnection) getConnectionFigure();

		if (nodes != null && !isManualLayout()) {
			List<AbsoluteBendpoint> bends = new ArrayList<AbsoluteBendpoint>();
			for (int i = 0; i < nodes.size(); i++) {
				Node vn = nodes.getNode(i);
				int x = vn.x;
				int y = vn.y;
				if (e.isFeedback()) {
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
					bends.add(new AbsoluteBendpoint(x, y));
				}
				else {
					bends.add(new AbsoluteBendpoint(x, y));
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
				}
			}
			conn.setRoutingConstraint(bends);
		}
		else {
			conn.setRoutingConstraint(Collections.EMPTY_LIST);
		}
	}

	protected void contributeToGraph(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		GraphAnimation.recordInitialState(getConnectionFigure());
		Node source = (Node) map.get(getSource());
		Node target = (Node) map.get(getTarget());
		Edge e = new Edge(this, source, target);
		graph.edges.add(e);
		map.put(this, e);
	}

	protected void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map<AbstractGraphicalEditPart, Object> map) {

	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new TransitionEditPolicy());
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		PolylineConnection conn = (PolylineConnection) super.createFigure();
		conn.setConnectionRouter(new BendpointConnectionRouter() {
			@Override
			public void route(Connection conn) {
				GraphAnimation.recordInitialState(conn);
				if (!GraphAnimation.playbackState(conn)) {
					super.route(conn);
				}
			}
		});
		if (((Transition) getModel()).isDirectional()) {
			conn.setTargetDecoration(new PolygonDecoration());
		}
		conn.setLineStyle(((Transition) getModel()).getLineStyle());
		conn.setForegroundColor(ColorConstants.gray);
		conn.setLineWidth(2);
		return conn;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		((Transition) getModel()).removePropertyChangeListener(this);
	}

	public boolean isManualLayout() {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			return diagramPart.isManualLayout();
		}
		return false;
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getPropertyName();
		if (AbstractGefGraphModelElement.LINESTYLE.equals(property)) {
			((PolylineConnection) getFigure()).setLineStyle(((Transition) getModel()).getLineStyle());
		}

	}

	/**
	 * @see org.eclipse.gef.EditPart#setSelected(int)
	 */
	@Override
	public void setSelected(int value) {
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE) {
			((PolylineConnection) getFigure()).setLineWidth(3);
		}
		else {
			((PolylineConnection) getFigure()).setLineWidth(2);
		}
	}

}
