/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.DocumentEdits;
import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

public class ManifestYamlCompletionEngine extends YamlCompletionEngine {

	private CompletionFactory proposalFactory;

	public ManifestYamlCompletionEngine() {
		super(YamlStructureProvider.DEFAULT);
		proposalFactory = new CompletionFactory();
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument _doc, int offset) throws Exception {
		YamlDocument doc = new YamlDocument(_doc, structureProvider);
		if (!doc.isCommented(offset)) {
			SRootNode root = doc.getStructure();
			SNode current = root.find(offset);
			YamlPath path = getContextPath(doc, current, offset);
			if (path!=null) {
				return getCompletions(doc, offset, path);
			}
		}
		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset, YamlPath path) {
		System.out.println("CA path: "+path.toPropString());
		return Collections.emptyList();
	}

	private ICompletionProposal insert(IDocument doc, int offset, String word, int order) {
		DocumentEdits edits = new DocumentEdits(doc);
		edits.insert(offset, word);
		return proposalFactory.simpleProposal(word, -order, edits);
	}
}
