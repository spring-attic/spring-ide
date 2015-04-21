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
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.PrefixFinder;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.reconcile.IndexNavigator;

/**
 * Represents a context relative to which we can provide content assistance.
 */
public abstract class YamlAssistContext {

// This may prove useful later but we don't need it for now
//	/**
//	 * AssistContextKind is an classification of the different kinds of
//	 * syntactic context that CA can be invoked from.
//	 */
//	public static enum Kind {
//		SKEY_KEY, /* CA called from a SKeyNode and node.isInKey(cursor)==true */
//		SKEY_VALUE, /* CA called from a SKeyNode and node.isInKey(cursor)==false */
//		SRAW /* CA called from a SRawNode */
//	}
//	protected final Kind contextKind;

	protected final YamlPath contextPath;

	public YamlAssistContext(YamlPath contextPath) {
		this.contextPath = contextPath;
	}

	private static class IndexContext extends YamlAssistContext {

		private static PrefixFinder prefixfinder = new PrefixFinder() {
			protected boolean isPrefixChar(char c) {
				return !Character.isWhitespace(c);
			}
		};

		private IndexNavigator indexNav;
		PropertyCompletionFactory completionFactory;

		public IndexContext(YamlPath contextPath, IndexNavigator indexNav, PropertyCompletionFactory completionFactory) {
			super(contextPath);
			this.indexNav = indexNav;
			this.completionFactory = completionFactory;
		}

		@Override
		public Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) throws Exception {
			String query = prefixfinder.getPrefix(doc.getDocument(), offset);
			Collection<Match<PropertyInfo>> matchingProps = indexNav.findMatching(query);
			if (!matchingProps.isEmpty()) {
				ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
				for (Match<PropertyInfo> match : matchingProps) {
					ProposalApplier edits = createEdits(doc, offset, query, match);
					completions.add(completionFactory.property(
							doc.getDocument(), offset, edits, match
					));
				}
				return completions;
			}
			return Collections.emptyList();
		}

		protected ProposalApplier createEdits(final YamlDocument doc,
				final int offset, final String query, final Match<PropertyInfo> match)
				throws Exception {
			//Edits created lazyly as they are somwehat expensive to compute and mostly
			// we need only the edits for the one proposal that user picks.
			return new LazyProposalApplier() {
				@Override
				protected ProposalApplier create() throws Exception {
					YamlPathEdits edits = new YamlPathEdits(doc);

					int queryOffset = offset-query.length();
					edits.delete(queryOffset, query);

					YamlPath propertyPath = YamlPath.fromProperty(match.data.getId());
					YamlPath relativePath = propertyPath.dropFirst(contextPath.size());
					YamlPathSegment nextSegment = relativePath.getSegment(0);
					SNode contextNode = contextPath.traverse((SNode)doc.getStructure());
					//To determine if this completion is 'in place' or needs to be inserted
					// elsewhere in the tree, we check whether a node already exists in our
					// context. If it doesn't we can create it as any child of the context
					// so that includes, right at place the user is typing now.
					SNode existingNode = contextNode.traverse(nextSegment);
					if (existingNode==null) {
						edits.createPathInPlace(contextNode, relativePath, queryOffset);
					} else {
						edits.createPath(YamlPath.fromProperty(match.data.getId()));
					}
					return edits;
				}
			};
		}

		@Override
		protected YamlAssistContext navigate(YamlPathSegment s) {
			if (s.getType()==YamlPathSegmentType.AT_SCALAR_KEY) {
				IndexNavigator subIndex = indexNav.selectSubProperty(s.toPropString());
				if (subIndex.getExtensionCandidate()!=null) {
					return new IndexContext(contextPath.append(s), subIndex, completionFactory);
				} else if (subIndex.getExactMatch()!=null) {
					//TODO: transition to a 'TypeContext' for the type the property is bound to
				}
			}
			//Unsuported navigation => no context for assist
			return null;
		}

		@Override
		public String toString() {
			return "YamlAssistIndexContext("+indexNav+")";
		}

	}

	public static YamlAssistContext global(FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory) {
		return new IndexContext(YamlPath.EMPTY, IndexNavigator.with(index), completionFactory);
	}

	public abstract Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) throws Exception;

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
