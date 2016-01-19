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
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.Set;

import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypedProperty;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class ManifestYmlSchema {

	public final YBeanType TOPLEVEL_TYPE;
	public final YTypeUtil TYPE_UTIL;

	private static final Set<String> TOPLEVEL_EXCLUDED = ImmutableSet.of(
		"name", "host", "hosts"
	);

	public ManifestYmlSchema() {
		YTypeFactory f = new YTypeFactory();
		TYPE_UTIL = f.TYPE_UTIL;

		// define schema types
		TOPLEVEL_TYPE = f.ybean("manifest.yml schema");

		YBeanType application = f.ybean("Application");
		YAtomicType t_path = f.yatomic("Path");

		YAtomicType t_boolean = f.yenum("boolean", "true", "false");
		YType t_string = f.yatomic("String");
		YType t_strings = f.yseq(t_string);

		YAtomicType t_memory = f.yatomic("Memory");
		t_memory.addHints("256M", "512M", "1024M");

		YType t_integer = f.yatomic("integer");

		YType t_env = f.ymap(t_string, t_string);

		// define schema structure...
		TOPLEVEL_TYPE.addProperty("applications", f.yseq(application));

		YTypedProperty[] props = {
			f.yprop("buildpack", t_string),
			f.yprop("command", t_string),
			f.yprop("disk_quota", t_memory),
			f.yprop("domain", t_string),
			f.yprop("env", t_env),
			f.yprop("host", t_string),
			f.yprop("hosts", t_strings),
			f.yprop("instances", t_integer),
			f.yprop("memory", t_memory),
			f.yprop("name", t_string),
			f.yprop("no-hostname", t_boolean),
			f.yprop("no-route", t_boolean),
			f.yprop("path", t_path),
			f.yprop("random-route", t_boolean),
			f.yprop("services", t_strings),
			f.yprop("stack", t_string),
			f.yprop("timeout", t_integer)
		};

		for (YTypedProperty prop : props) {
			if (!TOPLEVEL_EXCLUDED.contains(prop.getName())) {
				TOPLEVEL_TYPE.addProperty(prop);
			}
			application.addProperty(prop);
		}
	}

}
