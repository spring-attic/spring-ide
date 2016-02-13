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
package org.springframework.ide.eclipse.boot.properties.editor;

import java.util.List;

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;

import com.google.common.collect.ImmutableList;

/**
 * Methods for creating hints providers that provide hint in specific kind of context.
 *
 * @author Kris De Volder
 */
public class HintProviders {

	/**
	 * Create a hint provider that will return the given hints in the context following
	 * a traversal that goes down into a 'domain of' context a given number of times.
	 */
	public static HintProvider forDomainAt(final ImmutableList<ValueHint> valueHints, final int dim) {
		if (dim==0) {
			return forHere(valueHints);
		}
		return new HintProvider() {
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				switch (s.getType()) {
				case VAL_AT_INDEX:
				case VAL_AT_KEY:
					return forDomainAt(valueHints, dim-1);
				default:
					return null;
				}
			}

			public List<ValueHint> getValueHints() {
				return ImmutableList.of();
			}
		};
	}

	/**
	 * Omly returns the given hints in this context but not one of its 'sub contexts'.
	 */
	public static HintProvider forHere(final ImmutableList<ValueHint> valueHints) {
		return new HintProvider() {

			@Override
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				return null;
			}

			@Override
			public List<ValueHint> getValueHints() {
				return valueHints;
			}
		};
	}

	/**
	 * REturns the given hints in this context and any of its subcontexts that expect values.
	 */
	public static HintProvider forAllValueContexts(final ImmutableList<ValueHint> valueHints) {
		return new HintProvider() {
			@Override
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				switch (s.getType()) {
				case VAL_AT_INDEX:
				case VAL_AT_KEY:
					return this;
				default:
					return null;
				}
			}

			@Override
			public List<ValueHint> getValueHints() {
				return valueHints;
			}
		};
	}
}
