/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.StateLabel;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * 
 */
public class IfPart extends AbstractStatePart {

	/**
	 * 
	 */
	public static final Color COLOR = new Color(null, 255, 255, 206);

	/**
	 * 
	 */
	private static WebflowModelLabelProvider labelProvider = new WebflowModelLabelProvider();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeEdgesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * java.util.Map)
	 */
	public void contributeEdgesToGraph(CompoundDirectedGraph graph, Map map) {
		List outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			IfTransitionPart part = (IfTransitionPart) getSourceConnections()
					.get(i);
			part.contributeToGraph(graph, map);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeNodesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * org.eclipse.draw2d.graph.Subgraph, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map map) {
		Node n = new Node(this, s);
		n.outgoingOffset = 9;
		n.incomingOffset = 9;
		n.width = getFigure().getPreferredSize().width + 5;
		n.height = getFigure().getPreferredSize().height;
		n.setPadding(new Insets(0, 5, 5, 9));
		map.put(this, n);
		graph.nodes.add(n);

	}

	/**
	 * @return
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		Label l = new StateLabel();
		l.setBackgroundColor(COLOR);
		l.setLabelAlignment(PositionConstants.LEFT);
		l.setIcon(labelProvider.getImage(getModel()));
		l.setBorder(new LineBorder());
		return l;
	}

	/**
	 * @return
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	protected List getModelSourceConnections() {
		if (getModel() instanceof IIf) {
			List<IWebflowModelElement> sourceConnections = new ArrayList<IWebflowModelElement>();
			IIf theIf = (IIf) getModel();
			if (theIf.getThenTransition() != null
					&& theIf.getThenTransition().getToState() != null) {
				sourceConnections.add(theIf.getThenTransition());
			}
			if (theIf.getElseTransition() != null
					&& theIf.getElseTransition().getToState() != null) {
				sourceConnections.add(theIf.getElseTransition());
			}
			return sourceConnections;
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#performDirectEdit()
	 */
	protected void performDirectEdit() {
		/*
		 * if (manager == null) { Label l = (Label) getFigure(); manager = new
		 * StateDirectEditManager(this, TextCellEditor.class, new
		 * StateCellEditorLocator(l), l); } manager.show();
		 */
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		((Label) getFigure()).setText(labelProvider.getText(getModel()));
		((Label) getFigure()).setToolTip(new Label(labelProvider
				.getLongText(getModel())));
	}
}
