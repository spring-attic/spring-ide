///*******************************************************************************
// * Copyright (c) 2015 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * https://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.text.IDocument;
//import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
//import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
//import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
//
///**
// * A fake reconcule engine which is not useful except for quickly testing
// * whether stuff is wired up correctly to the editor.
// */
//public class BadWordReconcileEngine implements IReconcileEngine {
//
//	private final String[] BADWORDS = {
//			"bar", "foo"
//	};
//
//	public BadWordReconcileEngine() {
//	}
//
//	@Override
//	public void reconcile(IDocument doc, IProblemCollector problemCollector, IProgressMonitor mon) {
//		String text = doc.get();
//		System.out.println(">>>> reconciling for bad words ==========");
//		System.out.println(text);
//		System.out.println("<<<< reconciling for bad words ==========");
//
//		problemCollector.beginCollecting();
//		try {
//			for (String badword : BADWORDS) {
//				int pos = 0;
//				while (pos>=0 && pos < text.length()) {
//					int badPos = text.indexOf(badword, pos);
//					if (badPos>=0) {
//						if (badword.equals(BADWORDS[0])) {
//							problemCollector.accept(SpringPropertyProblem.error("'"+badword+"' is a bad word", badPos, badword.length()));
//						} else {
//							problemCollector.accept(SpringPropertyProblem.warning("'"+badword+"' is a bad word", badPos, badword.length()));
//						}
//						pos = badPos+1;
//					} else {
//						pos = badPos;
//					}
//				}
//			}
//		} finally {
//			problemCollector.endCollecting();
//		}
//	}
//
//}
