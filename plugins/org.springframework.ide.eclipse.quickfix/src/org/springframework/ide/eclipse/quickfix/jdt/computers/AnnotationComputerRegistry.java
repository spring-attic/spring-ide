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
			new ConfigurationLocationProposalComputer(), new SimpleClassProposalComputer() };

	public final static String DEFAULT_ATTRIBUTE_NAME = "value";

	// public final static String DEFAULT_ARRAY_ATTRIBUTE = "ARRAY_ATTRIBUTE";

	/**
	 * annotation name -> attribute name -> proposal computer
	 */
	public static Map<String, Map<String, Set<AnnotationProposalComputer>>> annotationArgComputers = null;

	public static Map<String, Map<String, Set<AnnotationProposalComputer>>> annotationArgComputersForArray = null;

	private static void init() {
		annotationArgComputers = new HashMap<String, Map<String, Set<AnnotationProposalComputer>>>();
		annotationArgComputersForArray = new HashMap<String, Map<String, Set<AnnotationProposalComputer>>>();

		addProposalComputer("Qualifier", new QualifierArgumentProposalComputer());

		addProposalComputer("RequestMapping", new RequestMappingVariableProposalComputer());

		addProposalComputer("ContextConfiguration", "locations", new ConfigurationLocationProposalComputer());
		addProposalComputer("ContextConfiguration", "value", new ConfigurationLocationProposalComputer());

		addProposalComputer("ComponentScan", new PackageNameProposalComputer());
		addProposalComputerForArrayAttribute("ComponentScan", new PackageNameProposalComputer());
		addProposalComputerForArrayAttribute("ComponentScan", "basePackages", new PackageNameProposalComputer());
	}

	private static void addProposalComputer(String annotationName, AnnotationProposalComputer proposalComputer) {
		addProposalComputer(annotationName, DEFAULT_ATTRIBUTE_NAME, proposalComputer);
	}

	private static void addProposalComputerForArrayAttribute(String annotationName,
			AnnotationProposalComputer proposalComputer) {
		addProposalComputerForArrayAttribute(annotationName, DEFAULT_ATTRIBUTE_NAME, proposalComputer);
	}

	private static void addProposalComputer(String annotationName, String attributeName,
			AnnotationProposalComputer proposalComputer) {
		addProposalComputerHelper(annotationName, attributeName, proposalComputer, annotationArgComputers);
	}

	private static void addProposalComputerForArrayAttribute(String annotationName, String attributeName,
			AnnotationProposalComputer proposalComputer) {
		addProposalComputerHelper(annotationName, attributeName, proposalComputer, annotationArgComputersForArray);
	}

	private static void addProposalComputerHelper(String annotationName, String attributeName,
			AnnotationProposalComputer proposalComputer, Map<String, Map<String, Set<AnnotationProposalComputer>>> map) {
		if (map != null) {
			Map<String, Set<AnnotationProposalComputer>> attributeToComputers = map.get(annotationName);
			if (attributeToComputers == null) {
				attributeToComputers = new HashMap<String, Set<AnnotationProposalComputer>>();
				map.put(annotationName, attributeToComputers);
			}

			Set<AnnotationProposalComputer> computers = attributeToComputers.get(attributeName);
			if (computers == null) {
				computers = new HashSet<AnnotationProposalComputer>();
				attributeToComputers.put(attributeName, computers);
			}

			computers.add(proposalComputer);
		}
	}

	public static Set<AnnotationProposalComputer> getProposalComputer(String annotationName) {
		return getProposalComputer(annotationName, DEFAULT_ATTRIBUTE_NAME);
	}

	public static Set<AnnotationProposalComputer> getProposalComputer(String annotationName, String attributeName) {
		if (annotationArgComputers == null) {
			init();
		}

		return getProposalComputerHelper(annotationName, attributeName, annotationArgComputers);
	}

	public static Set<AnnotationProposalComputer> getProposalComputerForArrayAttribute(String annotationName) {
		return getProposalComputerForArrayAttribute(annotationName, DEFAULT_ATTRIBUTE_NAME);
	}

	public static Set<AnnotationProposalComputer> getProposalComputerForArrayAttribute(String annotationName,
			String attributeName) {
		if (annotationArgComputersForArray == null) {
			init();
		}

		return getProposalComputerHelper(annotationName, attributeName, annotationArgComputersForArray);
	}

	private static Set<AnnotationProposalComputer> getProposalComputerHelper(String annotationName,
			String attributeName, Map<String, Map<String, Set<AnnotationProposalComputer>>> map) {
		Map<String, Set<AnnotationProposalComputer>> attributeToComputers = map.get(annotationName);
		if (attributeToComputers != null) {
			Set<AnnotationProposalComputer> result = attributeToComputers.get(attributeName);
			if (result != null) {
				return result;
			}
		}

		return Collections.emptySet();
	}
}
