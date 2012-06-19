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

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Leo Dos Santos
 */
public class FixedConnectionAnchor extends AbstractConnectionAnchor {

	public boolean leftToRight = true;

	public int offsetH;

	public int offsetV;

	public boolean topDown = true;

	private final String connectionAttr;

	public FixedConnectionAnchor(IFigure owner, String attr) {
		super(owner);
		this.connectionAttr = attr;
	}

	/**
	 * @see org.eclipse.draw2d.AbstractConnectionAnchor#ancestorMoved(IFigure)
	 */
	@Override
	public void ancestorMoved(IFigure figure) {
		if (figure instanceof ScalableFigure) {
			return;
		}
		super.ancestorMoved(figure);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof FixedConnectionAnchor) {
			FixedConnectionAnchor fa = (FixedConnectionAnchor) o;
			if (fa.leftToRight == this.leftToRight && fa.topDown == this.topDown && fa.offsetH == this.offsetH
					&& fa.offsetV == this.offsetV && fa.getOwner() == this.getOwner()) {
				return true;
			}
		}
		return false;
	}

	public String getConnectionLabel() {
		return connectionAttr;
	}

	public Point getLocation(Point reference) {
		Rectangle r = getOwner().getBounds();
		int x, y;
		if (topDown) {
			y = r.y + offsetV;
		}
		else {
			y = r.bottom() - 1 - offsetV;
		}

		if (leftToRight) {
			x = r.x + offsetH;
		}
		else {
			x = r.right() - 1 - offsetH;
		}

		Point p = new PrecisionPoint(x, y);
		getOwner().translateToAbsolute(p);
		return p;
	}

	@Override
	public Point getReferencePoint() {
		return getLocation(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((this.leftToRight ? 31 : 0) + (this.topDown ? 37 : 0) + this.offsetH * 43 + this.offsetV * 47)
				^ this.getOwner().hashCode();
	}

	/**
	 * @param offsetH The offsetH to set.
	 */
	public void setOffsetH(int offsetH) {
		this.offsetH = offsetH;
		fireAnchorMoved();
	}

	/**
	 * @param offsetV The offsetV to set.
	 */
	public void setOffsetV(int offsetV) {
		this.offsetV = offsetV;
		fireAnchorMoved();
	}

}
