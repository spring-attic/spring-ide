/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.util;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.CoreASTProvider;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;

/**
 *
 *
 */
public class AssistContextUtil {

	/**
	 * PT 162858442 - Some JDT internal API got refactored in Eclipse 4.10 that
	 * do not compile with older Eclipse. One of the things that changed was a
	 * flag that is used when getting an AST root for a given compilation unit:
	 * org.eclipse.jdt.core.manipulation.SharedASTProviderCore.WAIT_FLAG
	 * <p/>
	 * This flag was introduced in 4.10 in AssistContext and replaced an older
	 * related flag.
	 * <p/>
	 * In AssistContext, this flag can be passed in the constructor for use
	 * internally in the AssistContext to create an AST root, but this
	 * constructor is only available in Eclipse 4.10.
	 * <p/>
	 * An alternative to passing this flag is to instead create the AST root
	 * first and then set it in the AssistContext (See invocation of this method
	 * to see how it is used) via AssistContext setters. We can therefore create
	 * the AST root using a different flag from CoreASTProvider instead of the
	 * one from SharedASTProviderCore, the latter which is not available in
	 * Eclipse version older than 4.10
	 *
	 * @see org.eclipse.jdt.internal.ui.text.correction.AssistContext.getASTRoot()
	 * @see org.eclipse.jdt.core.manipulation.SharedASTProviderCore.getAST(ITypeRoot,
	 * WAIT_FLAG, IProgressMonitor)
	 * @param cu
	 * @param flag TODO
	 * @return
	 */
	public static CompilationUnit getASTRoot(ICompilationUnit cu, CoreASTProvider.WAIT_FLAG flag) {
		CompilationUnit aSTRoot = CoreASTProvider.getInstance().getAST(cu, flag, null);
		if (aSTRoot == null) {
			// see bug 63554
			aSTRoot = ASTResolving.createQuickFixAST(cu, null);
		}
		return aSTRoot;

		// This is the original code from AssistContext that we modified above:
		// The difference is that in our modification above, we call
		// CoreASTProvider directly and pass
		// a CoreASTProvider flag, which is available on all Eclipse versions,
		// rather than delegate to
		// SharedASTProviderCore, which uses a different flag type not available
		// in older versions of Eclipseß
//		if (fASTRoot == null) {
//			fASTRoot= SharedASTProviderCore.getAST(fCompilationUnit, fWaitFlag, null);
//			if (fASTRoot == null) {
//				// see bug 63554
//				fASTRoot= ASTResolving.createQuickFixAST(fCompilationUnit, null);
//			}
//		}
//		return fASTRoot;
	}

}
