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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.completions.DocumentEdits;
import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

public class ManifestYamlCompletionEngine extends YamlCompletionEngine {

	private CompletionFactory proposalFactory;

	public ManifestYamlCompletionEngine() {
		super(YamlStructureProvider.DEFAULT);
		proposalFactory = new CompletionFactory();
	}

	private ICompletionProposal insert(IDocument doc, int offset, String word, int order) {
		DocumentEdits edits = new DocumentEdits(doc);
		edits.insert(offset, word);
		return proposalFactory.simpleProposal(word, -order, edits);
	}

	@Override
	protected YamlAssistContext getGlobalContext(YamlDocument doc) {
		//TODO: implement
		return null;
	}
}
