/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor.completions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.processing.Completion;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.PrefixFinder;
import org.springframework.ide.eclipse.yaml.editor.reconcile.IndexNavigator;

/**
 * Represents a context relative to which we can provide content assistance.
 */
public abstract class YamlAssistContext {

	private static class IndexContext extends YamlAssistContext {

		private static PrefixFinder prefixfinder = new PrefixFinder() {
			protected boolean isPrefixChar(char c) {
				return !Character.isWhitespace(c);
			}
		};

		private IndexNavigator indexNav;
		PropertyCompletionFactory completionFactory;

		public IndexContext(IndexNavigator indexNav, PropertyCompletionFactory completionFactory) {
			this.indexNav = indexNav;
			this.completionFactory = completionFactory;
		}

		@Override
		public Collection<ICompletionProposal> getCompletions(IDocument doc, int offset) {
			String query = prefixfinder.getPrefix(doc, offset);
			Collection<Match<PropertyInfo>> matchingProps = indexNav.findMatching(query);
			if (!matchingProps.isEmpty()) {
				ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
				for (Match<PropertyInfo> match : matchingProps) {
					completions.add(completionFactory.property(
							doc, offset,
							completionFactory.replace(offset-query.length(), query, match.data.getId()+": "),
							match
					));
				}
				return completions;
			}
			return Collections.emptyList();
		}

	}

	public static YamlAssistContext global(FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory) {
		return new IndexContext(IndexNavigator.with(index), completionFactory);
	}

	public abstract Collection<ICompletionProposal> getCompletions(IDocument doc, int offset);

}
