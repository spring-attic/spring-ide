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
package org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.boot.properties.editor.yaml.ast.YamlFileAST;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

public class SpringYamlReconcileEngine implements IReconcileEngine {

	private YamlASTProvider parser;
	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;

	public SpringYamlReconcileEngine(YamlASTProvider astProvider, SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider) {
		this.parser = astProvider;
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
	}

	@Override
	public void reconcile(IDocument doc, IProblemCollector problemCollector, IProgressMonitor mon) {
		problemCollector.beginCollecting();
		try {
			YamlFileAST ast = parser.getAST(doc);
			FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
			if (index!=null && !index.isEmpty()) {
				IndexNavigator nav = IndexNavigator.with(index);
				YamlASTReconciler reconciler = new YamlASTReconciler(problemCollector, typeUtilProvider.getTypeUtil(doc));
				reconciler.reconcile(ast, nav, mon);
			}
		} catch (ParserException e) {
			String msg = e.getProblem();
			Mark mark = e.getProblemMark();
			problemCollector.accept(SpringPropertyProblem.error(msg, mark.getIndex(), 1));
		} catch (ScannerException e) {
			String msg = e.getProblem();
			Mark mark = e.getProblemMark();
			problemCollector.accept(SpringPropertyProblem.error(msg, mark.getIndex(), 1));
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		} finally {
			problemCollector.endCollecting();
		}
	}

}
