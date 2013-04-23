/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;

/**
 * @author Terry Denney
 * @author Martin Lippert
 * @author Kaitlin Duck Sherwood
 */
public class ConfigurationLocationProposalComputer extends AnnotationProposalComputer {

	final String ILLEGAL_STRING = ":!\t)";

	class ProposalAssemblyInformation {
		String prefix;

		String postfix;

		String filter;

		ProposalAssemblyInformation(String aPrefix, String aPostfix, String aFilter) {
			this.prefix = aPrefix;
			this.postfix = aPostfix;
			this.filter = aFilter;
		}

		public String getPrefix() {
			return prefix;
		}

		public String getPostfix() {
			return postfix;
		}

		public String getFilter() {
			return filter;
		}

	}

	private List<String> getPathPrefixes(IJavaProject project) {
		ArrayList<String> pathPrefixes = new ArrayList<String>();
		try {
			for (IClasspathEntry entry : project.getRawClasspath()) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					// Spring uses "/", not the system default separator
					pathPrefixes.add(entry.getPath().removeFirstSegments(1).toOSString() + "/");
				}
			}
		}
		catch (JavaModelException e) {
			// ignore
		}
		return pathPrefixes;
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, IAnnotation annotation,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
		for (IMemberValuePair memberValuePair : memberValuePairs) {
			if ("locations".equals(memberValuePair.getMemberName()) || "value".equals(memberValuePair.getMemberName())) {
				return getBeanProposals(javaContext, type.getCompilationUnit(), javaContext.getInvocationOffset(),
						annotation);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
			ICompilationUnit cu = javaContext.getCompilationUnit();
			try {
				int invocationOffset = context.getInvocationOffset();
				IJavaElement element = cu.getElementAt(invocationOffset);
				if (element instanceof IType) {
					IType type = (IType) element;
					// NOTE: getAnnotations does not get any {} associated with
					// the annotations
					IAnnotation[] annotations = type.getAnnotations();
					for (IAnnotation annotation : annotations) {

						if ("ContextConfiguration".equals(annotation.getElementName())) {
							IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
							for (IMemberValuePair memberValuePair : memberValuePairs) {
								if ("locations".equals(memberValuePair.getMemberName())
										|| "value".equals(memberValuePair.getMemberName())) {
									return getBeanProposals(context, cu, invocationOffset, annotation);
								}
							}
						}
					}
				}
			}
			catch (JavaModelException e) {
				// ignore
			}
		}
		return new ArrayList<ICompletionProposal>();
	}

	private List<ICompletionProposal> getBeanProposals(ContentAssistInvocationContext context, ICompilationUnit cu,
			int invocationOffset, IAnnotation annotation) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String annotationText = getAnnotationText(annotation, context.getViewer(), invocationOffset);
		String prefix = "";
		String postfix = "";
		String filter = "";

		try {
			ProposalAssemblyInformation assemblyInfo = createProposalAssemblyInformation(annotationText,
					invocationOffset - annotation.getNameRange().getOffset() - annotation.getNameRange().getLength());
			prefix = assemblyInfo.getPrefix();
			postfix = assemblyInfo.getPostfix();
			filter = assemblyInfo.getFilter();
		}
		catch (JavaModelException e) {
			prefix = "";
			postfix = "";
		}

		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(cu.getJavaProject().getProject());
		List<String> classpathPrefixes = getPathPrefixes(cu.getJavaProject());
		if (beansProject != null) {
			Set<IBeansConfig> configs = beansProject.getConfigs();

			// Imagine classpath "src/" and two config files
			// "src/one/two/three.xml" and
			// "config/x/y/z.xml" (<- i.e. not on classpath)
			// If the user type "src", "one", or "three", they should get
			// "classpath:one/two/three.xml" back.
			// Similarly if they preface either of those three with
			// "classpath:", they should get "classpath:one/two/three.xml".

			// If the user types "config" or "z", they should get
			// "file:config/x/y/z.xml" back.
			// If they preface any of the above with "file:", they should get
			// "file:src/one/two/three.xml" or
			// "file:config/x/y/z.xml" back.

			String resourceTypePrefix = "";
			// Don't just search for a colon: we only handle
			// "file:" and "classpath:".
			if (filter.startsWith("file:")) {
				resourceTypePrefix = "file:";
				filter = filter.replaceFirst(resourceTypePrefix, "");
			}
			else if (filter.startsWith("classpath:")) {
				resourceTypePrefix = "classpath:";
				filter = filter.replaceFirst(resourceTypePrefix, "");
			}

			for (IBeansConfig config : configs) {
				// Note that the displayText gets checked after
				// the proposals are returned: see
				// AbstractJavaCompletionProposal.getPrefix()
				String displayText = "";
				String replacementText = "";
				String fullConfigFilePathname = config.getElementName();
				String strippedConfigPath = fullConfigFilePathname;
				String basename = config.getElementResource().getName();
				boolean isOnClasspath = false;

				boolean matchesFilter = false;

				for (String classpathPrefix : classpathPrefixes) {
					if (fullConfigFilePathname.startsWith(classpathPrefix)) {
						isOnClasspath = true;
						strippedConfigPath = fullConfigFilePathname.substring(classpathPrefix.length());
						if (resourceTypePrefix.equals("classpath:")) {
							replacementText = strippedConfigPath;
						}
						else {
							replacementText = "classpath:" + strippedConfigPath;
						}
						break;
					}
				}

				// if this config file isn't where it is supposed to be --
				// e.g. on the classpath when file: is specified -- then it is
				// not a legitimate option, skip this if statement's body
				if (!(isOnClasspath && resourceTypePrefix.equals("file:"))
						&& !(!isOnClasspath && resourceTypePrefix.equals("classpath:"))) {

					if (!isOnClasspath) {
						if (resourceTypePrefix.equals("file:")) {
							replacementText = fullConfigFilePathname;
						}
						else {
							replacementText = "file:" + fullConfigFilePathname;
						}
					}

					if (fullConfigFilePathname.length() > 0) {
						// user starts typing the start of the path *without*
						// the classpath (e.g. "one" as described above)
						if (isOnClasspath && strippedConfigPath.startsWith(filter)) {
							displayText = strippedConfigPath;
							matchesFilter = true;
						}

						// user starts typing the start of the full path (e.g.
						// "src" or "config" as described above)
						else if (fullConfigFilePathname.startsWith(filter)) {
							displayText = fullConfigFilePathname;
							matchesFilter = true;
						}

						// user starts typing the filename (e.g. foo.xml)
						else if (basename.startsWith(filter)) {
							displayText = basename;
							matchesFilter = true;
						}
					}

					replacementText = prefix + replacementText + postfix;

					if (matchesFilter) {
						proposals.add(new JavaCompletionProposal(replacementText, invocationOffset - filter.length(),
								filter.length(), BeansModelImages.getImage(config), displayText, 0));
					}
				}
			}
		}

		return proposals;
	}

	private static final int LINE_LOCATIONS_POSITION = 1;

	private static final int LINE_LEADING_BRACE_PATTERN_POSITION = LINE_LOCATIONS_POSITION + 1;

	private static final int LINE_WHITESPACE1_PATTERN_POSITION = LINE_LEADING_BRACE_PATTERN_POSITION + 1;

	private static final int LINE_CONFIG_FILEPATHS_PATTERN_POSITION = LINE_WHITESPACE1_PATTERN_POSITION + 1;

	private static final int LINE_CONFIG_LAST_FILEPATH_WITH_DELIMITING_SPACE_PATTERN_POSITION = LINE_CONFIG_FILEPATHS_PATTERN_POSITION + 1;

	private static final int LINE_CONFIG_LAST_FILEPATH_PATTERN_POSITION = LINE_CONFIG_LAST_FILEPATH_WITH_DELIMITING_SPACE_PATTERN_POSITION + 1;

	private static final int LINE_CONFIG_FILEPATH_NO_SPACES_PATTERN_POSITION = LINE_CONFIG_LAST_FILEPATH_PATTERN_POSITION + 1;

	private static final int LINE_CONFIG_FILEPATH_SPACES_PATTERN_POSITION = LINE_CONFIG_FILEPATH_NO_SPACES_PATTERN_POSITION + 1;

	private static final int LINE_COMMA_POSITION = LINE_CONFIG_FILEPATH_SPACES_PATTERN_POSITION + 1;

	private static final int LINE_WHITESPACE2_PATTERN_POSITION = LINE_COMMA_POSITION + 1;

	private static final int LINE_CLOSING_BRACE_PATTERN_POSITION = LINE_WHITESPACE2_PATTERN_POSITION + 1;

	private static final int LINE_WHITESPACE3_PATTERN_POSITION = LINE_CLOSING_BRACE_PATTERN_POSITION + 1;

	private static final int LINE_CLOSING_PAREN_PATTERN_POSITION = LINE_WHITESPACE3_PATTERN_POSITION + 1;

	// This is only public to allow testing
	public static Pattern getLineCompiledPattern() {
		String locations = "(locations|value)\\s*=\\s*";
		String optionalLeadingBrace = "(\\{?)";
		// if you want a space in the file name, it must be quoted
		String oneFileNameAllowingSpaces = "([\"'][^\"']*[\"'])";
		// must allow possible missing close quote so you can autocomplete
		// the file, but a file must have at least an opening quote
		String oneFileNameDisallowingSpaces = "([\'\"][^\\s\"'\\)\\}]*[\"']?)";
		String oneFileName = "(" + oneFileNameAllowingSpaces + "|" + oneFileNameDisallowingSpaces + ")";

		String configurationFiles = "((" + oneFileName + "\\s*(,?)\\s*)*)";

		String optionalClosingBrace = "(\\}?)";

		String optionalClosingParen = "(\\)?)";
		String whitespace = "(\\s*)";

		String validCompletion = locations + optionalLeadingBrace + // group 1
				whitespace + // group 2
				configurationFiles + // group 3
				whitespace + // group 4
				optionalClosingBrace + // group 5
				whitespace + // group 6
				optionalClosingParen; // group 7
		return Pattern.compile(validCompletion);
	}

	private int invocationOffsetIsInWhichGroupNumber(Matcher matcher, int invocationIndex) {
		int groupIndex;
		for (groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
			if (invocationIndex < matcher.start(groupIndex)) {
				return CONFIG_AT_END; // we ran off the end
			}

			try {
				if (invocationIndex >= matcher.start(groupIndex) && invocationIndex <= matcher.end(groupIndex)) {
					return groupIndex;
				}
			}
			catch (IllegalStateException e) {
				// ignore
			}
		}

		return CONFIG_AT_END;
	}

	private boolean hasSomethingInField(Matcher matcher, int patternPosition) {
		return matcher.start(patternPosition) != matcher.end(patternPosition);
	}

	private ProposalAssemblyInformation createProposalAssemblyInformation(String annotationText, int invocationIndex) {
		String prefix = "";
		String postfix = "";
		String filter = "";

		Matcher matcher = getLineCompiledPattern().matcher(annotationText);
		if (matcher.find()) {
			boolean hasOpeningBrace = hasSomethingInField(matcher, LINE_LEADING_BRACE_PATTERN_POSITION);
			boolean hasOtherConfigFiles = hasSomethingInField(matcher, LINE_CONFIG_FILEPATHS_PATTERN_POSITION);
			boolean hasComma = hasSomethingInField(matcher, LINE_COMMA_POSITION);
			boolean hasClosingBrace = hasSomethingInField(matcher, LINE_CLOSING_BRACE_PATTERN_POSITION);

			int groupIndex = invocationOffsetIsInWhichGroupNumber(matcher, invocationIndex);

			if (groupIndex < 0) {
				if (invocationIndex >= annotationText.length()) {

					String comma = "";
					if (hasOtherConfigFiles && !hasComma) {
						comma = ",";
					}
					prefix = comma + "\"";

					// String closeParen = "";
					// if there is a close brace, end with a )
					if (hasClosingBrace) {
						postfix = "";
						filter = ILLEGAL_STRING;
					}
					else {
						filter = "";
						postfix = "\"";
					}

					return new ProposalAssemblyInformation(prefix, postfix, filter);
				}
				else {
					// This can happen if the string is just not legal,
					// or if the invocation is before the = sign.
					prefix = "";
					postfix = "";
					filter = ILLEGAL_STRING;
					return new ProposalAssemblyInformation(prefix, postfix, filter);
				}
			}

			filter = ""; // overridden as needed
			switch (groupIndex) {
			case LINE_LEADING_BRACE_PATTERN_POSITION:
				String closingBrace = "";
				if (!hasOpeningBrace) {
					prefix = "{\"";
					// if we open a brace, close it as well
					closingBrace = "}";
				}
				else {
					prefix = "\"";
				}

				if (hasOtherConfigFiles) {
					postfix = "\", " + closingBrace;
				}
				else {
					postfix = "\"" + closingBrace;
				}
				break;
			case LINE_WHITESPACE1_PATTERN_POSITION:
				prefix = "\"";
				if (hasOtherConfigFiles) {
					postfix = "\", ";
				}
				else {
					postfix = "\"";
				}
				break;

			case LINE_CONFIG_FILEPATHS_PATTERN_POSITION:
				String fileString = substringOfGroup(annotationText, matcher, LINE_CONFIG_FILEPATHS_PATTERN_POSITION);

				if (fileString.length() == 0) {
					prefix = "";
					postfix = "";
				}
				else {
					int newInvocationIndex = invocationIndex - matcher.end(groupIndex - 1);
					ProposalAssemblyInformation configAssemblyInfo = proposalAssemblyInfoFromConfigFiles(fileString,
							newInvocationIndex, hasClosingBrace);
					return configAssemblyInfo;
				}
				break;
			case LINE_WHITESPACE2_PATTERN_POSITION:
				// Whitespace usually ends up getting eaten by the config
				// line, so this will almost never get hit

				// Adding a prefix comma is the responsibility of the
				// CONFIG_WHITESPACE2_PATTERN_POSITION handler
				prefix = "\"";
				postfix = "\"";
				break;
			case LINE_CLOSING_BRACE_PATTERN_POSITION:
			case LINE_WHITESPACE3_PATTERN_POSITION:
				prefix = "\"";
				if (!hasClosingBrace) {
					postfix = "\"}";
				}
				// disallow matching; proposals are invalid here
				filter = ILLEGAL_STRING;
				break;
			case LINE_CLOSING_PAREN_PATTERN_POSITION: // too late
				prefix = "";
				postfix = "";
				// disallow matching; proposals are invalid here
				filter = ILLEGAL_STRING;
				break;
			}

			ProposalAssemblyInformation assemblyInfo = new ProposalAssemblyInformation(prefix, postfix, filter);
			return assemblyInfo;
		}

		return new ProposalAssemblyInformation("", "", "");
	}

	private String substringOfGroup(String annotationText, Matcher matcher, int groupIndex) {
		int startLocation = matcher.start(groupIndex);
		int endLocation = matcher.end(groupIndex);
		return annotationText.substring(startLocation, endLocation);
	}

	private ProposalAssemblyInformation proposalAssemblyInfoFromConfigFiles(String fileString, int invocationIndex,
			boolean hasClosingBrace) {
		String postfix = "";
		String prefix = "";
		String filter = "";

		java.util.regex.Pattern filePattern = ConfigurationLocationProposalComputer.getConfigFileCompiledPattern();
		Matcher fileMatcher = filePattern.matcher(fileString);

		int fileGroupIndex = 1;
		while (fileMatcher.find()) {
			if (invocationIndex >= fileMatcher.start() && invocationIndex <= fileMatcher.end()) {
				int fileSubgroupIndex = invocationOffsetIsInWhichGroupNumber(fileMatcher, invocationIndex);

				// boolean hasOpeningQuote = hasSomethingInField(fileMatcher,
				// CONFIG_OPENING_QUOTE_PATTERN_POSITION);
				boolean hasClosingQuote = hasSomethingInField(fileMatcher, CONFIG_CLOSING_QUOTE_PATTERN_POSITION);
				boolean hasComma = hasSomethingInField(fileMatcher, CONFIG_COMMA_PATTERN_POSITION);
				boolean hasFilePattern = hasSomethingInField(fileMatcher, CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION)
						|| hasSomethingInField(fileMatcher, CONFIG_CLOSING_QUOTE_PATTERN_POSITION);

				String comma = "";
				if (fileGroupIndex > 1 && !hasComma) {
					comma = ", ";
				}

				switch (fileSubgroupIndex) {
				// off the end
				case CONFIG_AT_END:
					prefix = comma + "\"";
					postfix = "\" ,";
					if (fileGroupIndex == fileMatcher.groupCount()) {
						return new ProposalAssemblyInformation(prefix, postfix, filter);
					}
					break;
				case CONFIG_WHITESPACE3_PATTERN_POSITION:
					postfix = "\"";
					prefix = "\"";
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_WHITESPACE1_PATTERN_POSITION:
					prefix = "\"";
					postfix = comma + "\"";
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_WHITESPACE2_PATTERN_POSITION:
					boolean areThereMoreFiles = hasFilePattern;

					if (areThereMoreFiles) {
						prefix = ", \"";
					}
					else {
						prefix = "\"";
					}
					postfix = "\"" + comma;
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_COMMA_PATTERN_POSITION:

					prefix = "\"";
					postfix = "\"";

					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_OPENING_QUOTE_PATTERN_POSITION:
					prefix = "";
					// If there is no trailing brace, the annotation text only
					// goes up to the invocation point. This means we can't
					// know if there is a close quote or not if there is no
					// closing brace.
					if (!hasClosingQuote && hasClosingBrace) {
						postfix = "\"";
					}
					else {
						postfix = "";
					}
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION:
					prefix = "";
					if (hasClosingQuote) {
						postfix = "";
					}
					else {
						postfix = "\"";
					}
					filter = fileMatcher.group(CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION);
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				case CONFIG_CLOSING_QUOTE_PATTERN_POSITION:
					prefix = ", \"";
					postfix = "\"";
					filter = "";
					return new ProposalAssemblyInformation(prefix, postfix, filter);

				}
			}
			fileGroupIndex++;

		}

		return new ProposalAssemblyInformation("", "", "");
	}

	private static final int CONFIG_AT_END = -1;

	private static final int CONFIG_WHITESPACE1_PATTERN_POSITION = 1;

	private static final int CONFIG_OPENING_QUOTE_PATTERN_POSITION = CONFIG_WHITESPACE1_PATTERN_POSITION + 1;

	private static final int CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION = CONFIG_OPENING_QUOTE_PATTERN_POSITION + 1;

	private static final int CONFIG_TYPE_PREFIXES = CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION + 1;

	private static final int CONFIG_CLOSING_QUOTE_PATTERN_POSITION = CONFIG_TYPE_PREFIXES + 1;

	private static final int CONFIG_WHITESPACE2_PATTERN_POSITION = CONFIG_CLOSING_QUOTE_PATTERN_POSITION + 1;

	private static final int CONFIG_COMMA_PATTERN_POSITION = CONFIG_WHITESPACE2_PATTERN_POSITION + 1;

	private static final int CONFIG_WHITESPACE3_PATTERN_POSITION = CONFIG_COMMA_PATTERN_POSITION + 1;

	// public only for testing purposes
	public static java.util.regex.Pattern getConfigFileCompiledPattern() {

		String whitespace = "(\\s*)"; // group
										// CONFIG_WHITESPACE1_PATTERN_POSITION
										// and group
										// CONFIG_WHITESPACE2_PATTERN_POSITION

		// group CONFIG_OPENING_QUOTE_PATTERN_POSITION
		String openingQuotePatternString = "(\"?)";

		// group CONFIG_TYPE_PREFIXES
		String allowedResourceTypePrefixes = "(file:|classpath:)?";

		// CONFIG_UNQUOTED_FILENAME_PATTERN_POSITION and CONFIG_TYPE_PREFIXES
		String unquotedFileNamePatternString = "(" + allowedResourceTypePrefixes + "[\\w\\.\\-\\/]*)";

		// group CONFIG_CLOSING_QUOTE_PATTERN_POSITION
		String closingQuotePatternString = "(\"?)";

		// group CONFIG_COMMA_PATTERN_POSITION
		String commaPatternString = "(,?)";
		String filePatternString = whitespace + openingQuotePatternString + unquotedFileNamePatternString
				+ closingQuotePatternString + whitespace + commaPatternString + whitespace;
		java.util.regex.Pattern filePattern = java.util.regex.Pattern.compile(filePatternString);

		return filePattern;
	}

	private String getAnnotationText(IAnnotation annotation, ITextViewer viewer, int invocationOffset) {
		ISourceRange nameRange;
		ISourceRange sourceRange;
		try {
			nameRange = annotation.getNameRange();
			sourceRange = annotation.getSourceRange();
		}
		catch (JavaModelException e) {
			return "";
		}

		int endPoint = sourceRange.getOffset() + sourceRange.getLength();
		// Want to get AT LEAST to the invocation point
		if (invocationOffset > endPoint) {
			endPoint = invocationOffset;
		}
		String text;
		try {
			text = viewer.getDocument().get((nameRange.getOffset() + nameRange.getLength()),
					endPoint - (nameRange.getOffset() + nameRange.getLength()));
		}
		catch (BadLocationException e) {
			return "";
		}
		return text;
	}

}
