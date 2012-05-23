/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;

/**
 * Interface to abstract how to get the {@link List} of keywords available for a given {@link IType}.
 * 
 * @author Oliver Gierke
 */
public interface KeywordProvider {

	/**
	 * Returns all keywords suitable for the given {@link IType}.
	 * 
	 * @param type the type to lookup the keywords for.
	 * @param the seed to find keywords for. The provider should only return keywords matching this seed.
	 * @return all keywords suitable for the given {@link IType}, will never be {@literal null}.
	 */
	Set<String> getKeywordsForPropertyOf(IType type, String seed);
}
