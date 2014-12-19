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
package org.springframework.ide.eclipse.propertiesfileeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap.Match;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertiesReconcileStrategy.ProblemCollector;
import org.springframework.ide.eclipse.propertiesfileeditor.reconciling.SpringPropertyProblem;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesCompletionEngine {
	
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
	
	private static final String JAVA_LANG = "java.lang.";
	private static final int JAVA_LANG_LEN = JAVA_LANG.length();
	
	private static final Map<String, String> PRIMITIVE_TYPES = new HashMap<String, String>();
	static {
		PRIMITIVE_TYPES.put("java.lang.Boolean", "boolean");
		PRIMITIVE_TYPES.put("java.lang.Integer", "int");
		PRIMITIVE_TYPES.put("java.lang.Long", "short");
		PRIMITIVE_TYPES.put("java.lang.Short", "int");
		PRIMITIVE_TYPES.put("java.lang.Double", "double");
		PRIMITIVE_TYPES.put("java.lang.Float", "float");
	}
	
	private static final Set<String> ASSIGNABLE_TYPES = new HashSet<String>(Arrays.asList(
			"java.lang.Boolean",
			"java.lang.String",
			"java.lang.Short",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lan.Double",
			"java.lang.Float",
			"java.lang.Character",
			"java.util.List"
	));
	
	public static final ICompletionProposalSorter SORTER = new ICompletionProposalSorter() {
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			if (p1 instanceof Proposal && p2 instanceof Proposal) {
				double s1 = ((Proposal)p1).match.score;
				double s2 = ((Proposal)p2).match.score;
				return Double.compare(s2, s1);
			}
			return 0;
		}
	};
	
	public static String formatDefaultValue(Object defaultValue) {
		if (defaultValue!=null) {
			if (defaultValue instanceof String) {
				return (String) defaultValue;
			} else if (defaultValue instanceof Number) {
				return ((Number)defaultValue).toString();
			} else if (defaultValue instanceof Boolean) {
				return Boolean.toString((Boolean) defaultValue);
			} else {
				//no idea what it is so ignore
			}
		}
		return null;
	}
	
	private FuzzyMap<ConfigurationMetadataProperty> index = new FuzzyMap<ConfigurationMetadataProperty>() {
		protected String getKey(ConfigurationMetadataProperty entry) {
			return entry.getId();
		}
	};
	private IInformationControlCreator informationControlCreator;
	
	/** 
	 * Create an empty completion engine. Meant for unit testing. Real clients should use the
	 * constructor that accepts an {@link IJavaProject}. 
	 */
	public SpringPropertiesCompletionEngine() {
	}
	
	/** 
	 * Create a completion engine and poplulate it with metadata parsed from given 
	 * {@link IJavaProject}'s classpath.
	 */
	public SpringPropertiesCompletionEngine(IJavaProject jp) throws Exception {
		StsConfigMetadataRepositoryJsonLoader loader = new StsConfigMetadataRepositoryJsonLoader();
		ConfigurationMetadataRepository metadata = loader.load(jp); //TODO: is this fast enough? Or should it be done in background?
		
		Collection<ConfigurationMetadataProperty> allEntries = metadata.getAllProperties().values();
		for (ConfigurationMetadataProperty item : allEntries) {
			add(item);
		}
		
		System.out.println(">>> spring properties metadata loaded ===");
		dumpAsTestData();
		System.out.println("<<< spring properties metadata loaded ===");
	}

	/**
	 * Add a ConfigurationMetadataProperty item to the CompletionEngine. Normal clients don't really need to
	 * call this, the data will be parsed from project's classpath. This mostly here to allow the engine to
	 * be more easily unit tested with controlled test data.
	 */
	public void add(ConfigurationMetadataProperty item) {
		index.add(item);
	}

	private String getPrefix(IDocument doc, int offset) throws BadLocationException {
		if (doc == null || offset > doc.getLength())
			return null;
		int prefixStart= offset;
		while (prefixStart > 0 && isPrefixChar(doc.getChar(prefixStart-1))) {
			prefixStart--;
		}

		return doc.get(prefixStart, offset-prefixStart);
	}
	
	private boolean isPrefixChar(char c) {
		return c=='.' || Character.isJavaIdentifierPart(c);
	}
	
	public Collection<ICompletionProposal> getCompletions(IDocument doc, int offset) throws BadLocationException {
		ITypedRegion partition = doc.getPartition(offset);
		if (partition.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
			//inside a property 'key' 
			String prefix= getPrefix(doc, offset);
			if (prefix != null) {
				Collection<Match<ConfigurationMetadataProperty>> matches = index.find(prefix);
				if (matches!=null && !matches.isEmpty()) {
					ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(matches.size());
					for (Match<ConfigurationMetadataProperty> match : matches) {
						proposals.add(new Proposal(prefix, offset, match));
					}
					return proposals;
				}
			}
		}
		return Collections.emptyList();
	}
	
	private static String getHtmlHoverText(ConfigurationMetadataProperty data) {
		HtmlBuffer html = new HtmlBuffer();
		
		html.raw("<b>");
			html.text(data.getId());
		html.raw("</b>");
		html.raw("<br>");
		
		String type = data.getType();
		if (type==null) {
			type = Object.class.getName();
		}
		html.raw("<a href=\"");
		html.url("type/"+type);
		html.raw("\">");
		html.text(type);
		html.raw("</a>");
		
		
		String deflt = formatDefaultValue(data.getDefaultValue());
		if (deflt!=null) {
			html.raw("<br><br>");
			html.text("Default: ");
			html.raw("<i>");
			html.text(deflt);
			html.raw("</i>");
		}
		
		String description = data.getDescription();
		if (description!=null) {
			html.raw("<br><br>");
			html.text(description);
		}
		
		return html.toString();
	}
	
	
	private final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4,
	 ICompletionProposalExtension6
	{

		private final String fPrefix;
		private final int fOffset;
		private Match<ConfigurationMetadataProperty> match;

		public Proposal(String prefix, int offset, Match<ConfigurationMetadataProperty> match) {
			fPrefix= prefix;
			fOffset= offset;
			this.match = match;
		}

		public Point getSelection(IDocument document) {
			return new Point(fOffset - fPrefix.length() + getCompletion().length(), 0);
		}

		public String getAdditionalProposalInfo() {
			ConfigurationMetadataProperty data = match.data;
			return getHtmlHoverText(data);
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
			} catch (BadLocationException x) {
				// TODO Auto-generated catch block
				x.printStackTrace();
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
					match.score = newScore; //Score might change, but I don't think Eclipse CA will re-sort results after incremental.
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
			String defaultValue = formatDefaultValue(match.data.getDefaultValue());
			if (defaultValue!=null) {
				completion.append("=");
				completion.append(defaultValue);
			} else {
				String type = match.data.getType();
				if (ASSIGNABLE_TYPES.contains(type)) {
					completion.append("=");
				} else {
					//assume some kind of 'Object' type
					completion.append(".");
				}
			}
			return completion.toString();
		}
		
		@Override
		public StyledString getStyledDisplayString() {
			StyledString result = new StyledString();
			result.append(match.data.getId());
			String defaultValue = formatDefaultValue(match.data.getDefaultValue());
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

		private String formatJavaType(String type) {
			if (type!=null) {
				String primitive = PRIMITIVE_TYPES.get(type);
				if (primitive!=null) {
					return primitive;
				} 
				if (type.startsWith(JAVA_LANG)) {
					return type.substring(JAVA_LANG_LEN);
				}
				return type;
			}
			return null;
		}

		@Override
		public String toString() {
			return "<"+fPrefix+">"+match.data.getId();
		}

	}

	public String getHoverInfo(IDocument doc, IRegion r, String contentType) {
		try {
			if (contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				ConfigurationMetadataProperty best = findBestHoverMatch(doc.get(r.getOffset(), r.getLength()));
				if (best!=null) {
					return getHtmlHoverText(best);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return null;
	}

	/**
	 * Search known properties for the best 'match' to show as hover data.
	 */
	private ConfigurationMetadataProperty findBestHoverMatch(String propName) {
		ConfigurationMetadataProperty best = null;
		int bestCommonPrefixLen = 0; //We try to pick property with longest common prefix
		int bestExtraLen = Integer.MAX_VALUE;
		for (ConfigurationMetadataProperty candidate : index) {
			int commonPrefixLen = commonPrefixLength(propName, candidate.getId());
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
		return best;
	}

	private int commonPrefixLength(String s, String t) {
		int shortestStringLen = Math.min(s.length(), t.length());
		for (int i = 0; i < shortestStringLen; i++) {
			if (s.charAt(i)!=t.charAt(i)) {
				return i;
			}
		}
		//no difference found upto entire length of shortest string.
		return shortestStringLen;
	}

	/**
	 * Dumps out 'test data' based on the current contents of the index. This is not meant to be
	 * used in 'production' code. The idea is to call this method during development to dump a
	 * 'snapshot' of the index onto System.out. The data is printed in a forma so that it can be easily 
	 * pasted/used into JUNit testing code. 
	 */
	public void dumpAsTestData() {
		List<Match<ConfigurationMetadataProperty>> allData = index.find("");
		for (Match<ConfigurationMetadataProperty> match : allData) {
			ConfigurationMetadataProperty d = match.data;
			System.out.println("data("
					+dumpString(d.getId())+", "
					+dumpString(d.getType())+", "
					+dumpString(d.getDefaultValue())+", "
					+dumpString(d.getDescription()) +");"
			);
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

	public IRegion getHoverRegion(IDocument document, int offset) {
    	try {
    		return TextUtilities.getPartition(document, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING, offset, true);
    	} catch (Exception e) {
    		SpringPropertiesEditorPlugin.log(e);
    		return null;
    	}
	}

	/**
	 * Used by Reconciling to scan document regions for invalid propery names and report them as problems.
	 */
	public void check(IDocument doc, IRegion[] regions, ProblemCollector problemCollector, IProgressMonitor mon) {
		problemCollector.beginCollecting();
		try {
			// TODO replace this 'fake' implementation which adds problems anywhere the word 'bad' occurs.
			for (IRegion r : regions) {
				try {
					String text = doc.get(r.getOffset(), r.getLength());
					int badPos = -1;
					while ((badPos = text.indexOf("bad", badPos+1))>=0) {
						problemCollector.accept(new SpringPropertyProblem("bad", r.getOffset()+badPos, 3));
					}
				} catch (Exception e) {
					SpringPropertiesEditorPlugin.log(e);
				}
			}
		} finally {
			problemCollector.endCollecting();
		}
	}
	
}