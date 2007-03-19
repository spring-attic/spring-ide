/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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