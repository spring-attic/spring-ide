/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.eclipse.boot.properties.editor.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.editor.support.EditorSupportActivator;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;

/**
 * Helper class that provides methods to create 'actionLinks' that open
 * a Java type in a java editor.
 *
 * @author Kris De Volder
 */
public class JavaTypeLinks {

	private final HoverInfo hoverInfo;

	public JavaTypeLinks(HoverInfo hoverInfo) {
		this.hoverInfo = hoverInfo;
	}

	/**
	 * Creates an action link that opens a given Java type in an editor.
	 */
	public void javaTypeLink(HtmlBuffer html, final TypeUtil typeUtil, final Type type) {
		javaTypeLink(html, typeUtil, type, ""+type);
	}

	/**
	 * Creates an action link that opens a given Java type in an editor.
	 */
	public void javaTypeLink(HtmlBuffer html, final IJavaProject javaProject, final String typeStr) {
		javaTypeLink(html, new TypeUtil(javaProject), TypeParser.parse(typeStr), typeStr);
	}

	private void javaTypeLink(HtmlBuffer html, final TypeUtil typeUtil, final Type type, final String displayString) {
		actionLink(html, displayString, new Runnable() {
			public void run() {
				if (type!=null) {
					Job j = new UIJob("Open type "+type) {
						public IStatus runInUIThread(IProgressMonitor mon) {
							mon.beginTask("Open type "+type, 10);
							try {
								IType javaType = typeUtil.getJavaProject().findType(type.getErasure(), new SubProgressMonitor(mon, 8));
								if (javaType!=null) {
									JavaUI.openInEditor(javaType);
								}
							} catch (Exception e) {
								EditorSupportActivator.log(e);
							} finally {
								mon.done();
							}
							return Status.OK_STATUS;
						}
					};
					j.schedule();
				}
			};
		});
	}

	private void actionLink(HtmlBuffer html, String displayString, Runnable runnable) {
		hoverInfo.actionLink(html, displayString, runnable);
	}

}
