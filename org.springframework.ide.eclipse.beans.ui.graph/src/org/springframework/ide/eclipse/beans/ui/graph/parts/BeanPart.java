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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.springframework.ide.eclipse.beans.ui.graph.figures.BeanFigure;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class BeanPart extends AbstractGraphicalEditPart implements NodeEditPart {

	public Bean getBean() {
		return (Bean) getModel();
	}

	protected IFigure createFigure() {
		Bean bean = getBean();
		BeanFigure figure = new BeanFigure(bean);
		return figure;
	}

	protected void createEditPolicies() {
	}

	/**
	 * Sets constraint for XYLayout (rectangle with figure bounds from bean). 
	 */
	protected void refreshVisuals() {
		Dimension dim = getFigure().getPreferredSize();
		Rectangle rect = new Rectangle(getBean().x, getBean().y, dim.width,
									dim.height);
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(),
															  rect);
	}

	protected List getModelSourceConnections() {
		return getBean().outgoing;
	}

	protected List getModelTargetConnections() {
		return getBean().incoming;
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart conn) {
		return new TopOrBottomAnchor(getFigure());
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new TopOrBottomAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart conn) {
		return new TopOrBottomAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new TopOrBottomAnchor(getFigure());
	}

	public void setSelected(int value) {
		super.setSelected(value);
		Border border = getFigure().getBorder();
		if (border instanceof LineBorder) {
			if (value != EditPart.SELECTED_NONE) {
				((LineBorder) border).setWidth(2);
			} else {
				((LineBorder) border).setWidth(1);
			}
			getFigure().repaint();
		}
	}

	/**
	 * Opens this bean's config file on double click.
	 */
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			IFile file = getBean().getConfigFile();
			if (file != null && file.exists()) {
				SpringUIUtils.openInEditor(file, getBean().getStartLine());
			}
		}
		super.performRequest(req);
	}

	protected static class TopOrBottomAnchor extends ChopboxAnchor {

		public TopOrBottomAnchor(IFigure owner) {
			super(owner);
		}

		public Point getLocation(Point reference) {
			Point p = getOwner().getBounds().getCenter();
			getOwner().translateToAbsolute(p);
			if (reference.y < p.y) {
				p = getOwner().getBounds().getTop();
			} else {
				p = getOwner().getBounds().getBottom();
			}
			getOwner().translateToAbsolute(p);
			return p;
		}
	}
}
