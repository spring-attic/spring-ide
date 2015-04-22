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

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.PrefixFinder;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPath;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.reconcile.IndexNavigator;

import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode.*;

/**
 * Represents a context relative to which we can provide content assistance.
 */
public abstract class YamlAssistContext {

	private boolean preferLowerCasedEnums = true; //make user configurable?

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
	protected final TypeUtil typeUtil;

	public YamlAssistContext(YamlPath contextPath, TypeUtil typeUtil) {
		this.contextPath = contextPath;
		this.typeUtil = typeUtil;
	}

	/**
	 * Computes the text that should be appended at the end of a completion
	 * proposal depending on what type of value is expected.
	 */
	protected String appendTextFor(Type type) {
		//Note that proper indentation after each \n" is added automatically
		//so the strings created here do not need to contain indentation spaces.
		if (TypeUtil.isMap(type)) {
			//ready to enter nested map key on next line
			return "\n";
		} if (TypeUtil.isArrayLike(type)) {
			//ready to enter sequence element on next line
			return "\n- ";
		} else if (typeUtil.isAtomic(type)) {
			//ready to enter whatever on the same line
			return " ";
		} else {
			//Assume its some kind of pojo bean
			return "\n";
		}
	}

	private static PrefixFinder prefixfinder = new PrefixFinder() {
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c) && c!=':';
		}
	};

	public class TypeContext extends YamlAssistContext {

		private PropertyCompletionFactory completionFactory;
		private Type type;

		public TypeContext(YamlPath contextPath, Type type, PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
			super(contextPath, typeUtil);
			this.completionFactory = completionFactory;
			this.type = type;
		}

		@Override
		public Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) throws Exception {
			String query = prefixfinder.getPrefix(doc.getDocument(), offset);
			EnumCaseMode enumCaseMode;
			if (query.isEmpty()) {
				enumCaseMode = preferLowerCasedEnums?LOWER_CASE:ORIGNAL;
			} else {
				enumCaseMode = ALIASED; // will match candidates from both lower and original based on what user typed
			}
			String[] values = typeUtil.getAllowedValues(type, enumCaseMode);
			if (values!=null) {
				ArrayList<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
				int sortingOrder = 0;
				for (String value : values) {
					if (value.startsWith(query)) {
						DocumentEdits edits = new DocumentEdits(doc.getDocument());
						edits.delete(offset-query.length(), offset);
						edits.insert(offset, value);
						completions.add(completionFactory.valueProposal(value, type, sortingOrder++, edits));
					}
				}
				return completions;
			}
			return Collections.emptyList();
		}

		@Override
		protected YamlAssistContext navigate(YamlPathSegment s) {
			//TODO: implement
			return null;
		}

	}



	private static class IndexContext extends YamlAssistContext {

		private IndexNavigator indexNav;
		PropertyCompletionFactory completionFactory;

		public IndexContext(YamlPath contextPath, IndexNavigator indexNav,
				PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
			super(contextPath, typeUtil);
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
					String appendText = appendTextFor(TypeParser.parse(match.data.getType()));
					if (existingNode==null) {
						edits.createPathInPlace(contextNode, relativePath, queryOffset, appendText);
					} else {
						String wholeLine = doc.getLineTextAtOffset(queryOffset);
						if (wholeLine.trim().equals(query.trim())) {
							edits.deleteLineBackwardAtOffset(queryOffset);
						}
						edits.createPath(YamlPath.fromProperty(match.data.getId()), appendText);
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
					return new IndexContext(contextPath.append(s), subIndex, completionFactory, typeUtil);
				} else if (subIndex.getExactMatch()!=null) {
					PropertyInfo prop = subIndex.getExactMatch();
					return new TypeContext(contextPath.append(s), TypeParser.parse(prop.getType()), completionFactory, typeUtil);
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

	public static YamlAssistContext global(FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
		return new IndexContext(YamlPath.EMPTY, IndexNavigator.with(index), completionFactory, typeUtil);
	}

	public abstract Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) throws Exception;

	public static YamlAssistContext forPath(YamlPath contextPath,  FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
		YamlAssistContext context = YamlAssistContext.global(index, completionFactory, typeUtil);
		for (YamlPathSegment s : contextPath.getSegments()) {
			if (context==null) return null;
			context = context.navigate(s);
		}
		return context;
	}

	protected abstract YamlAssistContext navigate(YamlPathSegment s);

}
