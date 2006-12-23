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
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Node;

/**
 * Storage for an <code>IModelElement</code>'s XML source location retrieved
 * via {@link XmlSourceExtractor}.
 * 
 * @author Torsten Juergeleit
 */
public class XmlSourceLocation implements IModelSourceLocation {

	private Resource resource;
	private String localName;
	private String prefix;
	private String namespaceURI;
	private int startLine;
	private int endLine;

	public XmlSourceLocation(Resource resource, Node node, int startLine,
			int endLine) {
		this.resource = resource;
		this.startLine = startLine;
		this.endLine = endLine;

		// If a DOM node given then retrieve the relevant information
		if (node != null) {
			localName = node.getLocalName();
			prefix = node.getPrefix();
			namespaceURI = node.getNamespaceURI();
		}
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

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof XmlSourceLocation)) {
			return false;
		}
		XmlSourceLocation that = (XmlSourceLocation) other;
		if (!ObjectUtils.nullSafeEquals(this.resource, that.resource))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.localName, that.localName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.prefix, that.prefix))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.namespaceURI, that.namespaceURI))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.startLine, that.startLine))
			return false;
		return ObjectUtils.nullSafeEquals(this.endLine, that.endLine);
	}

	public int hashCode() {
		int hashCode = 29 * ObjectUtils.nullSafeHashCode(resource);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(localName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(prefix);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(namespaceURI);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(startLine);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(endLine);
	}

	public String toString() {
		return "XmlSource: resource=" + resource + ", nodeName="
				+ getNodeName() + ", startLine=" + startLine + ", endLine="
				+ endLine;
	}
}
