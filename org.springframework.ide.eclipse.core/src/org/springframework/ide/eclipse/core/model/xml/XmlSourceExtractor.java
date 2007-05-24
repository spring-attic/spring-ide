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
package org.springframework.ide.eclipse.core.model.xml;

import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.w3c.dom.Node;

/**
 * A {@link SourceExtractor} implementation which retrieves
 * {@link XmlSourceLocation} from a given
 * {@link Node} using Apache's Xerces XML parser.
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
