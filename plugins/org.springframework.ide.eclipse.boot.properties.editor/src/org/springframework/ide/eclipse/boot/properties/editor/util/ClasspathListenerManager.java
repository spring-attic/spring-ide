/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * An instance of this class provides a means to register
 * listeners that get notified when classpath for a IJavaProject
 * changes.
 *
 * Deprecated. This provides very similar functionality to
 * ClasspathListenerManager in frameworks.core.
 * This one should probably go away.
 *
 * @author Kris De Volder
 */
@Deprecated
public class ClasspathListenerManager extends ListenerManager<ClasspathListener>{

	private class MyListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			visit(event.getDelta());
		}

		private void visit(IJavaElementDelta delta) {
			IJavaElement el = delta.getElement();
			switch (el.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				visitChildren(delta);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isClasspathChanged(delta.getFlags())) {
					notifyListeners((IJavaProject)el);
				}
				break;
			default:
				break;
			}
		}

		private boolean isClasspathChanged(int flags) {
			return 0!= (flags & (
					IJavaElementDelta.F_CLASSPATH_CHANGED |
					IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED
			));
		}

		public void visitChildren(IJavaElementDelta delta) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				visit(c);
			}
		}
	}


	public ClasspathListenerManager() {
		JavaCore.addElementChangedListener(new MyListener(), ElementChangedEvent.POST_CHANGE);
	}


	protected void notifyListeners(IJavaProject el) {
		for (ClasspathListener l : getListeners()) {
			l.classpathChanged(el);
		}
	}

}
