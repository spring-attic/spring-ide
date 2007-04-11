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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * This class defines an import within a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansImport extends AbstractBeansModelElement
		implements IBeansImport {

	private String resourcePath;

	public BeansImport(IBeansConfig config, ImportDefinition definition) {
		super(config, definition.getImportedResource(), definition);
		resourcePath = StringUtils.cleanPath(definition.getImportedResource());
	}

	public int getElementType() {
		return IBeansModelElementTypes.IMPORT_TYPE;
	}

	public IFile getImportedFile() {
		if (resourcePath.indexOf(':') > -1) {
			IBeansConfig config = (IBeansConfig) getElementParent();
			return config.getElementResource().getParent().getFile(
					new Path(resourcePath));
		}
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
		if (!ObjectUtils.nullSafeEquals(this.resourcePath, that.resourcePath))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(resourcePath);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": resource=");
		text.append(resourcePath);
		return text.toString();
	}
}
