/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType.PROP_UNKNOWN_PROPERTY;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemType.YAML_UNKNOWN_PROPERTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.IgnoreProblemTypeQuickfix;
import org.springframework.ide.eclipse.boot.properties.editor.quickfix.QuickfixContext;

/**
 * @author Kris De Volder
 */
public class SpringPropertyProblem {

	private static final EnumSet<ProblemType> FIXABLE_UNKNOWN_PROPERTY_PROBLEM_TYPES = EnumSet.of(
			PROP_UNKNOWN_PROPERTY,
			YAML_UNKNOWN_PROPERTY
	);

	//Mandatory properties (each problem must set them)
	private String msg;
	private int length;
	private int offset;
	private ProblemType type;

	//Optional properties (only some problems or problemtypes may set them, so they might be null)
	private String propertyName;

	/**
	 * Create a SpringProperty file annotation with a given severity.
	 * The severity should be one of the XXX_TYPE constants defined in
	 * {@link SpringPropertyAnnotation}.
	 */
	private SpringPropertyProblem(ProblemType type, String msg, int offset, int length) {
		this.msg = msg;
		this.offset = offset;
		this.length = length;
		this.type = type;
	}

	public String getMessage() {
		return msg;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "@["+offset+","+length+"]: "+msg;
	}

	public ProblemType getType() {
		return type;
	}

	public static SpringPropertyProblem problem(ProblemType problemType, String message, int offset, int len) {
		return new SpringPropertyProblem(problemType, message , offset, len);
	}

	public List<ICompletionProposal> getQuickfixes(QuickfixContext context) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(2);
		if (FIXABLE_UNKNOWN_PROPERTY_PROBLEM_TYPES.contains(type)) {
			String missingProperty = getPropertyName();
			IJavaProject project = context.getJavaProject();
			if (project!=null && missingProperty!=null) {
				proposals.add(new CreateAdditionalMetadataQuickfix(project, missingProperty, context.getUI()));
			}
		}
		proposals.add(new IgnoreProblemTypeQuickfix(context.getPreferences(), getType()));
		return Collections.unmodifiableList(proposals);
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}
