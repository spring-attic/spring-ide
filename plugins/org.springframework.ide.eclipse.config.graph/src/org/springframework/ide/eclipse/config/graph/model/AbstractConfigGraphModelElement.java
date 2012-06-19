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

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySource;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigGraphModelElement extends AbstractGefGraphModelElement implements IAdaptable {

	private IDOMElement input;

	private AbstractConfigGraphDiagram diagram;

	protected ShallowFormatProcessorXML formatter;

	private Rectangle bounds;

	private String name = "node"; //$NON-NLS-1$

	private String shortName;

	private String displayLabel;

	private String namespaceUri;

	private boolean hasManualBounds;

	public AbstractConfigGraphModelElement() {
		this.formatter = new ShallowFormatProcessorXML();
		hasManualBounds = false;
	}

	public AbstractConfigGraphModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		this();
		this.input = input;
		this.diagram = diagram;
		internalSetName();
	}

	protected void createInput(String uri) {
		if (input == null) {
			if (uri == null) {
				namespaceUri = getDiagram().getNamespaceUri();
			}
			else {
				namespaceUri = uri;
			}
			IDOMDocument document = getDiagram().getDomDocument();
			input = (IDOMElement) document.createElement(getInputName());
			input.setPrefix(ConfigCoreUtils.getPrefixForNamespaceUri(document, namespaceUri));
			internalSetName();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AbstractConfigGraphModelElement)) {
			return false;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		AbstractConfigGraphModelElement other = (AbstractConfigGraphModelElement) obj;
		if (input == null) {
			if (other.input != null) {
				return false;
			}
		}
		else if (!input.equals(other.input)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
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

	public Rectangle getBounds() {
		return bounds;
	}

	public AbstractConfigGraphDiagram getDiagram() {
		return diagram;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public IDOMElement getInput() {
		return input;
	}

	public abstract String getInputName();

	public List<Activity> getModelRegistry() {
		return getDiagram().getModelRegistry();
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		return result;
	}

	public boolean hasManualBounds() {
		return hasManualBounds;
	}

	protected void internalSetName() {
		String id = getInput().getAttribute(BeansSchemaConstants.ATTR_ID);
		if (id != null && !id.trim().equals("")) { //$NON-NLS-1$
			name = id;
			shortName = name;
		}
		else {
			name = getInput().getLocalName();
			shortName = ""; //$NON-NLS-1$
		}
		displayLabel = shortName;
	}

	public void modifyBounds(Rectangle bounds) {
		Rectangle oldBounds = this.bounds;
		if (!bounds.equals(oldBounds)) {
			this.bounds = bounds;
			hasManualBounds = true;
			firePropertyChange(BOUNDS, null, bounds);
		}
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public void setDiagram(AbstractConfigGraphDiagram diagram) {
		this.diagram = diagram;
	}

	public void setDisplayLabel(String label) {
		displayLabel = label;
	}

	public void setHasManualBounds(boolean hasManualBounds) {
		if (hasManualBounds) {
			this.hasManualBounds = hasManualBounds;
		}
	}

	public void setInput(IDOMElement input) {
		this.input = input;
	}

	public void setName(String s) {
		name = s;
		shortName = s;
		displayLabel = shortName;
		firePropertyChange(NAME, null, s);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String className = getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);
		return className + "(" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
