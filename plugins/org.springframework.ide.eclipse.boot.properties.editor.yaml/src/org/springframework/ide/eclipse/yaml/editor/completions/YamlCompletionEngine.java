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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.ICompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlFileAST;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.yaml.snakeyaml.Yaml;

public class YamlCompletionEngine implements ICompletionEngine {

	private Yaml yaml;
	private SpringPropertyIndexProvider indexProvider;
	private DocumentContextFinder contextFinder;

	public YamlCompletionEngine(Yaml yaml,
			SpringPropertyIndexProvider indexProvider,
			DocumentContextFinder documentContextFinder) {
		this.yaml = yaml;
		this.indexProvider = indexProvider;
		this.contextFinder = documentContextFinder;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument _doc, int offset) throws Exception {
		YamlDocument doc = new YamlDocument(_doc);
		PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(contextFinder);
		if (!doc.isCommented(offset)) {
			FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc.getDocument());
			if (index!=null && !index.isEmpty()) {
				int currentLine = doc.getLineOfOffset(offset);
				int startLine = findStartLine(doc, offset);
				YamlAssistContext context = null;
				if (currentLine>startLine) {
					SNode ast = new YamlStructureParser(doc).parse();
				} else {
					//We got nothing to parse to determine the block-level context.
					//Assume that we are in global context
					context = YamlAssistContext.global(indexProvider.getIndex(doc.getDocument()), completionFactory);
				}
				if (context!=null) {
					return context.getCompletions(doc.getDocument(), offset);
				}
			}


//			return simpleCompletions(offset, "some", "very", "fake", "completions");


//			int startLine = findNonIndentedLineBefore(doc, offset);



//			YamlFileAST ast = parser.getAST(doc);
//
//			doc.getLineInformationOfOffset(offset)
//			List<NodeRef<?>> path = ast.findPath(offset);
//			if (path!=null) {
//				String[] fakeSuggestions = new String[path.size()];
//				for (int i = 0; i < fakeSuggestions.length; i++) {
//					NodeRef<?> ref = path.get(i);
//					fakeSuggestions[i] = ""+ref + "::"+ ref.get().getNodeId();
//				}
//				return simpleCompletions(offset, fakeSuggestions);
//			}
		}
		return Collections.emptyList();
	}

	/**
	 * Parse text between startLine (inclusive) and endLine (exclusive).
	 */
	private YamlFileAST parseLines(YamlDocument doc, int startLine, int endLine) throws Exception {
		try {
			int start = doc.getLineOffset(startLine);
			int end = doc.getLineOffset(endLine);
			Reader input = new StringReader(doc.textBetween(start, end));
			return new YamlFileAST(yaml.composeAll(input));
		} catch (Exception e) {
			//Most likely garbage input... ignore
			return null;
		}
	}

	/**
	 * This finds the starting line for content assist purposes. Basically, we try
	 * to make the parser more robust and efficient by only trying to parse
	 * a portion of the document rather than the whole thing.
	 * <p>
	 * Basically, we try to find a the closest line of text at or before the
	 * given offset that is not-indented. (In this search we ignore fully commented lines
	 * as and empty lines)
	 */
	private int findStartLine(YamlDocument doc, int offset) throws Exception {
		int line = doc.getLineOfOffset(offset);
		IRegion region = doc.getLineInformation(line);
		int cursorColumn = offset - region.getOffset();
		if (cursorColumn==0) {
			//special case, as cursor is at the start of the line, the current line
			//would be 0 indented if we started typing here.
			return line;
		}
		while (line>=0) {
			int indentation = doc.getLineIndentation(line);
			if (indentation==0) {
				return line;
			}
			line--;
		}
		return 0; //no non-indented line found, start parsing from begining of document.
	}


	/////////////////// THe 'simpleCompletions' stuff isn't really useful, except for quickly creating
	/// a silly 'fake' CA implementation to determine whether the engine is wired up correctly.

	private Collection<ICompletionProposal> simpleCompletions(int offset, String... strings) {
		ArrayList<ICompletionProposal> props = new ArrayList<ICompletionProposal>();

		for (int i = 0; i < strings.length; i++) {
			props.add(simpleCompletion(offset, strings[i]));
		}

		return props;
	}

	private ICompletionProposal simpleCompletion(final int offset, final String string) {
		return new ICompletionProposal() {

			@Override
			public Point getSelection(IDocument document) {
				return new Point(offset+string.length(), 0);
			}

			@Override
			public Image getImage() {
				return null;
			}

			@Override
			public String getDisplayString() {
				return string;
			}

			@Override
			public IContextInformation getContextInformation() {
				return null;
			}

			@Override
			public String getAdditionalProposalInfo() {
				return null;
			}

			@Override
			public void apply(IDocument document) {
				try {
					document.replace(offset, 0, string);
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}
		};
	}

}
