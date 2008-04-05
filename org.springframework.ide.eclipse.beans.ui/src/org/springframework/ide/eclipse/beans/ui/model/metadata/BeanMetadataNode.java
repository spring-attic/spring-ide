/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model.metadata;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Tree element used in the Spring and Project Explorer to integrate generically
 * integrate third-party contributed bean meta data.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataNode {

	private Object[] children = IModelElement.NO_CHILDREN;

	private String description;

	private String handleIdentifier;

	private Image image;

	private String label;

	private IModelSourceLocation location;

	public BeanMetadataNode(String handleIdentifier) {
		this.handleIdentifier = handleIdentifier;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanMetadataNode)) {
			return false;
		}
		BeanMetadataNode that = (BeanMetadataNode) other;
		if (!ObjectUtils.nullSafeEquals(this.children, that.children))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.description, that.description))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.image, that.image))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.label, that.label))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.handleIdentifier, that.handleIdentifier))
			return false;
		return ObjectUtils.nullSafeEquals(this.location, that.location);
	}

	public Object[] getChildren() {
		return children;
	}

	public String getDescription() {
		return description;
	}

	public String getHandleIdentifier() {
		return handleIdentifier;
	}

	public org.eclipse.swt.graphics.Image getImage() {
		return image;
	}

	public String getLabel() {
		return label;
	}

	public IModelSourceLocation getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(children);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(description);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(image);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(label);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(location);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(handleIdentifier);
		return 12 * hashCode;
	}

	public void setChildren(Object[] children) {
		this.children = children;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setImage(org.eclipse.swt.graphics.Image image) {
		this.image = image;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLocation(IModelSourceLocation location) {
		this.location = location;
	}
}
