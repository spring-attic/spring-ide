/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core;

import org.springframework.core.NestedRuntimeException;
import org.springframework.ide.eclipse.beans.core.internal.parser.LineNumberPreservingDOMParser;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

public class BeanDefinitionException extends NestedRuntimeException {

	private int lineNumber;

	public BeanDefinitionException(Node node, String message, Throwable cause) {
		super(message, cause);
		this.lineNumber = LineNumberPreservingDOMParser.getStartLineNumber(node);
	}

	public BeanDefinitionException(Node node, String message) {
		super(message);
		this.lineNumber = LineNumberPreservingDOMParser.getStartLineNumber(node);
	}

	public BeanDefinitionException(Node node, Throwable cause) {
		super(cause.getMessage(), cause);
		this.lineNumber = LineNumberPreservingDOMParser.getStartLineNumber(node);
	}

	public BeanDefinitionException(int lineNumber, String message,
								   Throwable cause) {
		super(message, cause);
		this.lineNumber = lineNumber;
	}

	public BeanDefinitionException(int lineNumber, String message) {
		super(message);
		this.lineNumber = lineNumber;
	}

	public BeanDefinitionException(int lineNumber, Throwable cause) {
		super(cause.getMessage(), cause);
		this.lineNumber = lineNumber;
	}

	public BeanDefinitionException(String message) {
		super(message);
		this.lineNumber = -1;
	}

	public BeanDefinitionException(Throwable cause) {
		super(cause.getMessage(), cause);
		int line = -1;
		if (cause instanceof SAXParseException) {
			line = ((SAXParseException) cause).getLineNumber();
		}
		this.lineNumber = line;
	}

	public int getLineNumber() {
		return lineNumber;	
	}

	public String getMessage() {
		return (getCause() != null ? getCause().getMessage() : super.getMessage());
	}
}
