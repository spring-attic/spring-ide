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
package org.springframework.ide.eclipse.quickfix.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.correction.NameMatcher;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.proposals.RenameToSimilarNameQuickFixProposal;


/**
 * Quick assist processor for bean property attribute in beans XML editor.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class PropertyAttributeQuickAssistProcessor extends BeanQuickAssistProcessor {

	public enum Type {
		GETTER, SETTER
	}

	private final String className;

	private final IJavaProject javaProject;

	private final IProject project;

	private final Type type;;

	public PropertyAttributeQuickAssistProcessor(int offset, int length, String className, String propertyName,
			IProject project, boolean missingEndQuote, Type type) {
		super(offset, length, propertyName, missingEndQuote);
		this.className = className;
		this.project = project;
		this.type = type;
		this.javaProject = JavaCore.create(project);
	}

	private void addPropertyName(String property, Set<String> properties) {
		if (NameMatcher.isSimilarName(property, text)) {
			if (property.length() > 1 && Character.isUpperCase(property.charAt(1))) {
				properties.add(property);
			}
			else {
				properties.add(property.substring(0, 1).toLowerCase() + property.substring(1));
			}
		}
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		List<String> similarProperties = getSimilarProperties();
		for (String similarProperty : similarProperties) {
			RenameToSimilarNameQuickFixProposal p = new RenameToSimilarNameQuickFixProposal(similarProperty, offset,
					length, missingEndQuote);
			proposals.add(p);
		}

		ICompletionProposal proposal = QuickfixUtils
				.getNewMethodQuickFixProposal(getNewMethodName(), null, getMethodParamTypes(), javaProject, className,
						offset, length, text, missingEndQuote, false, "property");
		if (proposal != null) {
			proposals.add(proposal);
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private String[] getMethodParamTypes() {
		switch (type) {
		case SETTER:
			return new String[] { "Object" };
		case GETTER:
			return new String[] {};
		}
		return null;
	}

	private String getNewMethodName() {
		String firstChar = text.substring(0, 1);
		switch (type) {
		case SETTER:
			return "set" + firstChar.toUpperCase() + text.substring(1);
		case GETTER:
			return "get" + firstChar.toUpperCase() + text.substring(1);
		}
		return null;
	}

	private List<String> getSimilarProperties() {
		HashSet<String> properties = new HashSet<String>();
		IType type = JdtUtils.getJavaType(project, className);
		if (type != null) {
			try {
				Set<IMethod> methods = Introspector.findAllWritableProperties(type);
				for (IMethod method : methods) {
					String methodName = method.getElementName();
					if (methodName.startsWith("set")) {
						String propertyName = methodName.replace("set", "");
						addPropertyName(propertyName, properties);
					}
				}
			}
			catch (JavaModelException e) {
			}
		}

		List<String> result = new ArrayList<String>(properties);
		Collections.sort(result, new NameSuggestionComparator(text));
		return result;
	}

}
