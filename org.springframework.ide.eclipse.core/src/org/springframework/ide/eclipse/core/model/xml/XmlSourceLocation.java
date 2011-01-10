/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.xml;

import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Node;

/**
 * Storage for an {@link IModelElement}'s XML source location retrieved
 * via {@link XmlSourceExtractor}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
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
	
	public XmlSourceLocation(XmlSourceLocation location) {
		resource = location.getResource();
		localName = location.getLocalName();
		prefix = location.getPrefix();
		namespaceURI = location.getNamespaceURI();
		startLine = location.getStartLine();
		endLine = location.getEndLine();
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public String getNodeName() {
		return (prefix == null ? localName : prefix + ':' + localName);
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getLocalName() {
		return localName;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getEndLine() {
		return endLine;
	}

	@Override
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

	@Override
	public int hashCode() {
		int hashCode = 29 * ObjectUtils.nullSafeHashCode(resource);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(localName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(prefix);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(namespaceURI);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(startLine);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(endLine);
	}

	@Override
	public String toString() {
		return "XmlSource: resource=" + resource + ", nodeName="
				+ getNodeName() + ", startLine=" + startLine + ", endLine="
				+ endLine;
	}
}
