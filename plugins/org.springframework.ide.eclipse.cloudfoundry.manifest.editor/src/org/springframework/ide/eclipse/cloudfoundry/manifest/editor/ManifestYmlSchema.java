/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.Collection;
import java.util.Set;

import javax.inject.Provider;

import org.springframework.ide.eclipse.editor.support.hover.DescriptionProviders;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;
import org.springframework.ide.eclipse.editor.support.util.ValueParsers;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.eclipse.editor.support.yaml.schema.DynamicSchemaContext;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.AbstractType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.YAtomicType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.YBeanType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeFactory.YTypedPropertyImpl;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YValueHint;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YamlSchema;
import org.springframework.ide.eclipse.editor.support.yaml.schema.constraints.Constraints;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class ManifestYmlSchema implements YamlSchema {

	private static final String HEALTH_CHECK_HTTP_ENDPOINT_PROP = "health-check-http-endpoint";
	private static final String HEALTH_CHECK_TYPE_PROP = "health-check-type";

	private final YBeanType TOPLEVEL_TYPE;
	private final YTypeUtil TYPE_UTIL;
	private final Provider<Collection<YValueHint>> buildpackProvider;

	private static final Set<String> TOPLEVEL_EXCLUDED = ImmutableSet.of(
		"name", "host", "hosts"
	);

	public ManifestYmlSchema(Provider<Collection<YValueHint>> buildpackProvider) {
		this.buildpackProvider = buildpackProvider;
		YTypeFactory f = new YTypeFactory();
		TYPE_UTIL = f.TYPE_UTIL;

		// define schema types
		TOPLEVEL_TYPE = f.ybean("Cloudfoundry Manifest");
		TOPLEVEL_TYPE.require(this::verify_heatth_check_http_end_point_constraint);

		AbstractType application = f.ybean("Application");
		application.require(this::verify_heatth_check_http_end_point_constraint);
		application.require(
				ManifestConstraints.mutuallyExclusive("routes", "domain", "domains", "host", "hosts", "no-hostname"));
		YAtomicType t_path = f.yatomic("Path");

		YAtomicType t_buildpack_entry = f.yatomic("Buildpack Entry");
		if (buildpackProvider != null) {
			t_buildpack_entry.addHintProvider(buildpackProvider);
//			t_buildpack_entry.parseWith(ManifestYmlValueParsers.fromHints(t_buildpack_entry.toString(), buildpackProvider));
		}

		// Deprecated. See: https://www.pivotaltracker.com/story/show/162499688
		YAtomicType t_buildpack = f.yatomic("Buildpack");
		if (t_buildpack != null) {
			t_buildpack.addHintProvider(buildpackProvider);
			t_buildpack.require(Constraints.deprecateProperty((name) ->
								"Deprecated: Use `buildpacks` instead.", "buildpack"));
		}

		YAtomicType t_boolean = f.yenum("boolean", "true", "false");
		YAtomicType t_ne_string = f.yatomic("String");
		t_ne_string.parseWith(ValueParsers.NE_STRING);
		YType t_string = f.yatomic("String");
		YType t_strings = f.yseq(t_string);
		
		YBeanType route = f.ybean("Route");
		route.addProperty(f.yprop("route", t_string));

		YAtomicType t_memory = f.yatomic("Memory");
		t_memory.addHints("256M", "512M", "1024M");
		t_memory.parseWith(ManifestYmlValueParsers.MEMORY);
		
		YAtomicType t_health_check_type = f.yenumBuilder("Health Check Type", "none", "process", "port", "http")
				.deprecateWithReplacement("none", "process")
				.build();

		YAtomicType t_strictly_pos_integer = f.yatomic("Strictly Positive Integer");
		t_strictly_pos_integer.parseWith(ValueParsers.integerAtLeast(1));

		YAtomicType t_pos_integer = f.yatomic("Positive Integer");
		t_pos_integer.parseWith(ValueParsers.POS_INTEGER);

		YType t_env = f.ymap(t_string, t_string);

		// define schema structure...
		TOPLEVEL_TYPE.addProperty("applications", f.yseq(application));
		TOPLEVEL_TYPE.addProperty("inherit", t_string, descriptionFor("inherit"));

		YTypedPropertyImpl[] props = {
			f.yprop("buildpack", t_buildpack), 
			f.yprop("buildpacks", f.yseq(t_buildpack_entry)),
			f.yprop("command", t_string),
			f.yprop("disk_quota", t_memory),
			f.yprop("domain", t_string),
			f.yprop("domains", t_strings),
			f.yprop("env", t_env),
			f.yprop("host", t_string),
			f.yprop("hosts", t_strings),
			f.yprop("instances", t_strictly_pos_integer),
			f.yprop("memory", t_memory),
			f.yprop("name", t_string),
			f.yprop("no-hostname", t_boolean),
			f.yprop("no-route", t_boolean),
			f.yprop("path", t_path),
			f.yprop("random-route", t_boolean),
			f.yprop("routes", f.yseq(route)),
			f.yprop("services", t_strings),
			f.yprop("stack", t_string),
			f.yprop("timeout", t_pos_integer),
			
			f.yprop(HEALTH_CHECK_TYPE_PROP, t_health_check_type),
			f.yprop(HEALTH_CHECK_HTTP_ENDPOINT_PROP, t_ne_string)
		};

		for (YTypedPropertyImpl prop : props) {
			prop.setDescriptionProvider(descriptionFor(prop));
			if (!TOPLEVEL_EXCLUDED.contains(prop.getName())) {
				TOPLEVEL_TYPE.addProperty(prop);
			}
			application.addProperty(prop);
		}
	}

	private Provider<HtmlSnippet> descriptionFor(String propName) {
		return DescriptionProviders.fromClasspath(this.getClass(), "/description-by-prop-name/"+propName+".html");
	}

	private Provider<HtmlSnippet> descriptionFor(YTypedPropertyImpl prop) {
		return descriptionFor(prop.getName());
	}

	@Override
	public YBeanType getTopLevelType() {
		return TOPLEVEL_TYPE;
	}

	@Override
	public YTypeUtil getTypeUtil() {
		return TYPE_UTIL;
	}
	
	private void verify_heatth_check_http_end_point_constraint(DynamicSchemaContext dc, Node parent, Node node, YType type, IProblemCollector problems) {
		YamlFileAST ast = dc.getAST();
		if (ast!=null) {
			Node markerNode = YamlPathSegment.keyAt(HEALTH_CHECK_HTTP_ENDPOINT_PROP).traverseNode(node);
			if (markerNode != null) {
				String healthCheckType = getEffectiveHealthCheckType(ast, dc.getPath(), node);
				if (!"http".equals(healthCheckType)) {
					problems.accept(YamlSchemaProblems.problem(ManifestYamlSchemaProblemsTypes.IGNORED_PROPERTY,
							"This has no effect unless `"+HEALTH_CHECK_TYPE_PROP+"` is `http` (but it is currently set to `"+healthCheckType+"`)", markerNode));
				}
			}
		}
	}
	
	/**
	 * Determines the actual health-check-type that applies to a given node, taking into account
	 * inheritance from parent node, and default value.
	 */
	private String getEffectiveHealthCheckType(YamlFileAST ast, YamlPath path, Node node) {
		String explicit = NodeUtil.getScalarProperty(node, HEALTH_CHECK_TYPE_PROP);
		if (explicit!=null) {
			return explicit;
		}
		if (path.size()>2) {
			//Must consider inherited props!
			YamlPath parentPath = path.dropLast(2);
			Node parent = parentPath.traverseToNode(ast);
			String inherited = NodeUtil.getScalarProperty(parent, HEALTH_CHECK_TYPE_PROP);
			if (inherited!=null) {
				return inherited;
			}
		}
		return "port";
	}
}
