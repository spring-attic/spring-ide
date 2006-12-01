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

package org.springframework.ide.eclipse.core.io.xml;

import org.springframework.core.io.Resource;

/**
 * Storage for XML source information retrived via {@link XmlSourceExtractor}.
 * @author Torsten Juergeleit
 */
public class XmlSource {

	private Resource resource;
	private String nodeName;
	private int startLine;
	private int endLine;

	public XmlSource(Resource resource, String nodeName, int startLine,
			int endLine) {
		this.resource = resource;
		this.nodeName = nodeName;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public Resource getResource() {
		return resource;
	}

	public String getNodeName() {
		return nodeName;
	}

	public int getStartLine() {
		return startLine;
	}
	
	public int getEndLine() {
		return endLine;
	}

	public String toString() {
		return "LineNumberSource: resource=" + resource + ", nodeName="
				+ nodeName + ", startLine=" + startLine + ", endLine=" + endLine;
	}
}
