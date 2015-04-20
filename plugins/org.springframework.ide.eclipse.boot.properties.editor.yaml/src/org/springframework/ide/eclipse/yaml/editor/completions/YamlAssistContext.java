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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.PrefixFinder;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.AtScalarKey;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.YamlPathSegmentType;
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
		public Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) {
			String query = prefixfinder.getPrefix(doc.getDocument(), offset);
			Collection<Match<PropertyInfo>> matchingProps = indexNav.findMatching(query);
			if (!matchingProps.isEmpty()) {
				ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
//				for (Match<PropertyInfo> match : matchingProps) {
//					completions.add(completionFactory.property(
//							doc.getDocument(), offset,
//							new YamlDocumentModifier(doc)
//								.delete(offset-query.length(), query)
//								.createPath(YamlPath.fromProperty(match.data.getId())),
//							match
//					));
//				}
				return completions;
			}
			return Collections.emptyList();
		}

		@Override
		protected YamlAssistContext navigate(YamlPathSegment s) {
			if (s.getType()==YamlPathSegmentType.AT_SCALAR_KEY) {
				IndexNavigator subIndex = indexNav.selectSubProperty(s.toPropString());
				if (subIndex.getExtensionCandidate()!=null) {
					return new IndexContext(subIndex, completionFactory);
				} else if (subIndex.getExactMatch()!=null) {
					//TODO: transition to a 'TypeContext' for the type the property is bound to
				}
			}
			//Unsuported navigation => no context for assist
			return null;
		}

	}

	public static YamlAssistContext global(FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory) {
		return new IndexContext(IndexNavigator.with(index), completionFactory);
	}

	public abstract Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset);

	public static YamlAssistContext forPath(YamlPath contextPath,  FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory) {
		YamlAssistContext context = YamlAssistContext.global(index, completionFactory);
		for (YamlPathSegment s : contextPath.getSegments()) {
			if (context==null) return null;
			context = context.navigate(s);
		}
		return context;
	}

	protected abstract YamlAssistContext navigate(YamlPathSegment s);

}
