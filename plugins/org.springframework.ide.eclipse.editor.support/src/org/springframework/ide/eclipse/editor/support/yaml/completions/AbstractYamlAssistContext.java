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
package org.springframework.ide.eclipse.editor.support.yaml.completions;

import org.springframework.ide.eclipse.editor.support.completions.CompletionFactory;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SDocNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SRootNode;

/**
 * @author Kris De Volder
 */
public abstract class AbstractYamlAssistContext implements YamlAssistContext {

	/**
	 * Delete a content assist query from the document, and also the line of
	 * text in the document that contains it, if that line of text contains just the
	 * query surrounded by whitespace.
	 */
	public static void deleteQueryAndLine(YamlDocument doc, String query, int queryOffset, YamlPathEdits edits) throws Exception {
		edits.delete(queryOffset, query);
		String wholeLine = doc.getLineTextAtOffset(queryOffset);
		if (wholeLine.trim().equals(query.trim())) {
			edits.deleteLineBackwardAtOffset(queryOffset);
		}
	}

	public final int documentSelector;
	public final YamlPath contextPath;

	public AbstractYamlAssistContext(int documentSelector, YamlPath contextPath) {
		this.documentSelector = documentSelector;
		this.contextPath = contextPath;
	}

	protected SNode getContextNode(YamlDocument file) throws Exception {
		return contextPath.traverse((SNode)getContextRoot(file));
	}

	protected SDocNode getContextRoot(YamlDocument file) throws Exception {
		SRootNode root = file.getStructure();
		return (SDocNode) root.getChildren().get(documentSelector);
	}

	protected CompletionFactory completionFactory() {
		return CompletionFactory.DEFAULT;
	}

	@Override
	public HoverInfo getHoverInfo() {
		return null;
	}


}
