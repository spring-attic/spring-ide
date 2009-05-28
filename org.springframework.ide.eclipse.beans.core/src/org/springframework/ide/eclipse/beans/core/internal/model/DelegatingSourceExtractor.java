/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.java.classreading.JdtSourceExtractor;
import org.springframework.ide.eclipse.core.model.java.JavaSourceExtractor;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceExtractor;

/**
 * Composite {@link SourceExtractor} implementation that internally delegates to
 * a {@link XmlSourceExtractor} and {@link JavaSourceExtractor} to do the source
 * extraction.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class DelegatingSourceExtractor implements SourceExtractor {

	private Set<SourceExtractor> sourceExtractors;

	public DelegatingSourceExtractor(IProject project) {
		this.sourceExtractors = new HashSet<SourceExtractor>();
		this.sourceExtractors.add(new XmlSourceExtractor());
		this.sourceExtractors.add(new JavaSourceExtractor(project));
		this.sourceExtractors.add(new JdtSourceExtractor());
	}

	public Object extractSource(Object sourceCandidate,
			Resource definingResource) {
		if (sourceCandidate != null) {
			for (SourceExtractor sourceExtractor : sourceExtractors) {
				Object object = sourceExtractor.extractSource(sourceCandidate,
						definingResource);
				if (!sourceCandidate.equals(object)) {
					return object;
				}
			}
		}
		return sourceCandidate;
	}
}