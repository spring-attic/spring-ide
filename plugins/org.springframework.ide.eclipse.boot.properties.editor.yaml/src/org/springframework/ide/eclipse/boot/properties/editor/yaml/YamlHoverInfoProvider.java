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
package org.springframework.ide.eclipse.boot.properties.editor.yaml;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.IPropertyHoverInfoProvider;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.RelaxedNameConfig;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.completions.ApplicationYamlAssistContext;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPath;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

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
				List<NodeRef<?>> astPath = ast.findPath(r.getOffset());
				YamlPath path = YamlPath.fromASTPath(astPath);
				if (path!=null) {
					if (path.pointsAtKey()) {
						//When a path points at a key we must tramsform it to a 'value-terminating path'
						// to be able to reuse the 'getHoverInfo' method on YamlAssistContext (as navigation
						// into 'key' is not defined for YamlAssistContext.
						String key = path.getLastSegment().toPropString();
						path = path.dropLast().append(YamlPathSegment.valueAt(key));
					}
					ApplicationYamlAssistContext assistContext = (ApplicationYamlAssistContext) ApplicationYamlAssistContext.forPath(path, index, null, new TypeUtil(jp), RelaxedNameConfig.ALIASSED);
					if (assistContext!=null) {
						return assistContext.getHoverInfo();
					}
				}
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
		try {
			return astProvider.getAST(doc);
		} catch (ParserException|ScannerException e) {
			//ignore, the user just typed some crap
		}
		return null;
	}

}
