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

import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.w3c.dom.Node;

/**
 * A {@link SourceExtractor} implementation which retrieves
 * {@link XmlSourceLocation XML source location} from a given
 * {@link Node DOM node} using Apache's Xerces XML parser.
 * 
 * @author Torsten Juergeleit
 */
public class XmlSourceExtractor implements SourceExtractor {

	public Object extractSource(Object sourceCandidate,
			Resource definingResource) {

		if (sourceCandidate instanceof Node) {
			Node node = (Node) sourceCandidate;
			int startLine = LineNumberPreservingDOMParser
					.getStartLineNumber(node);
			int endLine = LineNumberPreservingDOMParser.getEndLineNumber(node);
			return new XmlSourceLocation(definingResource, node, startLine,
					endLine);
		}
		return sourceCandidate;
	}
}
