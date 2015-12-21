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

import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.jface.viewers.TextCellEditor;
import org.springframework.ide.eclipse.config.graph.figures.ParallelActivityFigure;
import org.springframework.ide.eclipse.config.graph.figures.SequentialActivityFigure;
import org.springframework.ide.eclipse.config.graph.figures.SubgraphFigure;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.policies.ActivityContainerHighlightEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.ActivityEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.ActivityNodeEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.StructuredActivityDirectEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.StructuredActivityLayoutEditPolicy;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class StructuredActivityPart extends ActivityPart implements NodeEditPart {

	public StructuredActivityPart(StructuredActivity activity) {
		super(activity);
	}

	protected void applyChildrenResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart part = (ActivityPart) getChildren().get(i);
			part.applyGraphResults(graph, map);
		}
	}

	@Override
	protected void applyGraphResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		applyOwnResults(graph, map);
		applyChildrenResults(graph, map);
	}

	protected void applyOwnResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		super.applyGraphResults(graph, map);
	}

	@Override
	protected void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map<AbstractGraphicalEditPart, Object> map) {
		GraphAnimation.recordInitialState(getContentPane());
		Subgraph me = new Subgraph(this, s);
		// me.setRowConstraint(getActivity().getSortIndex());
		me.outgoingOffset = 5;
		me.incomingOffset = 5;
		IFigure fig = getFigure();
		if (fig instanceof SubgraphFigure) {
			if (graph.getDirection() == PositionConstants.EAST) {
				if (fig instanceof ParallelActivityFigure) {
					me.width = fig.getPreferredSize(me.width, me.height).width;
					int tagHeight = ((SubgraphFigure) fig).getHeader().getPreferredSize().height;
					int tagWidth = ((SubgraphFigure) fig).getHeader().getPreferredSize().width;
					me.insets.left = tagHeight;
					me.insets.top = tagWidth;
					me.insets.right = tagHeight;
				}
				else if (fig instanceof SequentialActivityFigure) {
					me.height = fig.getPreferredSize(me.width, me.height).height;
					int tagWidth = ((SubgraphFigure) fig).getHeader().getPreferredSize().width;
					me.insets.top = tagWidth;
					me.insets.left = 0;
					me.insets.bottom = tagWidth;
				}
			}
			else {
				me.width = fig.getPreferredSize(me.width, me.height).width;
				int tagHeight = ((SubgraphFigure) fig).getHeader().getPreferredSize().height;
				me.insets.top = tagHeight;
				me.insets.left = 0;
				me.insets.bottom = tagHeight;
			}
		}
		me.innerPadding = new Insets(0);
		if (graph.getDirection() == PositionConstants.EAST) {
			me.setPadding(new Insets(6, 8, 6, 8));
		}
		else {
			me.setPadding(new Insets(8, 6, 8, 6));
		}
		map.put(this, me);
		graph.nodes.add(me);

		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart activity = (ActivityPart) getChildren().get(i);
			activity.contributeNodesToGraph(graph, me, map);
		}
		for (int i = 0; i < getSourceConnections().size(); i++) {
			TransitionPart trans = (TransitionPart) getSourceConnections().get(i);
			Subgraph sub = (Subgraph) map.get(getViewer().getContents());
			trans.contributeNodesToGraph(graph, s, map);
		}
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.ActivityPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ActivityEditPolicy());
		// installEditPolicy(EditPolicy.CONTAINER_ROLE, new
		// ActivityContainerEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new StructuredActivityDirectEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActivityNodeEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new StructuredActivityLayoutEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new ActivityContainerHighlightEditPolicy());
	}

	private boolean directEditHitTest(Point requestLoc) {
		IFigure header = ((SubgraphFigure) getFigure()).getHeader();
		header.translateToRelative(requestLoc);
		if (header.containsPoint(requestLoc)) {
			return true;
		}
		return false;
	}

	@Override
	protected int getAnchorOffset() {
		return -1;
	}

	@Override
	public IFigure getContentPane() {
		if (getFigure() instanceof SubgraphFigure) {
			return ((SubgraphFigure) getFigure()).getContents();
		}
		return getFigure();
	}

	@Override
	protected List<Activity> getModelChildren() {
		return getStructuredActivity().getChildren();
	}

	StructuredActivity getStructuredActivity() {
		return (StructuredActivity) getModel();
	}

	/**
	 * @see org.springframework.ide.eclipse.config.graph.parts.ActivityPart#performDirectEdit()
	 */
	@Override
	protected void performDirectEdit() {
		if (manager == null) {
			Label l = ((Label) ((SubgraphFigure) getFigure()).getHeader());
			manager = new ActivityDirectEditManager(this, TextCellEditor.class, new ActivityCellEditorLocator(l), l);
		}
		manager.show();
	}

	/**
	 * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
	 */
	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			if (request instanceof DirectEditRequest
					&& !directEditHitTest(((DirectEditRequest) request).getLocation().getCopy())) {
				return;
			}
			performDirectEdit();
		}
		else if (request.getType() == RequestConstants.REQ_OPEN) {
			performOpen();
		}
	}

	@Override
	protected void refreshFigureVisuals() {
		((Label) ((SubgraphFigure) getFigure()).getHeader()).setText(getActivity().getName());
		((Label) ((SubgraphFigure) getFigure()).getFooter()).setText("/" + getActivity().getName()); //$NON-NLS-1$
	}

}
