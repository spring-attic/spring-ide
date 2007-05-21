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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.swt.SWT;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.FlowEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.FlowStateLayoutEditPolicy;
import org.springframework.ide.eclipse.webflow.ui.graph.policies.StateContainerEditPolicy;

public class FlowPart extends ChildrenStatePart implements
		PropertyChangeListener {

	private static final String CONNECTION_LAYER = "Connection Layer";

	//private Label flowLabel;

	private AnimatedFanConnectionRouter fanRouter = null;

	private ShortestPathConnectionRouter router = null;

	private CommandStackListener stackListener = new CommandStackListener() {

		public void commandStackChanged(EventObject event) {
			if (!GraphAnimation.captureLayout(getFigure()))
				return;
			while (GraphAnimation.step())
				getFigure().getUpdateManager().performUpdate();
			GraphAnimation.end();
		}
	};

	public void activate() {
		super.activate();
		getViewer().getEditDomain().getCommandStack().addCommandStackListener(
				stackListener);
	}

	protected void applyOwnResults(CompoundDirectedGraph graph, Map map) {
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.NODE_ROLE, null);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new FlowEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new FlowStateLayoutEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE,
				new StateContainerEditPolicy());
	}

	protected IFigure createFigure() {
		Figure f = new Figure() {

			public void setBounds(Rectangle rect) {
				int x = bounds.x, y = bounds.y;

				boolean resize = (rect.width != bounds.width)
						|| (rect.height != bounds.height), translate = (rect.x != x)
						|| (rect.y != y);

				if (isVisible() && (resize || translate))
					erase();
				if (translate) {
					int dx = rect.x - x;
					int dy = rect.y - y;
					primTranslate(dx, dy);
				}
				bounds.width = rect.width;
				bounds.height = rect.height;
				if (resize || translate) {
					fireFigureMoved();
					repaint();
				}
			}
		};

		f.setLayoutManager(new GraphLayoutManager(this));

		//addFlowRootLabel(f);

		return f;
	}

	/*private void addFlowRootLabel(IFigure f) {

		IWebflowState webflowState = (IWebflowState) getModel();
		IWebflowConfig webflowConfig = (IWebflowConfig) webflowState
				.getElementParent();

		StringBuilder labelTextBuilder = new StringBuilder();

		if (webflowState.getOutputMapper() != null) {
			List<IMapping> mapping = webflowState.getOutputMapper()
					.getMapping();
		}
		else {
			labelTextBuilder.append("void ");
		}
		labelTextBuilder.append(webflowConfig.getName());
		labelTextBuilder.append("(");
		if (webflowState.getInputMapper() != null) {
			List<IInputAttribute> inputAttributes = webflowState
					.getInputMapper().getInputAttributes();
			for (IInputAttribute inputAttribute : inputAttributes) {
				String source = inputAttribute.getName();
				labelTextBuilder.append(source);
				labelTextBuilder.append(", ");
			}
			List<IMapping> mappings = webflowState.getInputMapper()
					.getMapping();
			for (IMapping mapping : mappings) {
				String source = mapping.getSource();
				String type = mapping.getFrom();
				if (type != null) {
					labelTextBuilder.append(type);
					labelTextBuilder.append(" ");
				}
				labelTextBuilder.append(source);
				labelTextBuilder.append(", ");
			}
		}

		String labelText = labelTextBuilder.toString();
		if (labelText.endsWith(", ")) {
			labelText = labelText.substring(0, labelText.length() - 2);
		}
		labelTextBuilder = new StringBuilder(labelText);
		labelTextBuilder.append(")");

		if (flowLabel == null) {

			flowLabel = new Label(labelTextBuilder.toString(), WebflowUIImages
					.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW));

			flowLabel.setLabelAlignment(PositionConstants.LEFT);
			flowLabel.setFont(JFaceResources
					.getFont(JFaceResources.DEFAULT_FONT));

			f.add(flowLabel);
		}
		else {
			flowLabel.setText(labelTextBuilder.toString());
		}
		
		Rectangle rec = flowLabel.getBounds();
		rec.x = 5;
		rec.y = 5;
		rec.height = flowLabel.getPreferredSize().height;
		rec.width = flowLabel.getPreferredSize().width;
		flowLabel.setBounds(rec);
	}*/

	public void deactivate() {
		getViewer().getEditDomain().getCommandStack()
				.removeCommandStackListener(stackListener);
		super.deactivate();
	}

	protected List getModelChildren() {
		if (((IWebflowState) getState()).getStates() != null) {
			List<IState> states = new ArrayList<IState>();
			states.addAll(((IWebflowState) getState()).getStates());
			states.addAll(((IWebflowState) getState()).getInlineFlowStates());
			return states;
		}
		else {
			return Collections.EMPTY_LIST;
		}
	}

	public boolean isSelectable() {
		return true;
	}

	protected void refreshVisuals() {

		ConnectionLayer cLayer = (ConnectionLayer) getLayer(CONNECTION_LAYER);
		cLayer.setAntialias(SWT.ON);
		if (fanRouter == null && router == null) {
			fanRouter = new AnimatedFanConnectionRouter();
			fanRouter.setSeparation(20);
			router = new ShortestPathConnectionRouter(getFigure());
		}
		
		//addFlowRootLabel(getFigure());

		fanRouter.setNextRouter(router);
		cLayer.setConnectionRouter(fanRouter);
	}
}
