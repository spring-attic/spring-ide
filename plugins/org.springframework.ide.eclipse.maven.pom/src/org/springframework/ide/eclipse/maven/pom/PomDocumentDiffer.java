/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.pom;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Difference;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Direction;

public class PomDocumentDiffer {

	public static XmlDocumentDiffer create(IDocument document1, IDocument document2) {
		return new XmlDocumentDiffer(document1, document2)
				.idProvider(XmlSelectors.childrenOf("properties", "project", "parent", "dependency", "repository",
						"pluginRepository", "plugin"), IdProviders.FROM_TAG_NAME)
				.idProvider(XmlSelectors.tagName("dependency"), IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.tagName("repository"), IdProviders.fromChildren("url"))
				.idProvider(XmlSelectors.tagName("pluginRepository"), IdProviders.fromChildren("url"))
				.idProvider(XmlSelectors.tagName("plugin"), IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.path("dependency", "exclusions", "exclusion"),
						IdProviders.fromChildren("groupId", "artifactId"))
				.idProvider(XmlSelectors.path("dependency", "exclusions", "exclusion", "*"),
						IdProviders.FROM_TAG_NAME);
	}

	public static Predicate<Difference> differenceDirections(Direction... directions) {
		return new Predicate<Difference>() {

			@Override
			public boolean test(Difference t) {
				return Arrays.asList(directions).contains(t.direction);
			}
			
		};
	}

}
