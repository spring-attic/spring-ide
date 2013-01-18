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

import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;

/**
 * @author Terry Denney
 * @since 2.6
 */
public class AnnotationComputerRegistry {

	public static JavaCompletionProposalComputer[] computers = new JavaCompletionProposalComputer[] {
			new QualifierArgumentProposalComputer(), new QualifierCompletionProposalComputer(),
			new RequestMappingVariableProposalComputer(), new RequestMappingParamTypeProposalComputer(),
			new ConfigurationLocationProposalComputer() };
}
