/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
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

import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.editor.support.util.CollectionUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

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
	private Deprecation deprecation;
	private ImmutableList<ValueHint> valueHints;
	private ImmutableList<ValueHint> keyHints;

	public PropertyInfo(String id, String type, String name,
			Object defaultValue, String description,
			Deprecation deprecation,
			List<ValueHint> valueHints,
			List<ValueHint> keyHints,
			List<PropertySource> sources) {
		super();
		this.id = id;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
		this.description = description;
		this.deprecation = deprecation;
		this.valueHints = valueHints==null?null:ImmutableList.copyOf(valueHints);
		this.keyHints = keyHints==null?null:ImmutableList.copyOf(keyHints);
		this.sources = sources;
	}
	public PropertyInfo(ConfigurationMetadataProperty prop) {
		this(
			prop.getId(),
			prop.getType(),
			prop.getName(),
			prop.getDefaultValue(),
			prop.getDescription(),
			prop.getDeprecation(),
			prop.getValueHints(),
			prop.getKeyValueHints(),
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

	public HintProvider getHints(TypeUtil typeUtil, boolean dimensionAware) {
		Type type = TypeParser.parse(this.type);
		if (TypeUtil.isMap(type)) {
			if (CollectionUtil.hasElements(valueHints) || CollectionUtil.hasElements(keyHints)) {
				return HintProviders.forMap(keyHints, valueHints, TypeUtil.getDomainType(type), dimensionAware);
			}
		} else if (TypeUtil.isSequencable(type)) {
			if (CollectionUtil.hasElements(valueHints)) {
				if (dimensionAware) {
					if (TypeUtil.isSequencable(type)) {
						return HintProviders.forDomainAt(valueHints, TypeUtil.getDimensionality(type));
					} else {
						return HintProviders.forHere(valueHints);
					}
				} else {
					return HintProviders.forAllValueContexts(valueHints);
				}
			}
		} else {
			if (CollectionUtil.hasElements(valueHints)) {
				return HintProviders.forHere(valueHints);
			}
		}
		return null;
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
		return new PropertyInfo(alias, type, name, defaultValue, description, deprecation, valueHints, keyHints, sources);
	}

	public void setDeprecation(Deprecation d) {
		this.deprecation = d;
	}

	public boolean isDeprecated() {
		return deprecation!=null;
	}

	public String getDeprecationReason() {
		return deprecation == null ? null : deprecation.getReason();
	}

	public String getDeprecationReplacement() {
		return deprecation == null ? null : deprecation.getReplacement();
	}

	public void addValueHints(List<ValueHint> hints) {
		Builder<ValueHint> builder = ImmutableList.builder();
		builder.addAll(valueHints);
		builder.addAll(hints);
		valueHints = builder.build();
	}
	public void addKeyHints(List<ValueHint> hints) {
		Builder<ValueHint> builder = ImmutableList.builder();
		builder.addAll(keyHints);
		builder.addAll(hints);
		keyHints = builder.build();
	}
}
