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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.ICompletionEngine;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.path.YamlPath;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SKeyNode;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SNodeType;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureParser.SSeqNode;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

public class YamlCompletionEngine implements ICompletionEngine {

	//private Yaml yaml;
	private SpringPropertyIndexProvider indexProvider;
	private DocumentContextFinder contextFinder;
	private YamlStructureProvider structureProvider;
	private PropertyCompletionFactory completionFactory;
	private TypeUtilProvider typeUtilProvider;

	public YamlCompletionEngine(Yaml yaml,
			SpringPropertyIndexProvider indexProvider,
			DocumentContextFinder documentContextFinder,
			YamlStructureProvider structureProvider,
			TypeUtilProvider typeUtilProvider
	) {
		//this.yaml = yaml;
		this.indexProvider = indexProvider;
		this.contextFinder = documentContextFinder;
		this.structureProvider = structureProvider;
		this.completionFactory = new PropertyCompletionFactory(contextFinder);
		this.typeUtilProvider = typeUtilProvider;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument _doc, int offset) throws Exception {
		YamlDocument doc = new YamlDocument(_doc, structureProvider);
		if (!doc.isCommented(offset)) {
			FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc.getDocument());
			if (index!=null && !index.isEmpty()) {
				SRootNode root = doc.getStructure();
				SNode current = root.find(offset);
				YamlAssistContext context = getContext(doc, current, offset, index);
				if (context!=null) {
					return context.getCompletions(doc, offset);
				}
			}
		}
		return Collections.emptyList();
	}

	private YamlAssistContext getContext(YamlDocument doc, SNode node, int offset, FuzzyMap<PropertyInfo> index) throws Exception {
		TypeUtil typeUtil = typeUtilProvider.getTypeUtil(doc.getDocument());
		if (node==null) {
			return YamlAssistContext.forPath(YamlPath.EMPTY, index, completionFactory, typeUtil);
		}
		if (node.getNodeType()==SNodeType.KEY) {
			//slight complication. The area in the key and value of a key node represent different
			// contexts for content assistance
			SKeyNode keyNode = (SKeyNode)node;
			if (keyNode.isInValue(offset)) {
				return YamlAssistContext.forPath(keyNode.getPath(), index, completionFactory, typeUtil);
			} else {
				return YamlAssistContext.forPath(keyNode.getParent().getPath(), index, completionFactory, typeUtil);
			}
		} else if (node.getNodeType()==SNodeType.RAW) {
			//Treat raw node as a 'key node'. This is basically assuming that is misclasified
			// by structure parser because the ':' was not yet typed into the document.

			//Complication: if line with cursor is empty or the cursor is inside the indentation
			// area then the structure may not reflect correctly the context. This is because
			// the correct context depends on text the user has not typed yet.(which will change the
			// indentation level of the current line. So we must use the cursorIndentation
			// rather than the structur-tree to determine the 'context' node.
			int cursorIndent = doc.getColumn(offset);
			int nodeIndent = node.getIndent();
			int currentIndent = IndentUtil.minIndent(cursorIndent, nodeIndent);
			while (node.getIndent()==-1 || (node.getIndent()>=currentIndent && node.getNodeType()!=SNodeType.DOC)) {
				node = node.getParent();
			}
			return YamlAssistContext.forPath(node.getPath(), index, completionFactory, typeUtil);
		} else if (node.getNodeType()==SNodeType.SEQ) {
			SSeqNode seqNode = (SSeqNode)node;
			if (seqNode.isInValue(offset)) {
				return YamlAssistContext.forPath(seqNode.getPath(), index, completionFactory, typeUtil);
			} else {
				return YamlAssistContext.forPath(seqNode.getParent().getPath(), index, completionFactory, typeUtil);
			}
		} else if (node.getNodeType()==SNodeType.DOC) {
			return  YamlAssistContext.forPath(node.getPath(), index, completionFactory, typeUtil);
		} else {
			throw new IllegalStateException("Missing case");
		}
	}


}
