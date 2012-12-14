/*******************************************************************************
 * Copyright (c) 2009, 2012 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class JdtSourceExtractor implements SourceExtractor {

	public Object extractSource(Object sourceCandidate, Resource definingResource) {
		if (sourceCandidate instanceof IJdtMethodMetadata) {
			try {
				return new JavaModelSourceLocation(((IJdtMethodMetadata) sourceCandidate).getMethod());
			}
			catch (JavaModelException e) {
				throw new JdtMetadataReaderException(e);
			}
		}
		return sourceCandidate;
	}

}
