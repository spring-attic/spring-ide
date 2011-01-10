/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.webflow.v1.config;

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.webflow.execution.repository.continuation.ClientContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.continuation.ContinuationFlowExecutionRepository;
import org.springframework.webflow.execution.repository.support.SimpleFlowExecutionRepository;

/**
 * Type-safe enumeration of logical flow execution repository types.
 * @author Christian Dupuis
 * @author Keith Donald
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepository
 */
@SuppressWarnings("serial")
public class RepositoryType extends StaticLabeledEnum {

	/**
	 * The 'simple' flow execution repository type.
	 * @see SimpleFlowExecutionRepository
	 */
	public static final RepositoryType SIMPLE = new RepositoryType(0, "Simple");

	/**
	 * The 'continuation' flow execution repository type.
	 * @see ContinuationFlowExecutionRepository
	 */
	public static final RepositoryType CONTINUATION = new RepositoryType(1, "Continuation");

	/**
	 * The 'client' (continuation) flow execution repository type.
	 * @see ClientContinuationFlowExecutionRepository
	 */
	public static final RepositoryType CLIENT = new RepositoryType(2, "Client");

	/**
	 * The 'singleKey' flow execution repository type.
	 * @see SimpleFlowExecutionRepository
	 * @see SimpleFlowExecutionRepository#setAlwaysGenerateNewNextKey(boolean)
	 */
	public static final RepositoryType SINGLEKEY = new RepositoryType(3, "Single Key");
	
	/**
	 * Private constructor because this is a typesafe enum!
	 */
	private RepositoryType(int code, String label) {
		super(code, label);
	}
}