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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Terry Denney
 */
public class ControllerTest {
	
	private class NoAnnotation {
		@RequestMapping("/")
		public void requestMappingAnnotation() {
		}
		
		public void noAnnotation() {
		}	
	}
	
	private class NoAnnotationNoRequestMapping {
		public void noAnnotation() {
		}
	}
	
	@RequestMapping
	private class RequestMappingAnnotationNoRequestMapping {
		public void noAnnotation() {
		}
	}
	
	@RequestMapping
	private class RequestMappingAnnotation {
		@RequestMapping("/")
		public void requestMappingAnnotation() {
		}
		
		public void noAnnotation() {
		}
	}
	
	@Controller
	private class ControllerAnnotation {
		@RequestMapping("/")
		public void requestMappingAnnotation() {
		}
		
		public void noAnnotation() {
		}
	}
	
	@Controller
	private class ControllerAnnotationNoRequestMapping {		
		public void noAnnotation() {
		}
	}
	
	@Controller
	@RequestMapping("/")
	private class BothAnnotationsNoRequestMapping {
		public void noAnnotation() {
		}
	}

	@Controller
	@RequestMapping("/")
	private class BothAnnotations {
		@RequestMapping("/")
		public void requestMappingAnnotation() {
		}
		
		public void noAnnotation() {
		}
	}
	
}
