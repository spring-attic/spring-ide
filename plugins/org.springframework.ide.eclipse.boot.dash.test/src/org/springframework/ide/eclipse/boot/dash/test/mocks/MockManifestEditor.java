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
import org.springframework.ide.eclipse.boot.properties.editor.test.MockYamlEditor;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYmlSchema;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.completions.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YamlSchema;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Kris De Volder
 */
public class MockManifestEditor extends MockYamlEditor {

	private static class Config {
		Yaml yaml = new Yaml();
		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlASTProvider astProvider = new YamlASTProvider(yaml);
		YamlSchema schema = new ManifestYmlSchema();
		YamlAssistContextProvider assistContextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		HoverInfoProvider hoverProvider = new YamlHoverInfoProvider(astProvider, structureProvider, assistContextProvider);
	}

	private Comparator<ICompletionProposal> PROPOSAL_COMPARATOR = new Comparator<ICompletionProposal>() {
		private final ICompletionProposalSorter SORTER = CompletionFactory.SORTER;
		public int compare(ICompletionProposal p1, ICompletionProposal p2) {
			return SORTER.compare(p1, p2);
		};
	};
	private YamlCompletionEngine completionEngine;

	public MockManifestEditor(String text) {
		this(text, new Config());
	}

	private MockManifestEditor(String text, Config config) {
		super(text, config.structureProvider, config.astProvider, config.hoverProvider);
		this.completionEngine = new YamlCompletionEngine(config.structureProvider, config.assistContextProvider);
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
