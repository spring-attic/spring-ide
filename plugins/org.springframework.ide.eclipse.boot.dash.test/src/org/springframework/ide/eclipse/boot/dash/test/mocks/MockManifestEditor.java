/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.springframework.ide.eclipse.boot.properties.editor.test.MockEditor;
import org.springframework.ide.eclipse.boot.properties.editor.test.MockProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.test.MockYamlEditor;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYamlReconcileEngine;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestYmlSchema;
import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.reconcile.IReconcileEngine;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
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
		YamlSchema schema = new ManifestYmlSchema(null);
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
	private YamlSchema schema;

	public MockManifestEditor(String text) {
		this(text, new Config());
	}

	private MockManifestEditor(String text, Config config) {
		super(text, config.structureProvider, config.astProvider, config.hoverProvider);
		this.schema = config.schema;
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

	public ICompletionProposal[] getCompletions() throws Exception {
		ICompletionProposal[] props = completionEngine.getCompletions(getDocument(), selectionStart).toArray(new ICompletionProposal[0]);
		Arrays.sort(props, PROPOSAL_COMPARATOR);
		return props;
	}

	public void assertProblems(String... expectedProblems) throws Exception {
		List<ReconcileProblem> actualProblems = reconcile();
		String bad = null;
		if (actualProblems.size()!=expectedProblems.length) {
			bad = "Wrong number of problems (expecting "+expectedProblems.length+" but found "+actualProblems.size()+")";
		} else {
			for (int i = 0; i < expectedProblems.length; i++) {
				if (!matchProblem(actualProblems.get(i), expectedProblems[i])) {
					bad = "First mismatch at index "+i+": "+expectedProblems[i]+"\n";
					break;
				}
			}
		}
		if (bad!=null) {
			fail(bad+problemSumary(actualProblems));
		}
	}

	public String problemSumary(List<ReconcileProblem> actualProblems) throws BadLocationException {
		StringBuilder buf = new StringBuilder();
		for (ReconcileProblem p : actualProblems) {
			buf.append("\n----------------------\n");

			String snippet = getText(p.getOffset(), p.getLength());
			buf.append("("+p.getOffset()+", "+p.getLength()+")["+snippet+"]:\n");
			buf.append("   "+p.getMessage());
		}
		return buf.toString();
	}

	public List<ReconcileProblem> reconcile() {
		IReconcileEngine reconciler = createReconcileEngine();
		MockProblemCollector problems=new MockProblemCollector();
		reconciler.reconcile(getDocument(), problems, new NullProgressMonitor());
		return problems.getAllProblems();
	}

	protected IReconcileEngine createReconcileEngine() {
		return new ManifestYamlReconcileEngine(astProvider, schema);
	}

	private boolean matchProblem(ReconcileProblem problem, String expect) {
		String[] parts = expect.split("\\|");
		assertEquals(2, parts.length);
		String badSnippet = parts[0];
		String messageSnippet = parts[1];
		try {
			String actualBadSnippet = getText(problem.getOffset(), problem.getLength()).trim();
			return actualBadSnippet.equals(badSnippet)
					&& problem.getMessage().contains(messageSnippet);
		} catch (BadLocationException e) {
			return false;
		}
	}


}
