/*******************************************************************************
 * Copyright (c) 2008, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ajdt.ui.actions;

import java.util.regex.Pattern;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.lazystart.Utils;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathContainer;
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
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class SpringAspectsToolingEnabler {

	// the stand alone spring aspects jar name
    final private static Pattern SPRING_ASPECTS_JAR_PATTERN = Pattern
            .compile(".*spring-aspects.*\\.jar");
	final private static String SPRING_ASPECTS_JAR_NAME = "spring-aspects.jar";

	// part of the name for the spring aspects jar included in BRITS.
	// the full name includes a version number at the end
	final private static Pattern SPRING_ASPECTS_PATTERN = Pattern
			.compile(".*org\\.springframework\\.aspects-.*\\.jar");

	final private IProject project;

	final private IJavaProject jProject;

	final private Shell shell;

	final private boolean askToChangeDefaultEditor;

	
	public SpringAspectsToolingEnabler(IProject project, Shell shell) {
	    this(project, shell, false);
	}

	public SpringAspectsToolingEnabler(IProject project, Shell shell,
			boolean askToChangeDefaultEditor) {
		this.project = project;
		this.jProject = JavaCore.create(project);
		this.askToChangeDefaultEditor = askToChangeDefaultEditor;
		this.shell = shell;
	}

	boolean run() {
		boolean isAJEditorDefault = false;
		try {
			if (hasSpringAspectsJar(jProject.getRawClasspath())) {
				if (!hasAJNature()) {
					AJDTUtils.addAspectJNature(project, false);
				}

				if (!isJarOnAspectPath(jProject.getRawClasspath())) {
					IClasspathEntry springAspectsEntry = findSpringAspectsJar(jProject
							.getRawClasspath());
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
								"Add " + SPRING_ASPECTS_JAR_NAME + " to build path", "Please add a version of "
										+ SPRING_ASPECTS_JAR_NAME + "\nto build path of "
										+ project.getName() + " and run this command again.");

			}
		}
		catch (CoreException e) {
			SpringCore.log("Error adding Spring tools to project "
					+ project, e);
		}
		return isAJEditorDefault;
	}

	private void makeAJEditorDefault() {
		EditorRegistry registry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();

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

	private IClasspathEntry findSpringAspectsJar(IClasspathEntry[] entries)
			throws JavaModelException {
		for (IClasspathEntry entry : entries) {
			if (isSpringAspectsEntry(entry)) {
				return entry;
			}
			else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathEntry containedEntry = findSpringAspectsJar(extractEntriesFomContainer(entry));
				if (containedEntry != null) {
					return containedEntry;
				}
			}
		}
		return null;
	}

	private boolean hasSpringAspectsJar(IClasspathEntry[] entries) throws JavaModelException {
		for (IClasspathEntry entry : entries) {
			if (isSpringAspectsEntry(entry)) {
				return true;
			}
			else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (hasSpringAspectsJar(extractEntriesFomContainer(entry))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isJarOnAspectPath(IClasspathEntry[] entries) throws JavaModelException {
		for (IClasspathEntry entry : entries) {
			if (isSpringAspectsEntry(entry)) {
				if (AspectJCorePreferences.isOnAspectpath(entry)) {
					return true;
				}
			}
			else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (isJarOnAspectPath(extractEntriesFomContainer(entry))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasAJNature() {
		return Utils.isAJProject(project);
	}

	private boolean isSpringAspectsEntry(IClasspathEntry entry) {
		String path = entry.getPath().toOSString();
		return SPRING_ASPECTS_JAR_PATTERN.matcher(path).matches()
				|| SPRING_ASPECTS_PATTERN.matcher(path).matches();
	}

	private IClasspathEntry[] extractEntriesFomContainer(IClasspathEntry containerEntry) {
		try {
			IClasspathContainer container = JavaCore.getClasspathContainer(
					containerEntry.getPath(), jProject);
			return container.getClasspathEntries();
		}
		catch (JavaModelException e) {
			SpringCore.log("Error accessing classpath container "
					+ containerEntry.getPath() + " from project " + project, e);
			return new IClasspathEntry[0];
		}
	}

	private boolean isAJEditorDefault() {
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(
				".java",
				ContentTypeManager.getInstance().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE));
		return desc.getId().equals(AspectJEditor.ASPECTJ_EDITOR_ID);
	}
}