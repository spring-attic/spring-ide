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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Kris De Volder
 */
public class SpringPropertyProblem {

	private String msg;
	private int length;
	private int offset;

	private ProblemType type;

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

	public List<ICompletionProposal> getQuickfixes(IPreferenceStore preferences) {
		ICompletionProposal prop = new IgnoreProblemTypeQuickfix(preferences, getType());
		return Collections.singletonList(prop);
	}
}
