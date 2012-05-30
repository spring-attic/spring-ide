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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.springframework.ide.eclipse.config.graph.parts.FixedConnectionAnchor;


/**
 * @author Leo Dos Santos
 */
public class BorderedActivityLabel extends SimpleActivityLabel {

	protected final int direction;

	protected final List<String> inputs;

	protected final List<String> outputs;

	protected Hashtable<String, FixedConnectionAnchor> connectionAnchors = new Hashtable<String, FixedConnectionAnchor>();

	protected Vector<FixedConnectionAnchor> inputConnectionAnchors = new Vector<FixedConnectionAnchor>();

	protected Vector<FixedConnectionAnchor> outputConnectionAnchors = new Vector<FixedConnectionAnchor>();

	protected ConnectorBorder border;

	public BorderedActivityLabel(int direction, List<String> inputs, List<String> outputs) {
		super(direction);
		this.direction = direction;
		this.inputs = inputs;
		this.outputs = outputs;
		border = createConnectorBorder();
		setBorder(border);
		createConnectionAnchors();
	}

	@Override
	protected Dimension calculateLabelSize(Dimension txtSize) {
		int gap = getIconTextGap();
		Dimension d = new Dimension(0, 0);
		if (getTextPlacement() == WEST || getTextPlacement() == EAST) {
			d.width = getIconSize().width + gap + txtSize.width;
			d.height = Math.max(getIconSize().height, txtSize.height);
		}
		else {
			d.width = Math.max(getIconSize().width, txtSize.width);
			d.height = getIconSize().height + gap + txtSize.height;
		}
		return d;
	}

	public ConnectionAnchor connectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration<FixedConnectionAnchor> e = getSourceConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		e = getTargetConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	protected void createConnectionAnchors() {
		for (int i = 0; i < inputs.size(); i++) {
			FixedConnectionAnchor in = new FixedConnectionAnchor(this, inputs.get(i));
			connectionAnchors.put(in.getConnectionLabel(), in);
			inputConnectionAnchors.addElement(in);
		}
		for (int i = 0; i < outputs.size(); i++) {
			FixedConnectionAnchor out = new FixedConnectionAnchor(this, outputs.get(i));
			connectionAnchors.put(out.getConnectionLabel(), out);
			outputConnectionAnchors.addElement(out);
		}
	}

	protected ConnectorBorder createConnectorBorder() {
		return new ConnectorBorder(direction, inputs.size(), outputs.size());
	}

	public ConnectionAnchor getConnectionAnchor(String terminal) {
		return connectionAnchors.get(terminal);
	}

	public ConnectionAnchor getConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Collection<FixedConnectionAnchor> coll = connectionAnchors.values();
		Iterator<FixedConnectionAnchor> iter = coll.iterator();
		while (iter.hasNext()) {
			ConnectionAnchor anchor = iter.next();
			Point p2 = anchor.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = anchor;
			}
		}
		return closest;
	}

	public String getConnectionAnchorName(ConnectionAnchor c) {
		Enumeration<String> keys = connectionAnchors.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (connectionAnchors.get(key).equals(c)) {
				return key;
			}
		}
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration<FixedConnectionAnchor> e = getSourceConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector<FixedConnectionAnchor> getSourceConnectionAnchors() {
		return outputConnectionAnchors;
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration<FixedConnectionAnchor> e = getTargetConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector<FixedConnectionAnchor> getTargetConnectionAnchors() {
		return inputConnectionAnchors;
	}

	protected void layoutConnectionAnchors() {
		FixedConnectionAnchor in;
		FixedConnectionAnchor out;
		Rectangle rect = getBounds();
		int inputCapacity = inputs.size();
		int outputCapacity = outputs.size();
		if (direction == PositionConstants.EAST) {
			int height = rect.height;
			for (int i = 0; i < inputCapacity; i++) {
				in = (FixedConnectionAnchor) getConnectionAnchor(inputs.get(i));
				in.offsetV = (2 * i + 1) * height / (inputCapacity * 2);
			}
			for (int i = 0; i < outputCapacity; i++) {
				out = (FixedConnectionAnchor) getConnectionAnchor(outputs.get(i));
				out.offsetV = (2 * i + 1) * height / (outputCapacity * 2);
				out.leftToRight = false;
			}
		}
		else {
			int width = rect.width;
			for (int i = 0; i < inputCapacity; i++) {
				in = (FixedConnectionAnchor) getConnectionAnchor(inputs.get(i));
				in.offsetH = (2 * i + 1) * width / (inputCapacity * 2);
			}
			for (int i = 0; i < outputCapacity; i++) {
				out = (FixedConnectionAnchor) getConnectionAnchor(outputs.get(i));
				out.offsetH = (2 * i + 1) * width / (outputCapacity * 2);
				out.topDown = false;
			}
		}
	}

	public void setBorderLabel(String label) {
		if (border != null) {
			border.setLabel(label);
		}
	}

	@Override
	public void validate() {
		if (isValid()) {
			return;
		}
		layoutConnectionAnchors();
		super.validate();
	}

}
