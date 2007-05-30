/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.List;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IWebflowState extends ITransitionableFrom {

	void addState(IState state);

	List<IVar> getVars();

	void addVar(IVar var);

	void addVar(IVar var, int i);

	void moveVar(IVar var, int i);

	void removeVar(IVar var);

	List<IImport> getImports();

	void addImport(IImport ip);

	void addImport(IImport ip, int i);

	void moveImport(IImport ip, int i);

	void removeImport(IImport ip);

	IState getStartState();

	void setStartState(IState state);

	boolean hasStartState();

	boolean isStartState(IState state);

	IInputMapper getInputMapper();

	IOutputMapper getOutputMapper();

	void setInputMapper(IInputMapper inputMapper);

	void setOutputMapper(IOutputMapper outputMapper);

	List<IInlineFlowState> getInlineFlowStates();

	void addInlineFlowState(IInlineFlowState state);

	void addInlineFlowState(IInlineFlowState state, int i);

	void moveInlineFlowState(IInlineFlowState state, int i);

	void removeInlineFlowState(IInlineFlowState state);

	void addState(IState state, int i);

	List<IState> getStates();

	void moveState(IState state, int i);

	void removeState(IState state);

	IGlobalTransitions getGlobalTransitions();

	void setGlobalTransitions(IGlobalTransitions inputMapper);

}
