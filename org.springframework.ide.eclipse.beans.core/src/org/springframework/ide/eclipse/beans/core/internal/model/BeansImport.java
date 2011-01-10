/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * This class defines an import within a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansImport extends AbstractBeansModelElement
		implements IBeansImport {

	private String resourcePath;
	
	private Set<IImportedBeansConfig> beansConfigs = 
		new LinkedHashSet<IImportedBeansConfig>();

	public BeansImport(IBeansConfig config, ImportDefinition definition) {
		super(config, definition.getImportedResource(), definition);
		resourcePath = StringUtils.cleanPath(definition.getImportedResource());
	}

	public int getElementType() {
		return IBeansModelElementTypes.IMPORT_TYPE;
	}
	
	@Override
	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getImportedBeansConfigs());
		return children.toArray(new IModelElement[children.size()]);
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

	public Set<IImportedBeansConfig> getImportedBeansConfigs() {
		return beansConfigs;
	}
	
	protected void addImportedBeansConfig(IImportedBeansConfig importedBeansConfig) {
		beansConfigs.add(importedBeansConfig);
	}
	
}
