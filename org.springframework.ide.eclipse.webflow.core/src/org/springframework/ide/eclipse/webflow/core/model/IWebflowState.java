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
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IWebflowState extends ITransitionableFrom {

	/**
	 * Adds the state.
	 * 
	 * @param state the state
	 */
	void addState(IState state);

	/**
	 * Gets the vars.
	 * 
	 * @return the vars
	 */
	List<IVar> getVars();

	/**
	 * Adds the var.
	 * 
	 * @param var the var
	 */
	void addVar(IVar var);

	/**
	 * Adds the var.
	 * 
	 * @param i the i
	 * @param var the var
	 */
	void addVar(IVar var, int i);

	/**
	 * Move var.
	 * 
	 * @param i the i
	 * @param var the var
	 */
	void moveVar(IVar var, int i);

	/**
	 * Removes the var.
	 * 
	 * @param var the var
	 */
	void removeVar(IVar var);

	/**
	 * Gets the imports.
	 * 
	 * @return the imports
	 */
	List<IImport> getImports();

	/**
	 * Adds the import.
	 * 
	 * @param ip the ip
	 */
	void addImport(IImport ip);

	/**
	 * Adds the import.
	 * 
	 * @param i the i
	 * @param ip the ip
	 */
	void addImport(IImport ip, int i);

	/**
	 * Move import.
	 * 
	 * @param i the i
	 * @param ip the ip
	 */
	void moveImport(IImport ip, int i);

	/**
	 * Removes the import.
	 * 
	 * @param ip the ip
	 */
	void removeImport(IImport ip);

	/**
	 * Gets the start state.
	 * 
	 * @return the start state
	 */
	IState getStartState();

	/**
	 * Sets the start state.
	 * 
	 * @param state the start state
	 */
	void setStartState(IState state);

	/**
	 * Checks for start state.
	 * 
	 * @return true, if has start state
	 */
	boolean hasStartState();

	/**
	 * Checks if is start state.
	 * 
	 * @param state the state
	 * 
	 * @return true, if is start state
	 */
	boolean isStartState(IState state);

	/**
	 * Gets the input mapper.
	 * 
	 * @return the input mapper
	 */
	IInputMapper getInputMapper();

	/**
	 * Gets the output mapper.
	 * 
	 * @return the output mapper
	 */
	IOutputMapper getOutputMapper();

	/**
	 * Sets the input mapper.
	 * 
	 * @param inputMapper the input mapper
	 */
	void setInputMapper(IInputMapper inputMapper);

	/**
	 * Sets the output mapper.
	 * 
	 * @param outputMapper the output mapper
	 */
	void setOutputMapper(IOutputMapper outputMapper);

	List<IInlineFlowState> getInlineFlowStates();

	/**
	 * Adds the state.
	 * 
	 * @param state the state
	 */
	void addInlineFlowState(IInlineFlowState state);

	/**
	 * Adds the state.
	 * 
	 * @param i the i
	 * @param state the state
	 */
	void addInlineFlowState(IInlineFlowState state, int i);

	/**
	 * Move state.
	 * 
	 * @param i the i
	 * @param state the state
	 */
	void moveInlineFlowState(IInlineFlowState state, int i);

	/**
	 * Removes the state.
	 * 
	 * @param state the state
	 */
	void removeInlineFlowState(IInlineFlowState state);

	/**
	 * Adds the state.
	 * 
	 * @param i the i
	 * @param state the state
	 */
	void addState(IState state, int i);

	/**
	 * Gets the states.
	 * 
	 * @return the states
	 */
	List<IState> getStates();

	/**
	 * Move state.
	 * 
	 * @param i the i
	 * @param state the state
	 */
	void moveState(IState state, int i);

	/**
	 * Removes the state.
	 * 
	 * @param state the state
	 */
	void removeState(IState state);

	IGlobalTransitions getGlobalTransitions();

	void setGlobalTransitions(IGlobalTransitions inputMapper);

}
