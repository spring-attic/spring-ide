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
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SNodeType;
import org.springframework.ide.eclipse.yaml.editor.completions.YamlStructureParser.SRootNode;
import org.yaml.snakeyaml.Yaml;

public class YamlCompletionEngine implements ICompletionEngine {

	private Yaml yaml;
	private SpringPropertyIndexProvider indexProvider;
	private DocumentContextFinder contextFinder;
	private YamlStructureProvider structureProvider;

	public YamlCompletionEngine(Yaml yaml,
			SpringPropertyIndexProvider indexProvider,
			DocumentContextFinder documentContextFinder,
			YamlStructureProvider structureProvider
	) {
		this.yaml = yaml;
		this.indexProvider = indexProvider;
		this.contextFinder = documentContextFinder;
		this.structureProvider = structureProvider;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument _doc, int offset) throws Exception {
		YamlDocument doc = new YamlDocument(_doc, structureProvider);
		PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(contextFinder);
		if (!doc.isCommented(offset)) {
			FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc.getDocument());
			if (index!=null && !index.isEmpty()) {
				SRootNode root = doc.getStructure();
				SNode current = root.find(offset);
				YamlPath contextPath = getContextPath(current, offset);
				YamlAssistContext context = YamlAssistContext.forPath(contextPath, index, completionFactory);
				if (context!=null) {
					return context.getCompletions(doc, offset);
				}
			}
		}
		return Collections.emptyList();
	}

	private YamlPath getContextPath(SNode node, int offset) throws Exception {
		if (node==null) {
			return YamlPath.EMPTY;
		}
		if (node.getNodeType()==SNodeType.KEY) {
			//slight complication. The area in the key and value of a key node represent different
			// contexts for content assistance
			SKeyNode keyNode = (SKeyNode)node;
			if (keyNode.isInKey(offset)) {
				return getContextPath(keyNode.getParent());
			} else {
				return getContextPath(keyNode);
			}
		} else {
			return getContextPath(node);
		}
	}

	private YamlPath getContextPath(SNode node) throws Exception {
		ArrayList<YamlPathSegment> segments = new ArrayList<YamlPathSegment>();
		buildContextPath(node, segments);
		return new YamlPath(segments);
	}

	private void buildContextPath(SNode node, ArrayList<YamlPathSegment> segments) throws Exception {
		if (node!=null) {
			buildContextPath(node.getParent(), segments);
			if (node.getNodeType()==SNodeType.KEY) {
				String key = ((SKeyNode)node).getKey();
				segments.add(YamlPathSegment.at(key));
			}
		}
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

//	/**
//	 * This finds the starting line for content assist purposes. Basically, we try
//	 * to make the parser more robust and efficient by only trying to parse
//	 * a portion of the document rather than the whole thing.
//	 * <p>
//	 * Basically, we try to find a the closest line of text at or before the
//	 * given offset that is not-indented. (In this search we ignore fully commented lines
//	 * as and empty lines)
//	 */
//	private int findStartLine(YamlDocument doc, int offset) throws Exception {
//		int line = doc.getLineOfOffset(offset);
//		IRegion region = doc.getLineInformation(line);
//		int cursorColumn = offset - region.getOffset();
//		if (cursorColumn==0) {
//			//special case, as cursor is at the start of the line, the current line
//			//would be 0 indented if we started typing here.
//			return line;
//		}
//		while (line>=0) {
//			int indentation = doc.getLineIndentation(line);
//			if (indentation==0) {
//				return line;
//			}
//			line--;
//		}
//		return 0; //no non-indented line found, start parsing from begining of document.
//	}


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
