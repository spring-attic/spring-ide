/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.stream.Collectors;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlExclusivePropertiesReconciler.ProblemFactory;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblemImpl;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.CompositeYamlASTReconciler;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlASTReconciler;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlSchemaProblems;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YamlSchema;

import com.google.common.collect.ImmutableSet;

/**
 * Deployment Manifest YAML reconciler engine 
 * 
 * @author Alex Boyko
 *
 */
public class ManifestYamlReconcileEngine extends YamlSchemaBasedReconcileEngine {
	
	private static final ImmutableSet<String> ROUTES = ImmutableSet.of("routes");
	private static final ImmutableSet<String> LEGACY_ROUTES = ImmutableSet.of("host", "hosts", "domain", "domains", "no-hostname");
	
	private static final ProblemType LEGACY_PROPERTY_PROBLEM = YamlSchemaProblems.problemType("ManifestYamlLegacyPropertyProblem");
	
	private static ProblemFactory LEGACY_ROUTES_PROBLEM_FACTORY = (text, start, end) -> {
		StringBuilder message = new StringBuilder();
		message.append('\'');
		message.append(text);		
		message.append('\'');
		message.append(" is a legacy property and cannot co-exist with ");
		if (ROUTES.size() > 1) {
			message.append("any of the properties: ");
			String.join(", ", ROUTES.stream().map(s -> "'" + s + "'").collect(Collectors.toList()));
		} else if (ROUTES.size() == 1){
			message.append("property ");
			message.append('\'');
			message.append(ROUTES.iterator().next());		
			message.append('\'');
		} else {
			throw new RuntimeException("ROUTES cannot be empty!");
		}
		return new ReconcileProblemImpl(LEGACY_PROPERTY_PROBLEM, message.toString(), start, end - start);
	};

	public ManifestYamlReconcileEngine(YamlASTProvider parser, YamlSchema schema) {
		super(parser, schema);
	}

	@Override
	protected YamlASTReconciler getASTReconciler(IDocument doc, IProblemCollector problemCollector) {
		return new CompositeYamlASTReconciler(super.getASTReconciler(doc, problemCollector),
				new ManifestYamlExclusivePropertiesReconciler(problemCollector, ROUTES, null, LEGACY_ROUTES,
						LEGACY_ROUTES_PROBLEM_FACTORY));
	}

}
