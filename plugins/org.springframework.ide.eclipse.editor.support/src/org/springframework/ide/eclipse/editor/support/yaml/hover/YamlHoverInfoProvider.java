/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.hover;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfoProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 * Abstract superclass that implements {@link HoverInfoProvider} for Yaml files
 * using the {@link YamlAssistContext}.
 *
 * @author Kris De Volder
 */
public abstract class YamlHoverInfoProvider implements HoverInfoProvider {

	private YamlASTProvider astProvider;

	protected YamlHoverInfoProvider(YamlASTProvider astProvider) {
		this.astProvider = astProvider;
	}

	@Override
	public IRegion getHoverRegion(IDocument document, int offset) {
		YamlFileAST ast = getAst(document);
		if (ast!=null) {
			Node n = ast.findNode(offset);
			if (n!=null && n.getNodeId()==NodeId.scalar) {
				int start = n.getStartMark().getIndex();
				int end = n.getEndMark().getIndex();
				return new Region(start, end-start);
			}
		}
		return null;
	}

	public YamlFileAST getAst(IDocument doc) {
		try {
			return astProvider.getAST(doc);
		} catch (ParserException|ScannerException e) {
			//ignore, the user just typed some crap
		}
		return null;
	}

	//TODO: Nothing in here yet. Must pull up stuff from subclasss

}
