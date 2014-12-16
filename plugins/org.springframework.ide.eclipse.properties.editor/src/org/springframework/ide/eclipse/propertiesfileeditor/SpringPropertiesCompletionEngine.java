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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
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
import org.eclipse.swt.widgets.Shell;
import org.springframework.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.ide.eclipse.propertiesfileeditor.FuzzyMap.Match;

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
		
//		System.out.println(">>> spring properties metadata loaded ===");
//		int i = 0;
//		for (Match<ConfigurationMetadataProperty> entry : index.find("")) {
//			System.out.println(String.format("%3d", ++i)+":"+ entry.data.getId());
//		}
//		System.out.println("<<< spring properties metadata loaded ===");
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
			HtmlBuffer html = new HtmlBuffer();
			
			html.raw("<b>");
				html.text(match.data.getId());
			html.raw("</b>");
			html.raw("<br>");
			
			String type = match.data.getType();
			if (type==null) {
				type = Object.class.getName();
			}
			html.raw("<a href=\"");
			html.url("type/"+type);
			html.raw("\">");
			html.text(type);
			html.raw("</a>");
			
			
			String deflt = formatDefaultValue(match.data.getDefaultValue());
			if (deflt!=null) {
				html.raw("<br><br>");
				html.text("Default: ");
				html.raw("<i>");
				html.text(deflt);
				html.raw("</i>");
			}
			
			String description = match.data.getDescription();
			if (description!=null) {
				html.raw("<br><br>");
				html.text(description);
			}
			
			return html.toString();
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
	

}

//Mock implementation (makes suggestions from a hard-coded list of words.
//
//public class SpringPropertiesCompletionEngine {
//	
//	private String[] WORDS = {
//		"bar",
//		"bartentender",
//		"bartering",
//		"barren",
//		"banana",
//		"bar.mitswa"
//	};
//	
//	private IJavaProject javaProject;
//
//	public SpringPropertiesCompletionEngine(IJavaProject jp) {
//		this.javaProject = jp;
//	}
//
//	public Collection<String> getCompletions(IJavaProject javaProject, IDocument doc, String prefix, int offset) {
//		ArrayList<String> completions = new ArrayList<>();
//		for (String word : WORDS) {
//			if (word.startsWith(prefix)) {
//				completions.add(word.substring(prefix.length()));
//			}
//		}
//		return completions;
//	}
//
//}
