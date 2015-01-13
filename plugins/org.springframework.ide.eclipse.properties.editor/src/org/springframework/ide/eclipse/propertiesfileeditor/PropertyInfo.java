/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataSource;

/**
 * Information about a spring property, basically, this is the same as
 * 
 * {@link ConfigurationMetadataProperty} but augmented with information
 * about {@link ConfigurationMetadataSource}s that declare the property.
 * 
 * @author Kris De Volder
 */
public class PropertyInfo {

	/**
	 * Identifies a 'Source'. This is essentially the sames as {@link ConfigurationMetadataSource}. 
	 * We could use {@link ConfigurationMetadataSource} directly, but this only contains
	 * the info that we actually use so takes less memory.
	 */
	public static class PropertySource {
		private final String sourceType;
		private final String sourceMethod;
		public PropertySource(ConfigurationMetadataSource source) {
			this.sourceType = source.getSourceType();
			this.sourceMethod = source.getSourceMethod();
		}
		@Override
		public String toString() {
			return sourceType+"::"+sourceMethod;
		}
		public String getSourceType() {
			return sourceType;
		}
		public String getSourceMethod() {
			return sourceMethod;
		}
	}
	
	private ConfigurationMetadataProperty property; //TODO: unpack and keep interesting data only and throw away the rest?
	private List<PropertySource> sources;
	
	public PropertyInfo(ConfigurationMetadataProperty prop) {
		this.property = prop;
	}
	public String getId() {
		return property.getId();
	}
	public String getType() {
		return property.getType();
	}
	public String getName() {
		return property.getName();
	}
	public Object getDefaultValue() {
		return property.getDefaultValue();
	}
	public String getDescription() {
		return property.getDescription();
	}
	
	public List<PropertySource> getSources() {
		if (sources!=null) {
			return sources;
		}
		return Collections.emptyList();
	}
	
	@Override
	public String toString() {
		return "PropertyInfo("+property.getId()+")";
	}
	public void addSource(ConfigurationMetadataSource source) {
		if (sources==null) {
			sources = new ArrayList<PropertySource>();
		}
		sources.add(new PropertySource(source));
	}
}
