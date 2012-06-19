/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.legacy.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.actions.UpdateSourcesAction;
import org.maven.ide.eclipse.project.IMavenProjectChangedListener;
import org.maven.ide.eclipse.project.MavenProjectChangedEvent;
import org.springframework.ide.eclipse.maven.legacy.MavenCorePlugin;


/**
 * Abstraction around the M2Eclipse {@link IMavenProjectChangedListener}. This
 * implementation will register a listener with {@link MavenPlugin}.
 * <p>
 * On changes to a project's <code>packaging</code> configuration setting in the
 * <code>pom.xml</code> the implementation will trigger the Maven Update
 * Configuration operation.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class MavenProjectChangeHandler {

	private MavenProjectChangeListener listener;

	/**
	 * Register the change handler with M2Eclipse
	 */
	public void register() {
		if (listener == null && MavenCorePlugin.IS_M2ECLIPSE_PRESENT) {
			listener = new MavenProjectChangeListener();
			MavenPlugin.getDefault().getMavenProjectManager().addMavenProjectChangedListener(listener);
		}
	}

	/**
	 * Un-register the change handler with M2Eclipse
	 */
	public void unregister() {
		if (listener != null && MavenCorePlugin.IS_M2ECLIPSE_PRESENT) {
			MavenPlugin.getDefault().getMavenProjectManager().removeMavenProjectChangedListener(listener);
		}
	}

	/**
	 * Internal class to prevent binary dependencies to M2Eclipse in {@link MavenProjectChangeHandler}. 
	 */
	private static class MavenProjectChangeListener implements IMavenProjectChangedListener {

		public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor arg1) {
			for (MavenProjectChangedEvent event : events) {

				// If the packaging has changed re-run the project configuration
				if (event.getMavenProject() != null && event.getOldMavenProject() != null && event.getMavenProject().getPackaging() != null
						&& !event.getMavenProject().getPackaging().equals(event.getOldMavenProject().getPackaging())) {

					final IProject project = event.getMavenProject().getProject();

					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							UpdateSourcesAction action = new UpdateSourcesAction(Display.getDefault().getActiveShell());
							action.selectionChanged(null, new StructuredSelection(project));
							action.run(null);
						}
					});

				}
			}
		}
	}

}
