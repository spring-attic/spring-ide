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
package org.springframework.ide.eclipse.beans.core;

import org.springframework.core.NestedRuntimeException;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

/**
 * This exception is thrown if a error while reading Spring bean definition XML
 * files occurs. Optionally it holds the corresponding line number.
 * 
 * @author Torsten Juergeleit
 */
public class BeanDefinitionException extends NestedRuntimeException {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private transient final int lineNumber;

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

	@Override
	public String getMessage() {
		return (getCause() != null ? getCause().getMessage() : super.getMessage());
	}
}
