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

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.CompoundStateFigure;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.SubgraphFigure;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.FlowStateLayoutEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateContainerEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateContainerHighlightEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateNodeEditPolicy;

/**
 * @author Christian Dupuis
 */
public abstract class ChildrenStatePart extends AbstractStatePart implements
		NodeEditPart {

	protected static ILabelProvider labelProvider = new DecoratingLabelProvider(
			new WebflowModelLabelProvider(), new WebflowModelLabelDecorator());

	protected WebflowModelLabelProvider eLabelProvider = new WebflowModelLabelProvider();

	protected void applyChildrenResults(CompoundDirectedGraph graph, Map map) {

		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			part.applyGraphResults(graph, map);
		}
	}

	protected void applyMaxWidths() {
		int width = 0;
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			if (part.getModel() instanceof IActionElement
					|| part.getModel() instanceof IExceptionHandler
					|| part.getModel() instanceof IAttributeMapper) {
				Rectangle rec = part.getFigure().getBounds();
				if (rec.width >= width) {
					width = rec.width;
				}
			}
		}
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);
			if (part.getModel() instanceof IActionElement
					|| part.getModel() instanceof IExceptionHandler
					|| part.getModel() instanceof IAttributeMapper) {
				Rectangle rect = part.getFigure().getBounds();
				rect.width = width;
				part.getFigure().setBounds(rect);
			}
		}
	}

	protected void applyChildrenResultsToOwn(CompoundDirectedGraph graph,
			Map map) {
		Node n = (Node) map.get(this);
		int bottom = -1;
		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart part = (AbstractStatePart) getChildren().get(i);

			if (part.getFigure().getBounds().bottom() > bottom) {
				bottom = part.getFigure().getBounds().getBottom().y;
			}
		}
		if (bottom > -1) {
			int top = getFigure().getBounds().getTop().y;
			int height = bottom - top + 7 + 7;
			getFigure().setBounds(new Rectangle(n.x, n.y, n.width, height));
		}
	}

	protected void applyGraphResults(CompoundDirectedGraph graph, Map map) {
		applyOwnResults(graph, map);
		applyChildrenResults(graph, map);
		applyChildrenResultsToOwn(graph, map);
	}

	protected void applyOwnResults(CompoundDirectedGraph graph, Map map) {
		Node n = (Node) map.get(this);

		if (getModelChildren() != null && getModelChildren().size() > 0) {
			getFigure().setBounds(new Rectangle(n.x, n.y, n.width, n.height));
		}
		else {
			int height = ((CompoundStateFigure) getFigure()).getHeader()
					.getPreferredSize().height;
			if (height < 20) {
				getFigure().setBounds(new Rectangle(n.x, n.y, n.width, 23));
			}
			else {
				getFigure().setBounds(new Rectangle(n.x, n.y, n.width, 33));
			}
		}

		for (int i = 0; i < getSourceConnections().size(); i++) {
			StateTransitionPart trans = (StateTransitionPart) getSourceConnections()
					.get(i);
			trans.applyGraphResults(graph, map);
		}
	}

	@SuppressWarnings("unchecked")
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map map) {
		GraphAnimation.recordInitialState(getContentPane());
		Subgraph me = new Subgraph(this, s);
		IState state = (IState) getModel();

		if (!(state instanceof IWebflowState)) {
			int index = -1;
			int stateCount = ((IWebflowState) state.getElementParent())
					.getStates().size();
			if (state instanceof IInlineFlowState) {
				index = ((IWebflowState) state.getElementParent())
						.getInlineFlowStates().indexOf(state)
						+ stateCount;
			}
			else {
				index = ((IWebflowState) state.getElementParent()).getStates()
						.indexOf(state);
			}
			me.setRowConstraint(index);
		}

		me.outgoingOffset = 5;
		me.incomingOffset = 5;
		IFigure fig = getFigure();
		if (fig instanceof SubgraphFigure) {
			me.width = fig.getPreferredSize(me.width, me.height).width;
			me.height = fig.getPreferredSize().height;

			me.insets.top = ((CompoundStateFigure) getFigure()).getHeader()
					.getPreferredSize().height + 5;
			me.insets.left = 5;
			me.insets.right = 5;
			me.insets.bottom = 15;
		}
		me.innerPadding = new Insets(0, 0, 0, 4);
		// me.setPadding(new Insets(10, 50, 50, 0));
		map.put(this, me);
		graph.nodes.add(me);

		for (int i = 0; i < getChildren().size(); i++) {
			AbstractStatePart activity = (AbstractStatePart) getChildren().get(
					i);
			activity.contributeNodesToGraph(graph, me, map);
		}
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new StateNodeEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new StateEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				new StateContainerHighlightEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE,
				new StateContainerEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new FlowStateLayoutEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
	}

	protected IFigure createFigure() {
		CompoundStateFigure figure = new CompoundStateFigure((IWebflowModelElement) getModel());
		((Label) figure.getHeader())
				.setIcon(labelProvider.getImage(getModel()));
		((Label) figure.getHeader()).setIconTextGap(5);
		((Label) figure.getHeader()).setIconAlignment(PositionConstants.TOP);
		return figure;
	}

	private boolean directEditHitTest(Point requestLoc) {
		return false;
	}

	public IFigure getContentPane() {
		if (getFigure() instanceof SubgraphFigure)
			return ((SubgraphFigure) getFigure()).getContents();
		return getFigure();
	}

	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
			if (request instanceof DirectEditRequest
					&& !directEditHitTest(((DirectEditRequest) request)
							.getLocation().getCopy()))
				return;
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

	protected void refreshVisuals() {
		((Label) ((SubgraphFigure) getFigure()).getHeader())
				.setText(labelProvider.getText(getModel()));
		((Label) ((SubgraphFigure) getFigure()).getHeader())
				.setIcon(labelProvider.getImage(getModel()));
		((Label) ((SubgraphFigure) getFigure()).getHeader()).setIconTextGap(5);
		((Label) ((SubgraphFigure) getFigure()).getHeader())
				.setToolTip(new Label(eLabelProvider.getText(getModel(), true, true, true)));
		getFigure().repaint();

	}

	public void setSelected(int value) {
		super.setSelected(value);
		if (!(getModel() instanceof IWebflowState)
				|| getModel() instanceof ISubflowState) {
			SubgraphFigure sf = (SubgraphFigure) getFigure();
			sf.setSelected(value != SELECTED_NONE);
		}
	}
}
