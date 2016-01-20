/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Provider;

import org.springframework.ide.eclipse.editor.support.hover.DescriptionProviders;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;

import com.google.common.collect.ImmutableList;

/**
 * Static utility method for creating YType objects representing either
 * 'array-like', 'map-like' or 'object-like' types which can be used
 * to build up a 'Yaml Schema'.
 *
 * @author Kris De Volder
 */
public class YTypeFactory {

	public YType yseq(YType el) {
		return new YSeqType(el);
	}

	public YType ymap(YType key, YType val) {
		return new YMapType(key, val);
	}

	public YBeanType ybean(String name, YTypedProperty... properties) {
		return new YBeanType(name, properties);
	}

	/**
	 * YTypeUtil instances capable of 'interpreting' the YType objects created by this
	 * YTypeFactory
	 */
	public final YTypeUtil TYPE_UTIL = new YTypeUtil() {

		@Override
		public boolean isSequencable(YType type) {
			return ((AbstractType)type).isSequenceable();
		}

		@Override
		public boolean isMap(YType type) {
			return ((AbstractType)type).isMap();
		}

		@Override
		public boolean isAtomic(YType type) {
			return ((AbstractType)type).isAtomic();
		}

		@Override
		public Map<String, YType> getPropertiesMap(YType type) {
			return ((AbstractType)type).getPropertiesMap();
		}

		@Override
		public List<YTypedProperty> getProperties(YType type) {
			return ((AbstractType)type).getProperties();
		}

		@Override
		public String[] getHintValues(YType type) {
			return ((AbstractType)type).getHintValues();
		}

		@Override
		public YType getDomainType(YType type) {
			return ((AbstractType)type).getDomainType();
		}

		@Override
		public String niceTypeName(YType type) {
			return type.toString();
		}
	};

	/////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Provides default implementations for all YType methods.
	 */
	public static abstract class AbstractType implements YType {

		private List<YTypedProperty> propertyList = new ArrayList<>();

		private final List<String> hints = new ArrayList<>();

		private Map<String, YType> cachedPropertyMap;

		public boolean isSequenceable() {
			return false;
		}

		public YType getDomainType() {
			return null;
		}

		public String[] getHintValues() {
			return hints.toArray(new String[hints.size()]);
		}

		public final List<YTypedProperty> getProperties() {
			return Collections.unmodifiableList(propertyList);
		}

		public final Map<String, YType> getPropertiesMap() {
			if (cachedPropertyMap==null) {
				cachedPropertyMap = new LinkedHashMap<>();
				for (YTypedProperty p : propertyList) {
					cachedPropertyMap.put(p.getName(), p.getType());
				}
			}
			return Collections.unmodifiableMap(cachedPropertyMap);
		}

		public boolean isAtomic() {
			return false;
		}

		public boolean isMap() {
			return false;
		}

		public abstract String toString(); // force each sublcass to implement a (nice) toString method.

		public void addProperty(YTypedProperty p) {
			cachedPropertyMap = null;
			propertyList.add(p);
		}

		public void addProperty(String name, YType type) {
			addProperty(new YTypedPropertyImpl(name, type));
		}

		public void addHints(String... strings) {
			hints.addAll(Arrays.asList(strings));
		}
	}

	public static class YMapType extends AbstractType {

		private final YType key;
		private final YType val;

		private YMapType(YType key, YType val) {
			this.key = key;
			this.val = val;
		}

		@Override
		public String toString() {
			return "Map<"+key.toString()+", "+val.toString()+">";
		}

		@Override
		public boolean isMap() {
			return true;
		}

		@Override
		public YType getDomainType() {
			return val;
		}
	}

	public static class YSeqType extends AbstractType {

		private YType el;

		private YSeqType(YType el) {
			this.el = el;
		}

		@Override
		public String toString() {
			return el.toString()+"[]";
		}

		@Override
		public boolean isSequenceable() {
			return true;
		}

		@Override
		public YType getDomainType() {
			return el;
		}
	}

	public static class YBeanType extends AbstractType {
		private final String name;

		public YBeanType(String name, YTypedProperty[] properties) {
			this.name = name;
			for (YTypedProperty p : properties) {
				addProperty(p);
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static class YAtomicType extends AbstractType {
		private final String name;
		private YAtomicType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		@Override
		public boolean isAtomic() {
			return true;
		}
	}

	public static class YTypedPropertyImpl implements YTypedProperty {

		final private String name;
		final private YType type;
		private Provider<HtmlSnippet> descriptionProvider = DescriptionProviders.NO_DESCRIPTION;

		private YTypedPropertyImpl(String name, YType type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public YType getType() {
			return this.type;
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}

		@Override
		public HtmlSnippet getDescription() {
			return descriptionProvider.get();
		}

		public void setDescriptionProvider(Provider<HtmlSnippet> descriptionProvider) {
			this.descriptionProvider = descriptionProvider;
		}

	}

	public YAtomicType yatomic(String name) {
		return new YAtomicType(name);
	}

	public YTypedPropertyImpl yprop(String name, YType type) {
		return new YTypedPropertyImpl(name, type);
	}

	public YAtomicType yenum(String name, String... values) {
		YAtomicType t = yatomic(name);
		t.addHints(values);
		return t;
	}
}
