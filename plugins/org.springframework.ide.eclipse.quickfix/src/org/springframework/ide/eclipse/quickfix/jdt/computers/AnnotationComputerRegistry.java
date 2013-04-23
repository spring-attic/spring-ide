/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;

/**
 * @author Terry Denney
 * @since 2.6
 */
public class AnnotationComputerRegistry {

	public static JavaCompletionProposalComputer[] computers = new JavaCompletionProposalComputer[] {
			new QualifierCompletionProposalComputer(), new RequestMappingParamTypeProposalComputer(),
			new ConfigurationLocationProposalComputer() };

	public final static String DEFAULT_ATTRIBUTE_NAME = "value";

	/**
	 * annotation name -> attribute name -> proposal computer
	 */
	public static Map<String, Map<String, Set<AnnotationProposalComputer>>> annotationArgComputers = null;

	private static void init() {
		annotationArgComputers = new HashMap<String, Map<String, Set<AnnotationProposalComputer>>>();

		addProposalComputer("Qualifier", new QualifierArgumentProposalComputer());
		addProposalComputer("RequestMapping", new RequestMappingVariableProposalComputer());
		addProposalComputer("ContextConfiguration", new ConfigurationLocationProposalComputer());
		addProposalComputer("ComponentScan", new PackageNameProposalComputer());
	}

	private static void addProposalComputer(String annotationName, AnnotationProposalComputer proposalComputer) {
		addProposalComputer(annotationName, DEFAULT_ATTRIBUTE_NAME, proposalComputer);
	}

	private static void addProposalComputer(String annotationName, String attributeName,
			AnnotationProposalComputer proposalComputer) {
		if (annotationArgComputers != null) {
			Map<String, Set<AnnotationProposalComputer>> attributeToComputers = annotationArgComputers
					.get(annotationName);
			if (attributeToComputers == null) {
				attributeToComputers = new HashMap<String, Set<AnnotationProposalComputer>>();
				annotationArgComputers.put(annotationName, attributeToComputers);

				Set<AnnotationProposalComputer> computers = attributeToComputers.get(attributeName);
				if (computers == null) {
					computers = new HashSet<AnnotationProposalComputer>();
					attributeToComputers.put(attributeName, computers);
				}

				computers.add(proposalComputer);
			}

		}
	}

	public static Set<AnnotationProposalComputer> getProposalComputer(String annotationName) {
		return getProposalComputer(annotationName, DEFAULT_ATTRIBUTE_NAME);
	}

	public static Set<AnnotationProposalComputer> getProposalComputer(String annotationName, String attributeName) {
		if (annotationArgComputers == null) {
			init();
		}

		Map<String, Set<AnnotationProposalComputer>> attributeToComputers = annotationArgComputers.get(annotationName);
		if (attributeToComputers != null) {
			Set<AnnotationProposalComputer> result = attributeToComputers.get(attributeName);
			if (result != null) {
				return result;
			}
		}

		return Collections.emptySet();
	}
}
