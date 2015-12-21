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
package org.springframework.ide.eclipse.quickfix.jdt.processors;

/**
 * @author Terry Denney
 * @author Kaitlin Duck Sherwood
 */
public class AnnotationQuickAssistProcessorRegistry {

	public static AbstractAnnotationQuickAssistProcessor[] processors = new AbstractAnnotationQuickAssistProcessor[] {
			new AutowiredAnnotationQuickAssistProcessor(), new QualifierAnnotationQuickAssistProcessor(),
			new AutowireRequiredNotFoundAnnotationQuickAssistProcessor(),
			new ControllerAnnotationQuickAssistProcessor(), new RequestMappingParamAnnotationQuickAssistProcessor(),
			new InitBinderAnnotationQuickAssistProcessor(), new ExceptionHandlerAnnotationQuickAssistProcessor(),
			new ResponseBodyAnnotationQuickAssistProcessor(), new AddAutowireConstructorQuickAssistProcessor()
	// path variable quick assist is implemented in marker resolution generator
	// , new PathVariableAnnotationQuickAssistProcessor()
	// , new RequestMappingAnnotationQuickAssistProcessor()
	};

}
