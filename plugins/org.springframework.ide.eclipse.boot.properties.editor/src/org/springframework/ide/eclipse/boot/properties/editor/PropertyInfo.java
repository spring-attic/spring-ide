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
package org.springframework.ide.eclipse.boot.properties.editor;

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
			String st = source.getSourceType();
			this.sourceType = st!=null?st:source.getType();
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

	final private String id;
	final private String type;
	final private String name;
	final private Object defaultValue;
	final private String description;
	private List<PropertySource> sources;

	private PropertyInfo(String id, String type, String name,
			Object defaultValue, String description,
			List<PropertySource> sources) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.description = description;
		this.sources = sources;
	}
	public PropertyInfo(ConfigurationMetadataProperty prop) {
		this(
			prop.getId(),
			prop.getType(),
			prop.getName(),
			prop.getDefaultValue(),
			prop.getDescription(),
			null
		);
	}
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public String getDescription() {
		return description;
	}

	public List<PropertySource> getSources() {
		if (sources!=null) {
			return sources;
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "PropertyInfo("+getId()+")";
	}
	public void addSource(ConfigurationMetadataSource source) {
		if (sources==null) {
			sources = new ArrayList<PropertySource>();
		}
		sources.add(new PropertySource(source));
	}

	public PropertyInfo withId(String alias) {
		if (alias.equals(id)) {
			return this;
		}
		return new PropertyInfo(alias, type, name, defaultValue, description, sources);
	}
}
