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
package org.springframework.ide.eclipse.boot.properties.editor.yaml.completions;

import static org.eclipse.jdt.internal.ui.text.javadoc.JavadocContentAccess2.getHTMLContent;
import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode.ALIASED;
import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode.LOWER_CASE;
import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode.ORIGNAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.HoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.completions.DocumentEdits;
import org.springframework.ide.eclipse.boot.properties.editor.completions.LazyProposalApplier;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.completions.ProposalApplier;
import org.springframework.ide.eclipse.boot.properties.editor.util.HtmlBuffer;
import org.springframework.ide.eclipse.boot.properties.editor.util.PrefixFinder;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.path.YamlPath;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.path.YamlPathSegment;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.path.YamlPathSegment.YamlPathSegmentType;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile.IndexNavigator;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.utils.CollectionUtil;

/**
 * Represents a context relative to which we can provide content assistance.
 */
@SuppressWarnings("restriction")
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

	public static YamlAssistContext global(FuzzyMap<PropertyInfo> index, PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
		return new IndexContext(YamlPath.EMPTY, IndexNavigator.with(index), completionFactory, typeUtil);
	}

	/**
	 * @return the type expected at this context, may return null if unknown.
	 */
	protected abstract Type getType();

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

	public class TypeContext extends YamlAssistContext {

		private final class BeanPropertyHoverInfo extends HoverInfo {
			private final String id;
			private final String propName;

			private BeanPropertyHoverInfo(String id, String propName) {
				this.id = id;
				this.propName = propName;
			}

			@Override
			public String getHtml() {
				HtmlBuffer html = new HtmlBuffer();

				html.raw("<b>");
					html.text(id);
				html.raw("</b>");
				html.raw("<br>");

				String typeStr = type.toString();
				if (typeStr==null) {
					typeStr = Object.class.getName();
				}
				html.raw("<a href=\"");
				html.url("type/"+type);
				html.raw("\">");
				html.text(typeStr);
				html.raw("</a>");

//					String deflt = formatDefaultValue(data.getDefaultValue());
//					if (deflt!=null) {
//						html.raw("<br><br>");
//						html.text("Default: ");
//						html.raw("<i>");
//						html.text(deflt);
//						html.raw("</i>");
//					}

				String description = getDescription();
				if (description!=null) {
					html.raw("<br><br>");
					html.raw(description);
				}

				return html.toString();
			}

			private String getDescription() {
				try {
					List<IJavaElement> jes = getAllJavaElements();
					if (jes!=null) {
						for (IJavaElement je : jes) {
							if (je instanceof IMember) {
								String jdoc = getHTMLContent((IMember)je, true);
								if (jdoc!=null) {
									return jdoc;
								}
							}
						}
					}
				} catch (Exception e) {
					BootActivator.log(e);
				}
				return null;
			}

			@Override
			public List<IJavaElement> getJavaElements() {
				IJavaElement je;
				Type beanType = parent.getType();
				je = typeUtil.getSetter(beanType, propName);
				if (je!=null) {
					return Collections.singletonList(je);
				}
				je = typeUtil.getGetter(beanType, propName);
				if (je!=null) {
					return Collections.singletonList(je);
				}
				je = typeUtil.getField(beanType, propName);
				if (je!=null) {
					return Collections.singletonList(je);
				}
				return Collections.emptyList();
			}

			private List<IJavaElement> getAllJavaElements() {
				if (propName!=null) {
					Type beanType = parent.getType();
					ArrayList<IJavaElement> elements = new ArrayList<IJavaElement>(3);
					maybeAdd(elements, typeUtil.getField(beanType, propName));
					maybeAdd(elements, typeUtil.getSetter(beanType, propName));
					maybeAdd(elements, typeUtil.getGetter(beanType, propName));
					if (!elements.isEmpty()) {
						return elements;
					}
				}
				return Collections.emptyList();
			}

			private void maybeAdd(ArrayList<IJavaElement> elements, IJavaElement e) {
				if (e!=null) {
					elements.add(e);
				}
			}
		}

		private PropertyCompletionFactory completionFactory;
		private Type type;
		private YamlAssistContext parent;

		public TypeContext(YamlAssistContext parent, YamlPath contextPath, Type type, PropertyCompletionFactory completionFactory, TypeUtil typeUtil) {
			super(contextPath, typeUtil);
			this.parent = parent;
			this.completionFactory = completionFactory;
			this.type = type;
		}

		@Override
		public Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset) throws Exception {
			String query = prefixfinder.getPrefix(doc.getDocument(), offset);
			EnumCaseMode enumCaseMode = enumCaseMode(query);
			List<ICompletionProposal> valueCompletions = getValueCompletions(doc, offset, query, enumCaseMode);
			if (!valueCompletions.isEmpty()) {
				return valueCompletions;
			}
			return getKeyCompletions(doc, offset, query, enumCaseMode);
		}

		private EnumCaseMode enumCaseMode(String query) {
			if (query.isEmpty()) {
				return preferLowerCasedEnums?LOWER_CASE:ORIGNAL;
			} else {
				return ALIASED; // will match candidates from both lower and original based on what user typed
			}
		}

		public List<ICompletionProposal> getKeyCompletions(YamlDocument doc, int offset, String query, EnumCaseMode enumCaseMode) throws Exception {
			int queryOffset = offset - query.length();
			List<TypedProperty> properties = typeUtil.getProperties(type, enumCaseMode);
			if (CollectionUtil.hasElements(properties)) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(properties.size());
				int sortingOrder = 0;
				for (TypedProperty p : properties) {
					String name = p.getName();
					Type type = p.getType();
					if (name.startsWith(query)) {
						YamlPathEdits edits = new YamlPathEdits(doc);
						edits.delete(queryOffset, query);

						SNode contextNode = contextPath.traverse((SNode)doc.getStructure());
						YamlPath relativePath = YamlPath.fromSimpleProperty(name);
						edits.createPathInPlace(contextNode, relativePath, queryOffset, appendTextFor(type));
						proposals.add(completionFactory.beanProperty(
								doc.getDocument(), queryOffset, contextPath.toPropString(), query,p, sortingOrder++, edits)
						);
					}
				}
				return proposals;
			}
			return Collections.emptyList();
		}

		private List<ICompletionProposal> getValueCompletions(YamlDocument doc, int offset, String query, EnumCaseMode enumCaseMode) {
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
			if (s.getType()==YamlPathSegmentType.VAL_AT_KEY) {
				if (TypeUtil.isArrayLike(type) || TypeUtil.isMap(type)) {
					return contextWith(s, TypeUtil.getDomainType(type));
				}
				String key = s.toPropString();
				Map<String, Type> subproperties = typeUtil.getPropertiesMap(type, ALIASED);
				if (subproperties!=null) {
					return contextWith(s, subproperties.get(key));
				}
			} else if (s.getType()==YamlPathSegmentType.VAL_AT_INDEX) {
				if (TypeUtil.isArrayLike(type)) {
					return contextWith(s, TypeUtil.getDomainType(type));
				}
			}
			return null;
		}

		private YamlAssistContext contextWith(YamlPathSegment s, Type nextType) {
			if (nextType!=null) {
				return new TypeContext(this, contextPath.append(s), nextType, completionFactory, typeUtil);
			}
			return null;
		}


		@Override
		public String toString() {
			return "TypeContext("+contextPath.toPropString()+"::"+type+")";
		}


		@Override
		public HoverInfo getHoverInfo() {
			if (parent instanceof IndexContext) {
				//this context is in fact an 'alias' of its parent, representing the
				// point in the context hierarchy where a we transition from navigating
				// the index to navigating type/bean properties
				return parent.getHoverInfo();
			} else {
				final String id = contextPath.toPropString();
				final String propName = contextPath.getBeanPropertyName();
				return new BeanPropertyHoverInfo(id, propName);
			}
		}

		@Override
		protected Type getType() {
			return type;
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
			if (s.getType()==YamlPathSegmentType.VAL_AT_KEY) {
				IndexNavigator subIndex = indexNav.selectSubProperty(s.toPropString());
				if (subIndex.getExtensionCandidate()!=null) {
					return new IndexContext(contextPath.append(s), subIndex, completionFactory, typeUtil);
				} else if (subIndex.getExactMatch()!=null) {
					IndexContext asIndexContext = new IndexContext(contextPath.append(s), subIndex, completionFactory, typeUtil);
					PropertyInfo prop = subIndex.getExactMatch();
					return new TypeContext(asIndexContext, contextPath.append(s), TypeParser.parse(prop.getType()), completionFactory, typeUtil);
				}
			}
			//Unsuported navigation => no context for assist
			return null;
		}

		@Override
		public String toString() {
			return "YamlAssistIndexContext("+indexNav+")";
		}

		@Override
		protected Type getType() {
			PropertyInfo match = indexNav.getExactMatch();
			if (match!=null) {
				return TypeParser.parse(match.getType());
			}
			return null;
		}

		@Override
		public HoverInfo getHoverInfo() {
			PropertyInfo prop = indexNav.getExactMatch();
			if (prop!=null) {
				return new SpringPropertyHoverInfo(typeUtil.getJavaProject(), prop);
			}
			return null;
		}
	}

	public abstract HoverInfo getHoverInfo();

}
