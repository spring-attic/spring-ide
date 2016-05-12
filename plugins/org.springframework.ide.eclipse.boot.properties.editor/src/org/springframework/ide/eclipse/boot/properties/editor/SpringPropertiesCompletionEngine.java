/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import static org.springframework.ide.eclipse.boot.util.StringUtil.camelCaseToHyphens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.completions.LazyProposalApplier;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.completions.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.completions.ValueHintHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.HintProvider;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.HintProviders;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.PropertyNavigator;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.ide.eclipse.editor.support.completions.DocumentEdits;
import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.util.CollectionUtil;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;
import org.springframework.ide.eclipse.editor.support.util.PrefixFinder;

import com.google.common.collect.ImmutableList;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class SpringPropertiesCompletionEngine implements HoverInfoProvider, ICompletionEngine {

	private boolean preferLowerCaseEnums = true; //might make sense to make this user configurable

	/**
	 * Pattern we look for at the start of the Document partition in 'value' part of a 'key-value'
	 * assignment. The stuff matching this pattern isn't to be treated as part of the actual value.
	 */
	public static final Pattern ASSIGN = Pattern.compile("^(\\h)*(=|:)(\\h|\\\\\\s)*");

	private static final boolean DEBUG = false; //(""+Platform.getLocation()).contains("kdvolder");
	public static void debug(String msg) {
		if (DEBUG) {
			System.out.println("SpringPropertiesCompletionEngine: "+msg);
		}
	}

	public static final boolean DEFAULT_VALUE_INCLUDED = false; //might make sense to make this user configurable

	private static final PrefixFinder valuePrefixFinder = new PrefixFinder() {
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c) && c!=',';
		}
	};

	private static final PrefixFinder fuzzySearchPrefix = new PrefixFinder() {
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c);
		}
	};

	private static final PrefixFinder navigationPrefixFinder = new PrefixFinder() {
		public String getPrefix(IDocument doc, int offset) {
			String prefix = super.getPrefix(doc, offset);
			//Check if character before looks like 'navigation'.. otherwise don't
			// return a navigationPrefix.
			char charBefore = getCharBefore(doc, prefix, offset);
			if (charBefore=='.' || charBefore==']') {
				return prefix;
			}
			return null;
		}
		private char getCharBefore(IDocument doc, String prefix, int offset) {
			try {
				if (prefix!=null) {
					int offsetBefore = offset-prefix.length()-1;
					if (offsetBefore>=0) {
						return doc.getChar(offsetBefore);
					}
				}
			} catch (BadLocationException e) {
				//ignore
			}
			return 0;
		}
		protected boolean isPrefixChar(char c) {
			return !Character.isWhitespace(c) && c!=']' && c!=']' && c!='.';
		}
	};

	private static final IContentProposal[] NO_CONTENT_PROPOSALS = new IContentProposal[0];

	private DocumentContextFinder documentContextFinder = null;
	private Provider<FuzzyMap<PropertyInfo>> indexProvider = null;
	private TypeUtil typeUtil = null;
	private PropertyCompletionFactory completionFactory = null;

	/**
	 * Create an empty completion engine. Meant for unit testing. Real clients should use the
	 * constructor that accepts an {@link IJavaProject}.
	 * <p>
	 * In a test context the test harness is responsible for injecting proper documentContextFinder
	 * and indexProvider.
	 */
	public SpringPropertiesCompletionEngine() {
	}

	/**
	 * Constructor used in 'production'. Wires up stuff properly for running inside a normal
	 * Eclipse runtime.
	 */
	public SpringPropertiesCompletionEngine(final IJavaProject jp) throws Exception {
		this.indexProvider = new Provider<FuzzyMap<PropertyInfo>>() {
			public FuzzyMap<PropertyInfo> get() {
				return SpringPropertiesEditorPlugin.getIndexManager().get(jp);
			}
		};
		setDocumentContextFinder(DocumentContextFinders.PROPS_DEFAULT);
		this.typeUtil = new TypeUtil(jp);

//		System.out.println(">>> spring properties metadata loaded "+index.size()+" items===");
//		dumpAsTestData();
//		System.out.println(">>> spring properties metadata loaded "+index.size()+" items===");

	}

	/**
	 * Create completions proposals in the context of a properties text editor.
	 */
	public Collection<ICompletionProposal> getCompletions(IDocument doc, int offset) throws BadLocationException {
		ITypedRegion partition = getPartition(doc, offset);
		String type = partition.getType();
		if (type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
			//inside a property 'key'
			return getPropertyCompletions(doc, offset);
		} else if (type.equals(IPropertiesFilePartitions.PROPERTY_VALUE)) {
			return getValueCompletions(doc, offset, partition);
		}
		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> getNavigationProposals(IDocument doc, int offset) {
		String navPrefix = navigationPrefixFinder.getPrefix(doc, offset);
		try {
			if (navPrefix!=null) {
				int navOffset = offset-navPrefix.length()-1; //offset of 'nav' operator char (i.e. '.' or ']').
				navPrefix = fuzzySearchPrefix.getPrefix(doc, navOffset);
				if (navPrefix!=null && !navPrefix.isEmpty()) {
					PropertyInfo prop = findLongestValidProperty(getIndex(), navPrefix);
					if (prop!=null) {
						int regionStart = navOffset-navPrefix.length();
						Collection<ICompletionProposal> hintProposals = getKeyHintProposals(doc, prop, navOffset, offset);
						if (CollectionUtil.hasElements(hintProposals)) {
							return hintProposals;
						}
						PropertyNavigator navigator = new PropertyNavigator(doc, null, typeUtil, region(regionStart, navOffset));
						Type type = navigator.navigate(regionStart+prop.getId().length(), TypeParser.parse(prop.getType()));
						if (type!=null) {
							return getNavigationProposals(doc, type, navOffset, offset);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> getKeyHintProposals(IDocument doc, PropertyInfo prop, int navOffset, int offset) {
		HintProvider hintProvider = prop.getHints(typeUtil, false);
		if (!HintProviders.isNull(hintProvider)) {
			String query = textBetween(doc, navOffset+1, offset);
			List<TypedProperty> hintProperties = hintProvider.getPropertyHints(query);
			if (CollectionUtil.hasElements(hintProperties)) {
				return createPropertyProposals(doc, TypeParser.parse(prop.getType()), navOffset, offset, query, hintProperties);
			}
		}
		return ImmutableList.of();
	}

	private String textBetween(IDocument doc, int start, int end) {
		if (end > doc.getLength()) {
			end = doc.getLength();
		}
		if (start>doc.getLength()) {
			start = doc.getLength();
		}
		if (start<0) {
			start = 0;
		}
		if (end < 0) {
			end = 0;
		}
		if (start<end) {
			try {
				return doc.get(start, end-start);
			} catch (BadLocationException e) {
			}
 		}
		return "";
	}

	private IRegion region(int start, int end) {
		return new Region(start, end-start);
	}

	/**
	 * @param type Type of the expression leading upto the 'nav' operator
	 * @param navOffset Offset of the nav operator (either ']' or '.'
	 * @param offset Offset of the cursor where CA was requested.
	 */
	private Collection<ICompletionProposal> getNavigationProposals(IDocument doc, Type type, int navOffset, int offset) {
		try {
			char navOp = doc.getChar(navOffset);
			if (navOp=='.') {
				String prefix = doc.get(navOffset+1, offset-(navOffset+1));
				EnumCaseMode caseMode = caseMode(prefix);
				List<TypedProperty> objectProperties = typeUtil.getProperties(type, caseMode, BeanPropertyNameMode.HYPHENATED);
				   //Note: properties editor itself deals with relaxed names. So it expects the properties here to be returned in hyphenated form only.
				if (objectProperties!=null && !objectProperties.isEmpty()) {
					return createPropertyProposals(doc, type, navOffset, offset, prefix, objectProperties);
				}
			} else {
				//TODO: other cases ']' or '[' ?
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	protected Collection<ICompletionProposal> createPropertyProposals(IDocument doc, Type type, int navOffset,
			int offset, String prefix, List<TypedProperty> objectProperties) {
		ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (TypedProperty prop : objectProperties) {
			double score = FuzzyMatcher.matchScore(prefix, prop.getName());
			if (score!=0) {
				Type valueType = prop.getType();
				String postFix = propertyCompletionPostfix(valueType);
				DocumentEdits edits = new DocumentEdits(doc);
				edits.delete(navOffset+1, offset);
				edits.insert(offset, prop.getName()+postFix);
				proposals.add(
					completionFactory.beanProperty(doc, null, type, prefix, prop, score, edits, typeUtil)
				);
			}
		}
		return proposals;
	}

	/**
	 * Determines the EnumCaseMode used to generate completion candidates based on prefix.
	 */
	protected EnumCaseMode caseMode(String prefix) {
		EnumCaseMode caseMode;
		if ("".equals(prefix)) {
			caseMode = preferLowerCaseEnums?EnumCaseMode.LOWER_CASE:EnumCaseMode.ORIGNAL;
		} else {
			caseMode = Character.isLowerCase(prefix.charAt(0))?EnumCaseMode.LOWER_CASE:EnumCaseMode.ORIGNAL;
		}
		return caseMode;
	}

	protected String propertyCompletionPostfix(Type type) {
		String postfix = "";
		if (type!=null) {
			if (typeUtil.isAssignableType(type)) {
				postfix = "=";
			} else if (TypeUtil.isBracketable(type)) {
				postfix = "[";
			} else if (typeUtil.isDotable(type)) {
				postfix = ".";
			}
		}
		return postfix;
	}

	public static boolean isAssign(char assign) {
		return assign==':'||assign=='=';
	}

	private ITypedRegion getPartition(IDocument doc, int offset) throws BadLocationException {
		ITypedRegion part = TextUtilities.getPartition(doc, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, offset, true);
		if (part.getType()==IDocument.DEFAULT_CONTENT_TYPE && part.getLength()==0 && offset==doc.getLength() && offset>0 && !newlineBefore(doc, offset)) {
			//A special case because when cursor at end of document and just after a space-padded '=' sign, then we get a DEFAULT content type
			// with a empty region. We rather would get the non-empty 'Value' partition just before that (which has the assignment in it.
			ITypedRegion previousPart = TextUtilities.getPartition(doc, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, offset-1, false);
			int previousEnd = previousPart.getOffset()+previousPart.getLength();
			if (previousEnd==offset) {
				//prefer this over a 0 length partition ending at the same location
				return previousPart;
			}
		}
		return part;
	}

	private boolean newlineBefore(IDocument doc, int offset) {
		try {
			if (offset>0) {
				char c = doc.getChar(offset-1);
				return c == '\n' || c=='\r';
			}
		} catch (BadLocationException e) {
			Log.log(e);
		}
		return false;
	}

	private Collection<ICompletionProposal> getValueCompletions(IDocument doc, int offset, ITypedRegion valuePartition) {
		int regionStart = valuePartition.getOffset();
		try {
			int startOfValue = skipAssign(doc, offset, valuePartition);
			String query = valuePrefixFinder.getPrefix(doc, offset, startOfValue);
			startOfValue = offset - query.length();
			EnumCaseMode caseMode = caseMode(query);
			String propertyName = fuzzySearchPrefix.getPrefix(doc, regionStart); //note: no need to skip whitespace backwards.
											//because value partition includes whitespace around the assignment
			if (propertyName!=null) {
				Collection<ValueHint> valueCompletions = getValueHints(query, propertyName, caseMode);
				if (valueCompletions!=null && !valueCompletions.isEmpty()) {
					ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
					for (ValueHint hint : valueCompletions) {
						String valueCandidate = ""+hint.getValue();
						double score = FuzzyMatcher.matchScore(query, valueCandidate);
						if (score!=0) {
							DocumentEdits edits = new DocumentEdits(doc);
							edits.delete(startOfValue, offset);
							edits.insert(offset, valueCandidate);
							proposals.add(
								completionFactory.valueProposal(valueCandidate, query, getValueType(propertyName), score, edits, new ValueHintHoverInfo(hint))
									//new ValueProposal(startOfValue, valuePrefix, valueCandidate, i)
							);
						}
					}
					return proposals;
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Determine the end of the 'ASSIGN' pattern which is expected at the beginning of a 'value' partition of
	 * props file. This is used to determine the start of the *real* value region (i.e. the assignment isn't
	 * really part of the value text even though Eclipse document partitioner inlcudes it as part of the 'valuePartition'
	 * region).
	 */
	private int skipAssign(IDocument doc, int offset, ITypedRegion valuePartition) {
		try {
			String text = doc.get(valuePartition.getOffset(), valuePartition.getLength());
			Matcher matcher = ASSIGN.matcher(text);
			if (matcher.find() && matcher.start()==0) {
				int len = matcher.end();
				return valuePartition.getOffset()+len;
			}
			return valuePartition.getOffset();
		} catch (BadLocationException e) {
			//This shouldn't really happen, but...
			return valuePartition.getOffset();
		}
	}

	private Collection<ValueHint> getValueHints(String query, String propertyName, EnumCaseMode caseMode) {
		Type type = getValueType(propertyName);
		if (TypeUtil.isArray(type) || TypeUtil.isList(type)) {
			//It is useful to provide content assist for the values in the list when entering a list
			type = TypeUtil.getDomainType(type);
		}
		List<ValueHint> allHints = new ArrayList<>();
		{
			Collection<ValueHint> hints = typeUtil.getHintValues(type, query, caseMode);
			if (CollectionUtil.hasElements(hints)) {
				allHints.addAll(hints);
			}
		}
		{
			PropertyInfo prop = getIndex().findLongestCommonPrefixEntry(propertyName);
			if (prop!=null) {
				HintProvider hintProvider = prop.getHints(typeUtil, false);
				if (!HintProviders.isNull(hintProvider)) {
					List<ValueHint> hints = hintProvider.getValueHints(query);
					if (CollectionUtil.hasElements(hints)) {
						allHints.addAll(hints);
					}
				}
			}
		}
		return allHints;
	}

	/**
	 * Determine the value type for a give propertyName.
	 */
	protected Type getValueType(String propertyName) {
		try {
			PropertyInfo prop = getIndex().get(propertyName);
			if (prop!=null) {
				return TypeParser.parse(prop.getType());
			} else {
				prop = findLongestValidProperty(getIndex(), propertyName);
				if (prop!=null) {
					Document doc = new Document(propertyName);
					PropertyNavigator navigator = new PropertyNavigator(doc, null, typeUtil, new Region(0, doc.getLength()));
					return navigator.navigate(prop.getId().length(), TypeParser.parse(prop.getType()));
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	private List<Match<PropertyInfo>> findMatches(String prefix) {
		List<Match<PropertyInfo>> matches = getIndex().find(camelCaseToHyphens(prefix));
		return matches;
	}

	private Collection<ICompletionProposal> getPropertyCompletions(IDocument doc, int offset) throws BadLocationException {
		Collection<ICompletionProposal> navProposals = getNavigationProposals(doc, offset);
		if (!navProposals.isEmpty()) {
			return navProposals;
		}
		return getFuzzyCompletions(doc, offset);
	}

	protected Collection<ICompletionProposal> getFuzzyCompletions(
			final IDocument doc, final int offset) {
		final String prefix = fuzzySearchPrefix.getPrefix(doc, offset);
		if (prefix != null) {
			Collection<Match<PropertyInfo>> matches = findMatches(prefix);
			if (matches!=null && !matches.isEmpty()) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(matches.size());
				for (final Match<PropertyInfo> match : matches) {
					ProposalApplier edits = new LazyProposalApplier() {
						@Override
						protected ProposalApplier create() throws Exception {
							Type type = TypeParser.parse(match.data.getType());
							DocumentEdits edits = new DocumentEdits(doc);
							edits.delete(offset-prefix.length(), offset);
							edits.insert(offset, match.data.getId() + propertyCompletionPostfix(type));
							return edits;
						}
					};
					proposals.add(
						completionFactory.property(doc, edits, match, typeUtil)
					);
				}
				return proposals;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Create completions proposals for a field editor where property names can be entered.
	 */
	public IContentProposal[] getPropertyFieldProposals(String contents, int position) {
		String prefix = contents.substring(0,position);
		if (StringUtil.hasText(prefix)) {
			List<Match<PropertyInfo>> matches = findMatches(prefix);
			if (matches!=null && !matches.isEmpty()) {
				IContentProposal[] proposals = new IContentProposal[matches.size()];
				Collections.sort(matches, new Comparator<Match<PropertyInfo>>() {
					@Override
					public int compare(Match<PropertyInfo> o1, Match<PropertyInfo> o2) {
						int scoreCompare = Double.compare(o2.score, o1.score);
						if (scoreCompare!=0) {
							return scoreCompare;
						} else {
							return o1.data.getId().compareTo(o2.data.getId());
						}
					}
				});
				int i = 0;
				for (Match<PropertyInfo> m : matches) {
					proposals[i++] = new ContentProposal(m.data.getId(), m.data.getDescription());
				}
				return proposals;
			}
		}
		return NO_CONTENT_PROPOSALS;
	}



	public SpringPropertyHoverInfo getHoverInfo(IDocument doc, IRegion region) {
		debug("getHoverInfo("+region+")");
		//The delegate 'getHoverRegion' for spring propery editor will return smaller word regions.
		// we must ensure to use our own region finder to identify correct property name.
		region = getHoverRegion(doc, region.getOffset());
		if (region!=null) {
			try {
	//			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
					debug("hoverRegion = "+region);
					PropertyInfo best = findBestHoverMatch(doc.get(region.getOffset(), region.getLength()).trim());
					if (best!=null) {
						return new SpringPropertyHoverInfo(documentContextFinder.getJavaProject(doc), best);
					}
	//			}
			} catch (Exception e) {
				SpringPropertiesEditorPlugin.log(e);
			}
		}
		return null;
	}

	public IRegion getHoverRegion(IDocument document, int offset) {
    	try {
    		ITypedRegion candidate = getPartition(document, offset);
    		if (candidate!=null && candidate.getType()==IDocument.DEFAULT_CONTENT_TYPE) {
    			return candidate;
    		}
    	} catch (Exception e) {
    		SpringPropertiesEditorPlugin.log(e);
    	}
		return null;
	}

	/**
	 * Search known properties for the best 'match' to show as hover data.
	 */
	private PropertyInfo findBestHoverMatch(String propName) {
		debug(">> findBestHoverMatch("+propName+")");
		debug("index size: "+getIndex().size());
		//TODO: optimize, should be able to use index's treemap to find this without iterating all entries.
		PropertyInfo best = null;
		int bestCommonPrefixLen = 0; //We try to pick property with longest common prefix
		int bestExtraLen = Integer.MAX_VALUE;
		for (PropertyInfo candidate : getIndex()) {
			int commonPrefixLen = StringUtil.commonPrefixLength(propName, candidate.getId());
			int extraLen = candidate.getId().length()-commonPrefixLen;
			if (commonPrefixLen==propName.length() && extraLen==0) {
				//exact match found, can stop searching for better matches
				return candidate;
			}
			//candidate is better if...
			if (commonPrefixLen>bestCommonPrefixLen // it has a longer common prefix
			|| commonPrefixLen==bestCommonPrefixLen && extraLen<bestExtraLen //or same common prefix but fewer extra chars
			) {
				bestCommonPrefixLen = commonPrefixLen;
				bestExtraLen = extraLen;
				best = candidate;
			}
		}
		debug("<< findBestHoverMatch("+propName+"): "+best);
		return best;
	}


	public FuzzyMap<PropertyInfo> getIndex() {
		return indexProvider.get();
	}

	public Provider<FuzzyMap<PropertyInfo>> getIndexProvider() {
		return indexProvider;
	}

	public void setDocumentContextFinder(DocumentContextFinder it) {
		this.documentContextFinder = it;
		this.completionFactory = new PropertyCompletionFactory(it);
	}

	public void setIndexProvider(Provider<FuzzyMap<PropertyInfo>> it) {
		this.indexProvider = it;
	}

	public void setTypeUtil(TypeUtil it) {
		this.typeUtil = it;
	}

	public TypeUtil getTypeUtil() {
		return typeUtil;
	}

	public boolean getPreferLowerCaseEnums() {
		return preferLowerCaseEnums;
	}

	public void setPreferLowerCaseEnums(boolean preferLowerCaseEnums) {
		this.preferLowerCaseEnums = preferLowerCaseEnums;
	}

	/**
	 * Find the longest known property that is a prefix of the given name. Here prefix does not mean
	 * 'string prefix' but a prefix in the sense of treating '.' as a kind of separators. So
	 * 'prefix' is not allowed to end in the middle of a 'segment'.
	 */
	public static PropertyInfo findLongestValidProperty(FuzzyMap<PropertyInfo> index, String name) {
		int bracketPos = name.indexOf('[');
		int endPos = bracketPos>=0?bracketPos:name.length();
		PropertyInfo prop = null;
		String prefix = null;
		while (endPos>0 && prop==null) {
			prefix = name.substring(0, endPos);
			String canonicalPrefix = camelCaseToHyphens(prefix);
			prop = index.get(canonicalPrefix);
			if (prop==null) {
				endPos = name.lastIndexOf('.', endPos-1);
			}
		}
		if (prop!=null) {
			//We should meet caller's expectation that matched properties returned by this method
			// match the names exactly even if we found them using relaxed name matching.
			return prop.withId(prefix);
		}
		return null;
	}


}