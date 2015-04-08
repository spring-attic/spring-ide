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

import static org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.formatJavaType;
import static org.springframework.ide.eclipse.boot.util.StringUtil.camelCaseToHyphens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap.Match;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo.PropertySource;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.PropertyNavigator;
import org.springframework.ide.eclipse.boot.properties.editor.util.Provider;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeParser;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.EnumCaseMode;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class SpringPropertiesCompletionEngine implements IPropertyHoverInfoProvider {

	private boolean preferLowerCaseEnums = true;

	private static abstract class PrefixFinder {
		public String getPrefix(IDocument doc, int offset) {
			try {
				if (doc == null || offset > doc.getLength())
					return null;
				int prefixStart= offset;
				while (prefixStart > 0 && isPrefixChar(doc.getChar(prefixStart-1))) {
					prefixStart--;
				}
				return doc.get(prefixStart, offset-prefixStart);
			} catch (BadLocationException e) {
				return null;
			}
		}
		protected abstract boolean isPrefixChar(char c);
	}

	private static final boolean DEBUG = false;
	public static final boolean DEFAULT_VALUE_INCLUDED = false; //might make sense to make this user configurable

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

//	private static final boolean DEBUG =
//			(""+Platform.getLocation()).contains("kdvolder") ||
//			(""+Platform.getLocation()).contains("bamboo");

	public static void debug(String msg) {
		if (DEBUG) {
			System.out.println("SpringPropertiesCompletionEngine: "+msg);
		}
	}

	public Styler JAVA_STRING_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_STRING);
		}
	};
	public Styler JAVA_KEYWORD_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_KEYWORD);
		}
	};
	public Styler JAVA_OPERATOR_COLOR = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVA_OPERATOR);
		}
	};



	public static final ICompletionProposalSorter SORTER = new ICompletionProposalSorter() {
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			if (p1 instanceof PropertyProposal && p2 instanceof PropertyProposal) {
				double s1 = ((PropertyProposal)p1).match.score;
				double s2 = ((PropertyProposal)p2).match.score;
				if (s1==s2) {
					String name1 = ((PropertyProposal)p1).match.data.getId();
					String name2 = ((PropertyProposal)p2).match.data.getId();
					return name1.compareTo(name2);
				} else {
					return Double.compare(s2, s1);
				}
			} else if (p1 instanceof ValueProposal && p2 instanceof ValueProposal) {
				int order1 = ((ValueProposal)p1).sortingOrder;
				int order2 = ((ValueProposal)p2).sortingOrder;
				return Integer.valueOf(order1).compareTo(Integer.valueOf(order2));
			}
			return 0;
		}
	};
	private static final IContentProposal[] NO_CONTENT_PROPOSALS = new IContentProposal[0];

	private DocumentContextFinder documentContextFinder = null;
	private Provider<FuzzyMap<PropertyInfo>> indexProvider = null;
	private TypeUtil typeUtil = null;

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
		this.documentContextFinder = DocumentContextFinder.DEFAULT;
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
						PropertyNavigator navigator = new PropertyNavigator(doc, null, typeUtil, region(regionStart, navOffset));
						Type type = navigator.navigate(regionStart+prop.getId().length(), TypeParser.parse(prop.getType()));
						if (type!=null) {
							return getNavigationProposals(doc, type, navOffset, offset);
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
	}

	private IRegion region(int start, int end) {
		return new Region(start, end-start);
	}

	/**
	 * @param type Type of the expression leading upto the 'nav' operator
	 * @param navOffset Offset of the nav operator (either ']' or '.'
	 * @param offset Offset of the cursor where CA was requested.
	 * @return
	 */
	private Collection<ICompletionProposal> getNavigationProposals(IDocument doc, Type type, int navOffset, int offset) {
		try {
			char navOp = doc.getChar(navOffset);
			if (navOp=='.') {
				String prefix = doc.get(navOffset+1, offset-(navOffset+1));
				EnumCaseMode caseMode = caseMode(prefix);
				List<TypedProperty> objectProperties = typeUtil.getProperties(type, caseMode);
				if (objectProperties!=null && !objectProperties.isEmpty()) {
					ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
					int sorting = 1;
					for (TypedProperty prop : objectProperties) {
						if (prop.getName().startsWith(prefix)) {
							Type valueType = prop.getType();
							String postFix = propertyCompletionPostfix(valueType);
							proposals.add(new ValueProposal(navOffset+1, prefix, prop.getName(), sorting++, postFix));
						}
					}
					return proposals;
				}
			} else {
				//TODO: other cases ']' or '[' ?
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
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
		if (part.getType()==IDocument.DEFAULT_CONTENT_TYPE && part.getLength()==0 && offset==doc.getLength() && offset>0) {
			//A special case because when cursor at end of document and just after a '=' sign, then we get a DEFAULT content type
			// with a empty region. We rather would get the non-empty 'Value' partition just before that (which has the assignment in it.
			char assign = doc.getChar(offset-1);
			if (isAssign(assign)) {
				return new TypedRegion(offset-1, 1, IPropertiesFilePartitions.PROPERTY_VALUE);
			} else {
				//For a similar case but where there's extra spaces after the '='
				ITypedRegion previousPart = TextUtilities.getPartition(doc, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, offset-1, true);
				int previousEnd = previousPart.getOffset()+previousPart.getLength();
				if (previousEnd==offset) {
					//prefer this over a 0 length partition ending at the same location
					return previousPart;
				}
			}
		}
		return part;
	}

	private Collection<ICompletionProposal> getValueCompletions(IDocument doc, int offset, ITypedRegion valuePartition) {
		int regionStart = valuePartition.getOffset();
		int startOfValue = findValueStart(doc, regionStart);
		try {
			String valuePrefix;
			if (startOfValue>=0 && startOfValue<offset) {
				valuePrefix = doc.get(startOfValue, offset-startOfValue);
			} else {
				startOfValue = offset;
				valuePrefix = "";
			}
			EnumCaseMode caseMode = caseMode(valuePrefix);
			String propertyName = fuzzySearchPrefix.getPrefix(doc, regionStart); //note: no need to skip whitespace backwards.
											//because value partition includes whitespace around the assignment
			if (propertyName!=null) {
				Type type = getValueType(propertyName);
				String[] valueCompletions = typeUtil.getAllowedValues(type, caseMode);
				if (valueCompletions!=null && valueCompletions.length>0) {
					ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
					for (int i = 0; i < valueCompletions.length; i++) {
						String valueCandidate = valueCompletions[i];
						if (valueCandidate.startsWith(valuePrefix)) {
							proposals.add(new ValueProposal(startOfValue, valuePrefix, valueCandidate, i));
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
			BootActivator.log(e);
		}
		return null;
	}

	private int findValueStart(IDocument doc, int pos) {
		try {
			pos = skipWhiteSpace(doc, pos);
			if (pos>=0) {
				char assign = doc.getChar(pos);
				if (!isAssign(assign)) {
					return pos; //For the case where key and value are separated by whitespace instead of assignment
				}
				pos = skipWhiteSpace(doc, pos+1);
				if (pos>=0) {
					return pos;
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return -1;
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
			IDocument doc, int offset) {
		String prefix = fuzzySearchPrefix.getPrefix(doc, offset);
		if (prefix != null) {
			Collection<Match<PropertyInfo>> matches = findMatches(prefix);
			if (matches!=null && !matches.isEmpty()) {
				ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(matches.size());
				for (Match<PropertyInfo> match : matches) {
					proposals.add(new PropertyProposal(doc, prefix, offset, match));
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

	private final class ValueProposal implements ICompletionProposal {

		private int valueStart;
		private String valuePrefix;
		private String substitution;
		private int sortingOrder;
		private String displayString;

		public ValueProposal(int valueStart, String valuePrefix, String value, int sortingOrder, String valuePostFix) {
			this.valueStart = valueStart;
			this.valuePrefix = valuePrefix;
			this.displayString = value;
			this.sortingOrder = sortingOrder;
			this.substitution = value + valuePostFix;
		}

		public ValueProposal(int valueStart, String valuePrefix, String value, int sortingOrder) {
			this(valueStart, valuePrefix, value, sortingOrder, "");
		}

		@Override
		public void apply(IDocument document) {
			try {
				document.replace(valueStart, valuePrefix.length(), substitution);
			} catch (BadLocationException e) {
				SpringPropertiesEditorPlugin.log(e);
			}
		}

		@Override
		public Point getSelection(IDocument document) {
			return new Point(valueStart+substitution.length(), 0);
		}

		@Override
		public String getAdditionalProposalInfo() {
			return null;
		}

		@Override
		public String getDisplayString() {
			return displayString;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public String toString() {
			return "<"+valuePrefix+">"+substitution;
		}
	}

	private final class PropertyProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3,
	ICompletionProposalExtension4, ICompletionProposalExtension5, ICompletionProposalExtension6
	{

		private final String fPrefix;
		private final int fOffset;
		private Match<PropertyInfo> match;
		private IDocument fDoc;

		public PropertyProposal(IDocument doc, String prefix, int offset, Match<PropertyInfo> match) {
			fDoc = doc;
			fPrefix= prefix;
			fOffset= offset;
			this.match = match;
		}

		public Point getSelection(IDocument document) {
			return new Point(fOffset - fPrefix.length() + getCompletion().length(), 0);
		}

		public String getAdditionalProposalInfo() {
			PropertyInfo data = match.data;
			return SpringPropertyHoverInfo.getHtmlHoverText(data);
		}

		@Override
		public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
			return new SpringPropertyHoverInfo(documentContextFinder.getJavaProject(fDoc), match.data);
		}


		public String getDisplayString() {
			StyledString styledText = getStyledDisplayString();
			return styledText.getString();
		}

		public Image getImage() {
			return null;
		}

		public IContextInformation getContextInformation() {
			return null;
		}

		public boolean isValidFor(IDocument document, int offset) {
			return validate(document, offset, null);
		}

		public char[] getTriggerCharacters() {
			return null;
		}

		public int getContextInformationPosition() {
			return 0;
		}

		public void apply(IDocument document) {
			apply(document, '\0', fOffset);
		}

		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument(), trigger, offset);
		}

		public void apply(IDocument document, char trigger, int offset) {
			try {
				String replacement= getCompletion();
				int start = this.fOffset-fPrefix.length();
				document.replace(start, offset-start, replacement);
			} catch (BadLocationException e) {
				BootActivator.log(e);
			}
		}

		public void selected(ITextViewer viewer, boolean smartToggle) {
		}

		public void unselected(ITextViewer viewer) {
		}

		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			try {
				int prefixStart= fOffset - fPrefix.length();
				String newPrefix = document.get(prefixStart, offset-prefixStart);
				double newScore = FuzzyMap.match(newPrefix, match.data.getId());
				if (newScore!=0.0) {
					match.score = newScore;
					return true;
				}
			} catch (BadLocationException x) {
			}
			return false;
		}

		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return match.data.getId();
		}

		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fOffset - fPrefix.length();
		}

		public boolean isAutoInsertable() {
			return true;
		}

		private String getCompletion() {
			StringBuilder completion = new StringBuilder(match.data.getId());
			Type type = TypeParser.parse(match.data.getType());
			completion.append(propertyCompletionPostfix(type));
			return completion.toString();
		}

		@Override
		public StyledString getStyledDisplayString() {
			StyledString result = new StyledString();
			result.append(match.data.getId());
			String defaultValue = SpringPropertyHoverInfo.formatDefaultValue(match.data.getDefaultValue());
			if (defaultValue!=null) {
				result.append("=", JAVA_OPERATOR_COLOR);
				result.append(defaultValue, JAVA_STRING_COLOR);
			}
			String type = formatJavaType(match.data.getType());
			if (type!=null) {
				result.append(" : ");
				result.append(type, JAVA_KEYWORD_COLOR);
			}
			String description = getShortDescription();
			if (description!=null && !"".equals(description.trim())) {
				result.append(" ");
				result.append(description.trim(), StyledString.DECORATIONS_STYLER);
			}
			return result;
		}

		private String getShortDescription() {
			String description = match.data.getDescription();
			if (description!=null) {
				int dotPos = description.indexOf('.');
				if (dotPos>=0) {
					description = description.substring(0, dotPos+1);
				}
				description = description.replaceAll("\\p{Cntrl}", ""); //mostly here to remove line breaks, these mess with the layout in the popup control.
				return description;
			}
			return null;
		}


		@Override
		public String toString() {
			return "<"+fPrefix+">"+match.data.getId();
		}

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

	public List<IJavaElement> getSourceElements(IDocument doc, IRegion region) {
		debug("getSourceElements");
		SpringPropertyHoverInfo hoverinfo = getHoverInfo(doc, region);
		if (hoverinfo!=null) {
			return hoverinfo.getJavaElements();
		} else {
			debug("hoverInfo = null");
		}
		return Collections.emptyList();
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

	/**
	 * Dumps out 'test data' based on the current contents of the index. This is not meant to be
	 * used in 'production' code. The idea is to call this method during development to dump a
	 * 'snapshot' of the index onto System.out. The data is printed in a forma so that it can be easily
	 * pasted/used into JUNit testing code.
	 */
	public void dumpAsTestData() {
		List<Match<PropertyInfo>> allData = getIndex().find("");
		for (Match<PropertyInfo> match : allData) {
			PropertyInfo d = match.data;
			System.out.println("data("
					+dumpString(d.getId())+", "
					+dumpString(d.getType())+", "
					+dumpString(d.getDefaultValue())+", "
					+dumpString(d.getDescription()) +");"
			);
			for (PropertySource source : d.getSources()) {
				String st = source.getSourceType();
				String sm = source.getSourceMethod();
				if (sm!=null) {
					System.out.println(d.getId() +" from: "+st+"::"+sm);
				}
			}
		}
	}

	private String dumpString(Object v) {
		if (v==null) {
			return "null";
		}
		return dumpString(""+v);
	}

	private String dumpString(String s) {
		if (s==null) {
			return "null";
		} else {
			StringBuilder buf = new StringBuilder("\"");
			for (char c : s.toCharArray()) {
				switch (c) {
				case '\r':
					buf.append("\\r");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\\':
					buf.append("\\\\");
					break;
				case '\"':
					buf.append("\\\"");
					break;
				default:
					buf.append(c);
					break;
				}
			}
			buf.append("\"");
			return buf.toString();
		}
	}

	public FuzzyMap<PropertyInfo> getIndex() {
		return indexProvider.get();
	}

	public Provider<FuzzyMap<PropertyInfo>> getIndexProvider() {
		return indexProvider;
	}

	public static int skipWhiteSpace(IDocument doc, int pos) {
		try {
			int end = doc.getLength();
			while (pos<end&&Character.isWhitespace(doc.getChar(pos))) {
				pos++;
			}
			if (pos<end) {
				return pos;
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return -1;
	}

	public void setDocumentContextFinder(DocumentContextFinder it) {
		this.documentContextFinder = it;
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