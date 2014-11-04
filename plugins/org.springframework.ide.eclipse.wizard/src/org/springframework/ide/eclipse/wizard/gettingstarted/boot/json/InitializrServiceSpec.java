/*******************************************************************************
 * Copyright (c) 2014 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.boot.json;

import java.io.InputStream;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class is the 'parsed' form of the json metadata for spring intializr service.
 *
 * See: https://github.com/spring-io/initializr/wiki/Initializr-metadata-json-format
 *
 * @author Kris De Volder
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class InitializrServiceSpec {

	public static InitializrServiceSpec parseFrom(InputStream input) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(input, InitializrServiceSpec.class);
	}

	/////////////////////////////////////////////////

	public static abstract class Nameable {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Type extends Option {
		private String action;

		public void setAction(String action) {
			this.action = action;
		}

		public String getAction() {
			return action;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Depependency extends Nameable {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		private String description;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class DependencyGroup extends Nameable {

		private Depependency[] content;

		public Depependency[] getContent() {
			return content;
		}

		public void setContent(Depependency[] content) {
			this.content = content;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Option extends Nameable {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		private boolean isDefault;

		public boolean isDefault() {
			return isDefault;
		}

		public void setDefault(boolean isDefault) {
			this.isDefault = isDefault;
		}
	}

	/////////////////////////////////////////////////////////////////

	private DependencyGroup[] dependencies;

	public DependencyGroup[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(DependencyGroup[] dependencies) {
		this.dependencies = dependencies;
	}

	/////////////////////////////////////////////////////////////////

	private Type[] types;

	public Type[] getTypes() {
		return types;
	}

	public void setTypes(Type[] types) {
		this.types = types;
	}

	/////////////////////////////////////////////////////////////////

	private Option[] packagings;

	public Option[] getPackagings() {
		return packagings;
	}

	public void setPackagings(Option[] packagings) {
		this.packagings = packagings;
	}

	/////////////////////////////////////////////////////////////////

	private Option[] javaVersions;

	public Option[] getJavaVersions() {
		return javaVersions;
	}

	public void setJavaVersions(Option[] javaVersions) {
		this.javaVersions = javaVersions;
	}

	/////////////////////////////////////////////////////////////////

	private Option[] languages;

	public Option[] getLanguages() {
		return languages;
	}

	public void setLanguages(Option[] languages) {
		this.languages = languages;
	}

	/////////////////////////////////////////////////////////////////

	private Option[] bootVersions;

	public Option[] getBootVersions() {
		return bootVersions;
	}

	public void setBootVersions(Option[] bootVersions) {
		this.bootVersions = bootVersions;
	}

	/////////////////////////////////////////////////////////////////

	private Map<String,String> defaults;

	public Map<String, String> getDefaults() {
		return defaults;
	}

	public void setDefaults(Map<String, String> defaults) {
		this.defaults = defaults;
	}

}
