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
package com.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Terry Denney
 */
public class PathVariableTest {
	
	private class NoAnnotation {
		
		public void noAnnotation(int userId) {
			
		}
		
		@RequestMapping("")
		public void noVariableNoPathVariable(int userId) {
			
		}
		
		@RequestMapping("")
		public void noVariablePathVariable(@PathVariable int userId) {
			
		}
		
		@RequestMapping("{userId}")
		public void variableNoPathVariable(int userId) {
			
		}
		
		@RequestMapping("{userId}")
		public void variablePathVariable(@PathVariable int userId) {
			
		}
		
		@RequestMapping("{userId:\\d+}")
		public void variableNoPathVariableRegEx(int userId) {
			
		}
		
		@RequestMapping("{userId:[0-9]}")
		public void variablePathVariableRegEx(@PathVariable int userId) {
			
		}
		
		@RequestMapping("{userId}/{petId}")
		public void variablesNoPathVariable(int userId, int petId) {
			
		}
		
		@RequestMapping("{userId}/{petId}")
		public void variablesPathVariable(@PathVariable int userId, int petId) {
			
		}
		
		@RequestMapping("{userId}/{petId}")
		public void variablesPathVariables(@PathVariable int userId, @PathVariable int petId) {
			
		}
	}
	
	@RequestMapping("")
	private class NoVariable {
		
		public void variableNoPathVariable(int userId) {
			
		}
		
	}
	
	@RequestMapping("{userId}")
	private class VariableNoPathVariable {
		
		public void variableNoPathVariable(int userId) {
			
		}
		
	}
	
	@RequestMapping("{userId}")
	private class VariableNoPathVariables {
		
		public void variableNoPathVariable(int userId) {
			
		}
		
		public void variableNoPathVariable2(int userId) {
			
		}
	}
	
	@RequestMapping("{userId}")
	private class VariableWithPathVariable {
		
		public void variableNoPathVariable(@PathVariable int userId) {
			
		}
		
	}
	
	@RequestMapping("{user.+}")
	private class VariableWithRegexPathVariable {
		
		public void variableNoPathVariable(@PathVariable int userId) {
			
		}
		
	}
	
	@RequestMapping("{userId.+}")
	private class VariableWithWrongRegexPathVariable {
		
		public void variableNoPathVariable(@PathVariable int userId) {
			
		}
		
	}
	
}
