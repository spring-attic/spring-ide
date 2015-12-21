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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springframework.ide.eclipse.config.graph.model.AbstractGefGraphModelElement;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.policies.ActivityDirectEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.ActivityEditPolicy;
import org.springframework.ide.eclipse.config.graph.policies.ActivityNodeEditPolicy;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class ActivityPart extends AbstractGraphicalEditPart implements PropertyChangeListener, NodeEditPart {

	protected ActivityDirectEditManager manager;

	public ActivityPart(Activity activity) {
		setModel(activity);
	}

	/**
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		getActivity().addPropertyChangeListener(this);
	}

	protected void applyBoundsResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		Node n = (Node) map.get(this);
		getFigure().setBounds(new Rectangle(n.x, n.y, n.width, n.height));
	}

	protected void applyGraphResults(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		applyBoundsResults(graph, map);
		for (int i = 0; i < getSourceConnections().size(); i++) {
			TransitionPart trans = (TransitionPart) getSourceConnections().get(i);
			trans.applyGraphResults(graph, map);
		}
	}

	protected void contributeEdgesToGraph(CompoundDirectedGraph graph, Map<AbstractGraphicalEditPart, Object> map) {
		for (int i = 0; i < getSourceConnections().size(); i++) {
			TransitionPart part = (TransitionPart) getSourceConnections().get(i);
			part.contributeToGraph(graph, map);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			ActivityPart child = (ActivityPart) getChildren().get(i);
			child.contributeEdgesToGraph(graph, map);
		}
	}

	protected abstract void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map<AbstractGraphicalEditPart, Object> map);

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ActivityEditPolicy());
		// installEditPolicy(EditPolicy.CONTAINER_ROLE, new
		// ActivitySourceEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ActivityDirectEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ActivityNodeEditPolicy());
	}

	/**
	 * @see org.eclipse.gef.EditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		getActivity().removePropertyChangeListener(this);
	}

	/**
	 * Returns the Activity model associated with this EditPart
	 * @return the Activity model
	 */
	protected Activity getActivity() {
		return (Activity) getModel();
	}

	protected abstract int getAnchorOffset();

	public abstract Activity getModelElement();

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	@Override
	protected List<Transition> getModelSourceConnections() {
		return getActivity().getOutgoingTransitions();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
	 */
	@Override
	protected List<Transition> getModelTargetConnections() {
		return getActivity().getIncomingTransitions();
	}

	/**
	 * @see NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagram = (ActivityDiagramPart) part;
			if (diagram.getDirection() == PositionConstants.EAST) {
				return new RightAnchor(getFigure(), getAnchorOffset());
			}
		}
		return new BottomAnchor(getFigure(), getAnchorOffset());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagram = (ActivityDiagramPart) part;
			if (diagram.getDirection() == PositionConstants.EAST) {
				return new RightAnchor(getFigure(), getAnchorOffset());
			}
		}
		return new BottomAnchor(getFigure(), getAnchorOffset());
	}

	/**
	 * @see NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagram = (ActivityDiagramPart) part;
			if (diagram.getDirection() == PositionConstants.EAST) {
				return new LeftAnchor(getFigure(), getAnchorOffset());
			}
		}
		return new TopAnchor(getFigure(), getAnchorOffset());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagram = (ActivityDiagramPart) part;
			if (diagram.getDirection() == PositionConstants.EAST) {
				return new LeftAnchor(getFigure(), getAnchorOffset());
			}
		}
		return new TopAnchor(getFigure(), getAnchorOffset());
	}

	protected void handleBoundsChange(PropertyChangeEvent event) {
		IFigure figure = getFigure();
		Rectangle constraint = (Rectangle) event.getNewValue();
		ActivityDiagramPart diagram = (ActivityDiagramPart) getParent();
		diagram.setLayoutConstraint(this, figure, constraint);
	}

	public boolean isManualLayout() {
		EditPart part = getViewer().getContents();
		if (part instanceof ActivityDiagramPart) {
			ActivityDiagramPart diagramPart = (ActivityDiagramPart) part;
			return diagramPart.isManualLayout();
		}
		return false;
	}

	protected void performDirectEdit() {
	}

	protected void performOpen() {
		// IDOMElement element = getActivity().getInput();
		// if (element != null) {
		// AbstractConfigFlowDiagram diagram = getActivity().getDiagram();
		// IConfigEditor cEditor = diagram.getGraphicalEditor().getEditor();
		// if (cEditor != null) {
		// cEditor.revealElement(element);
		// }
		// }
		showProperties();
	}

	/**
	 * @see org.eclipse.gef.EditPart#performRequest(org.eclipse.gef.Request)
	 */
	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT && getEditPolicy(EditPolicy.DIRECT_EDIT_ROLE) != null) {
			performDirectEdit();
		}
		if (request.getType() == RequestConstants.REQ_OPEN) {
			performOpen();
		}
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (AbstractGefGraphModelElement.CHILDREN.equals(prop)) {
			refreshChildren();
		}
		else if (AbstractGefGraphModelElement.INCOMINGS.equals(prop)) {
			refreshTargetConnections();
		}
		else if (AbstractGefGraphModelElement.OUTGOINGS.equals(prop)) {
			refreshSourceConnections();
		}
		else if (Activity.NAME.equals(prop)) {
			refreshVisuals();
		}
		else if (Activity.BOUNDS.equals(prop)) {
			handleBoundsChange(evt);
		}

		// Causes Graph to re-layout
		((GraphicalEditPart) (getViewer().getContents())).getFigure().revalidate();
	}

	public void refreshAll() {
		refresh();
		List children = getChildren();
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof ActivityPart) {
				ActivityPart child = (ActivityPart) children.get(i);
				child.refreshAll();
			}
		}
		List sources = getSourceConnections();
		for (int i = 0; i < sources.size(); i++) {
			if (sources.get(i) instanceof TransitionPart) {
				TransitionPart trans = (TransitionPart) sources.get(i);
				trans.refresh();
			}
		}
		List targets = getTargetConnections();
		for (int i = 0; i < targets.size(); i++) {
			if (targets.get(i) instanceof TransitionPart) {
				TransitionPart trans = (TransitionPart) targets.get(i);
				trans.refresh();
			}
		}
	}

	protected abstract void refreshFigureVisuals();

	protected void refreshTooltipVisuals() {
		if (getFigure() != null) {
			Label tooltip = (Label) getFigure().getToolTip();
			if (tooltip == null) {
				tooltip = new Label();
				getFigure().setToolTip(tooltip);
			}
			tooltip.setText(getActivity().getName());
		}
	}

	@Override
	protected void refreshVisuals() {
		refreshFigureVisuals();
		refreshTooltipVisuals();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#setFigure(org.eclipse.draw2d.IFigure)
	 */
	@Override
	protected void setFigure(IFigure figure) {
		figure.getBounds().setSize(0, 0);
		super.setFigure(figure);
	}

	public void showProperties() {
		try {
			IViewPart props = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
			if (props instanceof ISelectionListener) {
				IConfigEditor editor = getModelElement().getDiagram().getGraphicalEditor().getEditor();
				Object obj = editor.getAdapter(ISelectionProvider.class);
				if (obj instanceof ISelectionProvider) {
					ISelectionProvider provider = (ISelectionProvider) obj;
					((ISelectionListener) props).selectionChanged(editor, provider.getSelection());
				}
			}
		}
		catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
					Messages.StructuredActivityPart_ERROR_OPENING_VIEW, e));
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#toString()
	 */
	@Override
	public String toString() {
		return getModel().toString();
	}

}
