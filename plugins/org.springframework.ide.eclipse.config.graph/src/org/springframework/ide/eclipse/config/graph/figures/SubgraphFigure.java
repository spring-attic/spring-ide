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
package org.springframework.ide.eclipse.config.graph.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.springframework.ide.eclipse.config.graph.parts.DummyLayout;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SubgraphFigure extends Figure {

	protected int direction;

	protected IFigure contents;

	protected IFigure footer;

	protected IFigure header;

	public SubgraphFigure(IFigure header, IFigure footer, int direction) {
		this.direction = direction;
		contents = new Figure();
		contents.setLayoutManager(new DummyLayout());
		add(this.header = header);
		add(contents);
		add(this.footer = footer);
	}

	public IFigure getContents() {
		return contents;
	}

	public IFigure getFooter() {
		return footer;
	}

	public IFigure getHeader() {
		return header;
	}

	/**
	 * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
	 */
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		int wHeader = getHeader().getPreferredSize().width;
		int wFooter = getFooter().getPreferredSize().width;
		dim.width = wHeader >= wFooter ? wHeader : wFooter;
		dim.width += getInsets().getWidth();
		dim.height = 50;
		return dim;
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		rect = Rectangle.SINGLETON;
		getClientArea(rect);
		contents.setBounds(rect);

		Dimension size = footer.getPreferredSize();
		footer.setLocation(rect.getBottomLeft().translate(0, -size.height));
		footer.setSize(size);

		size = header.getPreferredSize();
		header.setLocation(rect.getLocation());
		header.setSize(size);
	}

	public void setSelected(boolean value) {
	}

}
