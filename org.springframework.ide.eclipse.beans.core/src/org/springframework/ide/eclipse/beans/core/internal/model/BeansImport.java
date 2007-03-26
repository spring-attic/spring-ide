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
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;

/**
 * This class defines an import within a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 */
public class BeansImport extends AbstractBeansModelElement
		implements IBeansImport {

	private String resourceName;

	public BeansImport(IBeansConfig config, ImportDefinition definition) {
		super(config, definition.getImportedResource(), definition);
	}

	public int getElementType() {
		return IBeansModelElementTypes.IMPORT_TYPE;
	}

	public IResource getImportedResource() {
		// TODO
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansImport)) {
			return false;
		}
		BeansImport that = (BeansImport) other;
		if (!ObjectUtils.nullSafeEquals(this.resourceName, that.resourceName))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(resourceName);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": resource=");
		text.append(resourceName);
		return text.toString();
	}
}
