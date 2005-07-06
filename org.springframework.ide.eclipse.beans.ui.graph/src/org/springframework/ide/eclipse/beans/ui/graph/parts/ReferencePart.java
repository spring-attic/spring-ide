/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.graph.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanReference;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.ConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.graph.model.Property;
import org.springframework.ide.eclipse.beans.ui.graph.model.Reference;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class ReferencePart extends AbstractConnectionEditPart {

	protected Reference getReference() {
		return (Reference) getModel();
	}

	protected IFigure createFigure() {
		PolylineConnection conn = createConnection(getReference());
		Label label = new Label();
		switch (getReference().getType()) {
			case BeanReference.PARENT_BEAN_TYPE :
				conn.setLineStyle(Graphics.LINE_DOT);
				label.setText("Parent bean: " +
							  getReference().getTargetBean().getName());
				break;

			case BeanReference.FACTORY_BEAN_TYPE :
				conn.setLineStyle(Graphics.LINE_DASH);
				label.setText("Factory bean");
				break;

			case BeanReference.DEPENDS_ON_BEAN_TYPE :
				conn.setLineStyle(Graphics.LINE_DASH);
				label.setText("Depends-on bean");
				break;

			case BeanReference.METHOD_OVERRIDE_BEAN_TYPE :
				conn.setLineStyle(Graphics.LINE_DOT);
				label.setText("Method-override bean");
				break;

			case BeanReference.INTERCEPTOR_BEAN_TYPE :
				conn.setLineStyle(Graphics.LINE_DASHDOT);
				label.setText("Interceptor bean");
				break;

			default :
				Node node = getReference().getNode();
				if (node instanceof ConstructorArgument) {
					label.setText("ConstructorArgument: " +
								  ((ConstructorArgument) node).getName());
				} else if (node instanceof Property) {
					label.setText("Property: " + ((Property) node).getName());
				}
				break;
		}
		conn.setToolTip(label);
		return conn;
	}

	protected void createEditPolicies() {
	}

	protected PolylineConnection createConnection(Edge edge) {
		PolylineConnection conn = new PolylineConnection();

		// Prepare connection router with corresponding bendpoints
		conn.setConnectionRouter(new BendpointConnectionRouter());
		List bends = new ArrayList();

		// Add bend point if source's bean prefered height is different from
		// heigth calculated by DirectedGraphLayout
		Bean source = (Bean) getReference().source;
		if (source.height > source.preferredHeight) {
			Rectangle rect = new Rectangle(source.x + GraphPart.MARGIN_SIZE,
										   source.y + GraphPart.MARGIN_SIZE,
										   source.width, source.height);
			bends.add(new AbsoluteBendpoint(rect.getBottom()));
		}

		// Create bend points for edge's virtual nodes (if any)
		NodeList nodes = edge.vNodes;
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.getNode(i);

				// Check if edge was inverted (due to broken cycle)
				if (edge.isFeedback) {
					bends.add(new AbsoluteBendpoint(
								node.x + GraphPart.MARGIN_SIZE,
								node.y + GraphPart.MARGIN_SIZE + node.height));
					bends.add(new AbsoluteBendpoint(
											  node.x + GraphPart.MARGIN_SIZE,
											  node.y + GraphPart.MARGIN_SIZE));
				} else {
					bends.add(new AbsoluteBendpoint(
											  node.x + GraphPart.MARGIN_SIZE,
											  node.y + GraphPart.MARGIN_SIZE));
					bends.add(new AbsoluteBendpoint(
								node.x + GraphPart.MARGIN_SIZE,
								node.y + GraphPart.MARGIN_SIZE + node.height));
				}
			}
		}
		conn.setRoutingConstraint(bends);

		conn.setTargetDecoration(new PolylineDecoration());
		return conn;
	}

	public void setSelected(int value) {
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE) {
			((PolylineConnection) getFigure()).setLineWidth(2);
		} else {
			((PolylineConnection) getFigure()).setLineWidth(1);
		}
	}

	/**
	 * Opens this property's config file on double click.
	 */
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			SpringUIUtils.openInEditor(getReference().getConfigFile(),
									  getReference().getStartLine());
		}
		super.performRequest(req);
	}
}
