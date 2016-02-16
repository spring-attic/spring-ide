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
package org.springframework.ide.eclipse.config.graph.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Graphics;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySource;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class Transition extends AbstractGefGraphModelElement implements IAdaptable {

	public static final Integer SOLID_CONNECTION = new Integer(Graphics.LINE_SOLID);

	public static final Integer DASHED_CONNECTION = new Integer(Graphics.LINE_DASH);

	private int lineStyle;

	private boolean isDirectional;

	public Activity source, target;

	private IDOMNode input;

	public Transition(Activity source, Activity target) {
		this(source, target, null);
	}

	public Transition(Activity source, Activity target, IDOMNode input) {
		this(source, target, input, Graphics.LINE_SOLID, true);
	}

	public Transition(Activity source, Activity target, IDOMNode input, int style, boolean isDirectional) {
		this.input = input;
		this.source = source;
		this.target = target;
		this.isDirectional = isDirectional;
		lineStyle = style;

		// GEF will attempt to break cycles when it finds them, but neglects to
		// do so when an object cycles on itself. Here we detect if the source
		// and target are equal, and if so discard the transition. Doing so will
		// avoid a runtime exception.
		if (!source.equals(target)) {
			source.addOutgoing(this);
			target.addIncoming(this);
		}
	}

	public Transition(Activity source, Activity target, IDOMNode input, boolean isDirectional) {
		this(source, target, input, Graphics.LINE_SOLID, isDirectional);
	}

	public Transition(Activity source, Activity target, IDOMNode input, int style) {
		this(source, target, input, style, true);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Transition)) {
			return false;
		}
		Transition other = (Transition) obj;
		if (input == null) {
			if (other.input != null) {
				return false;
			}
		}
		else if (!input.equals(other.input)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		}
		else if (!source.equals(other.source)) {
			return false;
		}
		if (target == null) {
			if (other.target != null) {
				return false;
			}
		}
		else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

	public Object getAdapter(Class adapter) {
		if (IPropertySource.class == adapter) {
			IPropertySource propertySource = (IPropertySource) input.getAdapterFor(IPropertySource.class);
			if (propertySource == null) {
				propertySource = new XMLPropertySource(input);
			}
			return propertySource;
		}
		return null;
	}

	public IDOMNode getInput() {
		return input;
	}

	public int getLineStyle() {
		return lineStyle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	public boolean isDirectional() {
		return isDirectional;
	}

	public void setInput(IDOMNode input) {
		this.input = input;
	}

	public void setIsDirectional(boolean isDirectional) {
		this.isDirectional = isDirectional;
	}

	public void setLineStyle(int lineStyle) {
		if (lineStyle == Graphics.LINE_SOLID || lineStyle == Graphics.LINE_DASH) {
			this.lineStyle = lineStyle;
			firePropertyChange(LINESTYLE, null, new Integer(this.lineStyle));
		}
	}

}
