/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ajdt.ui.actions;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.lazystart.Utils;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * @author Andrew Eisenberg
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class SpringAspectsToolingEnabler {

	final private static String SPRING_ASPECTS_JAR_NAME = "spring-aspects.jar";

	final private IProject project;

	final private Shell shell;

	final private boolean askToChangeDefaultEditor;

	public SpringAspectsToolingEnabler(IProject project, Shell shell,
			boolean askToChangeDefaultEditor) {
		this.project = project;
		this.askToChangeDefaultEditor = askToChangeDefaultEditor;
		this.shell = shell;
	}

	boolean run() {
		boolean isAJEditorDefault = false;
		try {
			if (hasSpringAspectsJar()) {
				if (!hasAJNature()) {
					AJDTUtils.addAspectJNature(project, false);
				}

				if (!isJarOnAspectPath()) {
					IClasspathEntry springAspectsEntry = getSpringAspectsJar();
					AspectJCorePreferences.addToAspectPath(project, springAspectsEntry);
				}

				if (askToChangeDefaultEditor) {
					if (!isAJEditorDefault()) {
						isAJEditorDefault = MessageDialog
								.openQuestion(
										shell,
										"Make AspectJ editor default?",
										"Do you want to make the AspectJ editor the default editor for editing Java files?\n\n"
												+ "This option can be changed by going to Preferences -> General-> Editors -> File Associations");
						if (isAJEditorDefault) {
							makeAJEditorDefault();
						}
					}
					else {
						isAJEditorDefault = true;
					}
				}
			}
			else {
				// can't do anything unless
				// spring jar is on build path
				MessageDialog
						.openInformation(shell,
								"Add " + SPRING_ASPECTS_JAR_NAME + " to build path", "Please add "
										+ SPRING_ASPECTS_JAR_NAME + " to build path of "
										+ project.getName() + " and run this command again.");

			}
		}
		catch (CoreException e) {
			SpringCore.log("Error adding Spring tools to project " + project, e);
		}
		return isAJEditorDefault;
	}

	private void makeAJEditorDefault() {
		EditorRegistry registry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();

		// will not work because this will not persist across sessions
		// registry.setDefaultEditor("*.java", AspectJEditor.ASPECTJ_EDITOR_ID);
		// registry.saveAssociations();

		IFileEditorMapping[] mappings = registry.getFileEditorMappings();
		for (IFileEditorMapping mapping : mappings) {
			if (mapping.getExtension().equals("java")) {
				if (mapping instanceof FileEditorMapping) {
					IEditorDescriptor desc = registry.findEditor(AspectJEditor.ASPECTJ_EDITOR_ID);
					((FileEditorMapping) mapping).setDefaultEditor((EditorDescriptor) desc);
				}
			}
		}
		registry.setFileEditorMappings((FileEditorMapping[]) mappings);
		registry.saveAssociations();
	}

	private boolean hasSpringAspectsJar() throws JavaModelException {
		IJavaProject jProject = JavaCore.create(project);
		IClasspathEntry[] entries = jProject.getRawClasspath();
		for (IClasspathEntry entry : entries) {
			if (entry.getPath().toOSString().endsWith(SPRING_ASPECTS_JAR_NAME)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAJNature() {
		return Utils.isAJProject(project);
	}

	private boolean isJarOnAspectPath() throws JavaModelException {
		IJavaProject jProject = JavaCore.create(project);
		IClasspathEntry[] entries = jProject.getRawClasspath();
		for (IClasspathEntry entry : entries) {
			if (entry.getPath().toOSString().endsWith(SPRING_ASPECTS_JAR_NAME)) {
				if (AspectJCorePreferences.isOnAspectpath(entry)) {
					return true;
				}
			}
		}
		return false;
	}

	private IClasspathEntry getSpringAspectsJar() throws JavaModelException {
		IJavaProject jProject = JavaCore.create(project);
		IClasspathEntry[] entries = jProject.getRawClasspath();
		for (IClasspathEntry entry : entries) {
			if (entry.getPath().toOSString().endsWith(SPRING_ASPECTS_JAR_NAME)) {
				return entry;
			}
		}
		return null;
	}

	private boolean isAJEditorDefault() {
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(
				".java",
				ContentTypeManager.getInstance().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE));
		return desc.getId().equals(AspectJEditor.ASPECTJ_EDITOR_ID);
	}
}