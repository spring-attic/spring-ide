/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(value=Include.NON_NULL)
public class InitializrDependencySpec {

	/* Some examples:
    {
        "id": "activiti-basic",
        "name": "Activiti",
        "groupId": "org.activiti",
        "artifactId": "activiti-spring-boot-starter-basic",
        "version": "5.19.0",
        "scope": "compile"
      },
    {
      "id": "lombok",
      "name": "Lombok",
      "groupId": "org.projectlombok",
      "artifactId": "lombok",
      "scope": "compile",
      "bom": "cloud-bom"
    },
    {
      "id": "postgresql",
      "name": "PostgreSQL",
      "groupId": "org.postgresql",
      "artifactId": "postgresql",
      "version": "9.4-1201-jdbc41",
      "scope": "runtime"
    },
	 */

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonInclude(value=Include.NON_NULL)
	public static class DependencyInfo {
		private String id;
		private String groupId;
		private String artifactId;
		private String version;
		private String scope;
		private String bom;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getGroupId() {
			return groupId;
		}
		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
		public String getArtifactId() {
			return artifactId;
		}
		public void setArtifactId(String artifactId) {
			this.artifactId = artifactId;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getScope() {
			return scope;
		}
		public void setScope(String scope) {
			this.scope = scope;
		}
		public String getBom() {
			return bom;
		}
		public void setBom(String bom) {
			this.bom = bom;
		}
	}

	private static final String JSON_CONTENT_TYPE_HEADER = "application/json";

	private DependencyInfo[] dependencies;

	public static InitializrDependencySpec parseFrom(InputStream input) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(input, InitializrDependencySpec.class);
	}

	public DependencyInfo[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(DependencyInfo[] dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String toString() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			BootActivator.log(e);
			return super.toString();
		}
	}

	public static InitializrDependencySpec parseFrom(URLConnectionFactory urlConnectionFactory, URL url) throws Exception {
		URLConnection conn = null;
		InputStream input = null;
		try {
			conn = urlConnectionFactory.createConnection(url);
			conn.addRequestProperty("Accept", JSON_CONTENT_TYPE_HEADER);
			conn.connect();
			input = conn.getInputStream();
			return parseFrom(input);
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static InitializrDependencySpec parseFrom(URLConnectionFactory urlConnectionFactory, String url) throws Exception {
		Exception exception = null;
		for (int i = 0; i < 5; i++) { //TODO: remove this, but at the moment this service seems unreliable for an unknown reason
			try {
				return parseFrom(urlConnectionFactory, new URL(url));
			} catch (Exception e) {
				exception = e;
			}
		}
		throw exception;
	}
}
