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
package org.springframework.ide.eclipse.yaml.editor;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.HoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyHoverInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.NodeRef;
import org.springframework.ide.eclipse.yaml.editor.ast.PathUtil;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlASTProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlFileAST;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;

/**
 * @author Kris De Volder
 */
public class YamlHoverInfoProvider implements IPropertyHoverInfoProvider {

	private YamlASTProvider astProvider;
	private SpringPropertyIndexProvider indexProvider;
	private DocumentContextFinder contextFinder;

	public YamlHoverInfoProvider(YamlASTProvider astProvider, SpringPropertyIndexProvider indexProvider, DocumentContextFinder contextFinder) {
		this.astProvider = astProvider;
		this.indexProvider = indexProvider;
		this.contextFinder = contextFinder;
	}

	@Override
	public HoverInfo getHoverInfo(IDocument doc, IRegion r) {
		YamlFileAST ast = getAst(doc);
		if (ast!=null) {
			IJavaProject jp = contextFinder.getJavaProject(doc); //Note in some contexts jp may be null
			FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
			if (index!=null) {
				List<NodeRef<?>> path = ast.findPath(r.getOffset());
				String propExp = PathUtil.toPropertyPrefixString(path);
				PropertyInfo info = index.findLongestCommonPrefixEntry(propExp);
				if (info!=null && propExp.equals(info.getId())) {
					return new SpringPropertyHoverInfo(jp, info);
				}
				//return HoverInfo.withText(propExp);
			}
		}
		return null;
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


	private YamlFileAST getAst(IDocument doc) {
		return astProvider.getAST(doc);
	}

}
