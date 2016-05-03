/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
public class ResourceHintProvider implements ValueProviderStrategy {

	final private ImmutableList<ValueHint> urlPrefixHints = ImmutableList.copyOf(
			Arrays.stream(new String[] {
					"file:///",
					"classpath:",
					"classpath*:",
					"http://",
					"https://"
			})
			.map(ValueHint::withValue)
			.collect(Collectors.toList())
	);

	@Override
	public Collection<ValueHint> getValues(IJavaProject javaProject, String query) {
		return urlPrefixHints;
	}

}
