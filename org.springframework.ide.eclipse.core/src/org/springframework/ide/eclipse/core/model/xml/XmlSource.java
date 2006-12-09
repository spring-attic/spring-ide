/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.model.xml;

import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.model.IModelSource;
import org.w3c.dom.Node;

/**
 * Storage for an <code>IModelElement</code>'s XML source information
 * retrieved via {@link XmlSourceExtractor}.
 * 
 * @author Torsten Juergeleit
 */
public class XmlSource implements IModelSource {

	private Resource resource;
	private String localName;
	private String prefix;
	private String namespaceURI;
	private int startLine;
	private int endLine;

	public XmlSource(Resource resource, Node node, int startLine,
			int endLine) {
		this.resource = resource;
		this.localName = node.getLocalName();
		this.prefix = node.getPrefix();
		this.namespaceURI = node.getNamespaceURI();
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public Resource getResource() {
		return resource;
	}

	public String getNodeName() {
		return (prefix == null ? localName : prefix + ':' + localName);
	}

	public String getPrefix() {
		return prefix;
	}

	public String getLocalName() {
		return localName;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public int getStartLine() {
		return startLine;
	}
	
	public int getEndLine() {
		return endLine;
	}

	public String toString() {
		return "XmlSource: resource=" + resource + ", nodeName="
				+ getNodeName() + ", startLine=" + startLine + ", endLine="
				+ endLine;
	}
}
