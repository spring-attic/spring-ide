/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.core;

import org.springframework.core.NestedRuntimeException;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

public class WebFlowDefinitionException extends NestedRuntimeException {

    private int lineNumber;

    public WebFlowDefinitionException(Node node, String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = LineNumberPreservingDOMParser
                .getStartLineNumber(node);
    }

    public WebFlowDefinitionException(Node node, String message) {
        super(message);
        this.lineNumber = LineNumberPreservingDOMParser
                .getStartLineNumber(node);
    }

    public WebFlowDefinitionException(Node node, Throwable cause) {
        super(cause.getMessage(), cause);
        this.lineNumber = LineNumberPreservingDOMParser
                .getStartLineNumber(node);
    }

    public WebFlowDefinitionException(int lineNumber, String message,
            Throwable cause) {
        super(message, cause);
        this.lineNumber = lineNumber;
    }

    public WebFlowDefinitionException(int lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public WebFlowDefinitionException(int lineNumber, Throwable cause) {
        super(cause.getMessage(), cause);
        this.lineNumber = lineNumber;
    }

    public WebFlowDefinitionException(String message) {
        super(message);
        this.lineNumber = -1;
    }

    public WebFlowDefinitionException(Throwable cause) {
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
        return (getCause() != null ? getCause().getMessage() : super
                .getMessage());
    }
}