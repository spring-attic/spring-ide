/*******************************************************************************
 * Copyright (c) 2004, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.parts;

import java.util.List;

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
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.graph.figures.BeanFigure;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeanPart extends AbstractGraphicalEditPart implements NodeEditPart {

	public Bean getBean() {
		return (Bean) getModel();
	}

	@Override
	protected IFigure createFigure() {
		Bean bean = getBean();
		BeanFigure figure = new BeanFigure(bean);
		return figure;
	}

	@Override
	protected void createEditPolicies() {
	}

	/**
	 * Sets constraint for XYLayout (rectangle with figure bounds from bean).
	 */
	@Override
	protected void refreshVisuals() {
		Dimension dim = getFigure().getPreferredSize();
		Rectangle rect = null;
		if (getBean().getInnerBeans().length > 0) {
			rect = new Rectangle(getBean().x, getBean().y, dim.width + 3, dim.height);
		}
		else {
			rect = new Rectangle(getBean().x, getBean().y, dim.width + 3, dim.height);
		}
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), rect);
		getFigure().setBounds(rect);
	}

	@Override
	protected List getModelSourceConnections() {
		return getBean().outgoing;
	}

	@Override
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

	@Override
	public void setSelected(int value) {
		super.setSelected(value);
		Border border = getFigure().getBorder();
		if (border instanceof LineBorder) {
			if (value != EditPart.SELECTED_NONE) {
				((LineBorder) border).setWidth(2);
			}
			else {
				((LineBorder) border).setWidth(1);
			}
			getFigure().repaint();
		}
	}

	/**
	 * Opens this bean's config file or corresponding java element on double click.
	 */
	@Override
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			if (BeansUIUtils.shouldOpenConfigFile()) {
				BeansUIUtils.openInEditor(getBean().getBean());
			}
			else {
				IType type = BeansModelUtils.getBeanType(getBean().getBean(), null);
				if (type != null) {
					SpringUIUtils.openInEditor(type);
				}
			}
		}
		super.performRequest(req);
	}

	protected static class TopOrBottomAnchor extends ChopboxAnchor {

		public TopOrBottomAnchor(IFigure owner) {
			super(owner);
		}

		@Override
		public Point getLocation(Point reference) {
			Point p = getOwner().getBounds().getCenter();
			getOwner().translateToAbsolute(p);
			if (reference.y < p.y) {
				p = getOwner().getBounds().getTop();
			}
			else {
				p = getOwner().getBounds().getBottom();
			}
			getOwner().translateToAbsolute(p);
			return p;
		}
	}
}
