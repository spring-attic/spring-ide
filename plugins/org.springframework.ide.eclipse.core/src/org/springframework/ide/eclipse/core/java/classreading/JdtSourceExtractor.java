/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.core.io.Resource;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class JdtSourceExtractor implements SourceExtractor {

	public Object extractSource(Object sourceCandidate, Resource definingResource) {
		try {
			if (sourceCandidate instanceof JdtConnectedMetadata) {
				return ((JdtConnectedMetadata) sourceCandidate).createSourceLocation();
			}
		}
		catch (JavaModelException e) {
			throw new JdtMetadataReaderException(e);
		}
		return sourceCandidate;
	}

}
