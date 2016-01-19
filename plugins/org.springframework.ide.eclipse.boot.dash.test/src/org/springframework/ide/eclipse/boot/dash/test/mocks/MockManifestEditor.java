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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.springframework.ide.eclipse.boot.properties.editor.test.MockEditor;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlCompletionEngine;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYmlSchema;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class MockManifestEditor extends MockEditor {

	private YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
	private ManifestYmlSchema schema = new ManifestYmlSchema();
	private ManifestYamlCompletionEngine completionEngine = new ManifestYamlCompletionEngine(structureProvider, schema);
	private Comparator<ICompletionProposal> PROPOSAL_COMPARATOR = new Comparator<ICompletionProposal>() {
		private final ICompletionProposalSorter SORTER = CompletionFactory.SORTER;
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			return SORTER.compare(p1, p2);
		};
	};

	public MockManifestEditor(String text) {
		super(text);
	}

	public void assertCompletions(String... expectTextAfter) throws Exception {
		StringBuilder expect = new StringBuilder();
		StringBuilder actual = new StringBuilder();
		for (String after : expectTextAfter) {
			expect.append(after);
			expect.append("\n-------------------\n");
		}

		for (ICompletionProposal completion : getCompletions()) {
			MockEditor editor = new MockEditor(getText());
			editor.apply(completion);
			actual.append(editor.getText());
			actual.append("\n-------------------\n");
		}
		assertEquals(expect.toString(), actual.toString());
	}

	private ICompletionProposal[] getCompletions() throws Exception {
		ICompletionProposal[] props = completionEngine.getCompletions(getDocument(), selectionStart).toArray(new ICompletionProposal[0]);
		Arrays.sort(props, PROPOSAL_COMPARATOR);
		return props;
	}

}
