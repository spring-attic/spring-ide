/*******************************************************************************
 *  Copyright (c) 2012, 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.texteditor.templates.TemplatesView;

/**
 * Default perspective for STS
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @version 2.3.0
 */
public class StsPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
		folder.addView(JavaUI.ID_PACKAGES);
		folder.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
		folder.addPlaceholder("org.eclipse.ui.views.ResourceNavigator");
		folder.addPlaceholder("org.eclipse.ui.navigator.ProjectExplorer");
		layout.addFastView("org.eclipse.jdt.junit.ResultView", (float) 0.25);
		layout.addFastView("org.springframework.ide.eclipse.aop.ui.navigator.aopReferenceModelNavigator", (float) 0.25);
		layout.addFastView("org.eclipse.contribution.xref.ui.views.XReferenceView", (float) 0.25);

		IFolderLayout serverFolder = layout.createFolder("server", IPageLayout.BOTTOM, (float) 0.80, "left");
		serverFolder.addView("org.eclipse.wst.server.ui.ServersView");

		IFolderLayout tasklistFolder = layout.createFolder("topright", IPageLayout.RIGHT, (float) 0.75, editorArea); //$NON-NLS-1$
		tasklistFolder.addView("org.eclipse.mylyn.tasks.ui.views.tasks");
		IFolderLayout springFolder = layout.createFolder("spring", IPageLayout.BOTTOM, (float) 0.50, "topright");
		springFolder.addView("org.springframework.ide.eclipse.ui.navigator.springExplorer");

		IFolderLayout outlineFolder = layout.createFolder("middleright", IPageLayout.BOTTOM, (float) 0.55, "topright"); //$NON-NLS-1$
		outlineFolder.addView(IPageLayout.ID_OUTLINE);
		outlineFolder.addPlaceholder(TemplatesView.ID);

		IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.80, editorArea); //$NON-NLS-1$
		outputfolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		outputfolder.addView("org.eclipse.ui.views.AllMarkersView");
		outputfolder.addView(IProgressConstants.PROGRESS_VIEW_ID);
		outputfolder.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder(IPageLayout.ID_TASK_LIST);
		outputfolder.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
		outputfolder.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
		outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder("com.springsource.sts.ide.metadata.ui.RequestMappingView");
		outputfolder.addPlaceholder("com.springsource.sts.roo.ui.rooShellView");
		outputfolder.addPlaceholder("*");

		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		// actions - add AJDT actions
		layout.addActionSet("ajelementCreation");

		// views - java
		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
		layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

		// views - search
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - debugging
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		// layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut("org.eclipse.ui.views.AllMarkersView");
		// TODO e3.5 replace with IPageLayout.ID_PROJECT_EXPLORER
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer");
		// layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
		layout.addShowViewShortcut(TemplatesView.ID);

		// views - springsource views
		layout.addShowViewShortcut("com.springsource.sts.ide.metadata.ui.RequestMappingView");
		layout.addShowViewShortcut("org.springframework.ide.eclipse.aop.ui.navigator.aopReferenceModelNavigator");
		layout.addShowViewShortcut("com.springsource.sts.roo.ui.rooShellView");
		layout.addShowViewShortcut("org.springframework.ide.eclipse.aop.ui.tracing.eventTraceView");
		layout.addShowViewShortcut("org.eclipse.contribution.xref.ui.views.XReferenceView");
		layout.addShowViewShortcut("org.eclipse.mylyn.tasks.ui.views.tasks");
		layout.addShowViewShortcut("org.eclipse.wst.server.ui.ServersView");

		// new files
		layout.addNewWizardShortcut("ajaspectwizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
		//		layout.addNewWizardShortcut("org.codehaus.groovy.eclipse.ui.groovyClassWizard");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.junit.wizards.NewTestCaseCreationWizard");//$NON-NLS-1$
		//		layout.addNewWizardShortcut("org.codehaus.groovy.eclipse.ui.groovyJUnitWizard");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.springframework.ide.eclipse.beans.ui.wizards.newBeansConfig");
		layout.addNewWizardShortcut("org.springframework.ide.eclipse.webflow.ui.wizard.newWebflowConfigWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

		// new projects
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("ajprojectwizard");
		layout.addNewWizardShortcut("com.springsource.sts.wizard.template");
		layout.addNewWizardShortcut("org.springsource.ide.eclipse.commons.gettingstarted.wizard.boot.NewSpringBootWizard");
		layout.addNewWizardShortcut("com.springsource.sts.roo.ui.wizard.newRooProjectWizard");
		// layout.addNewWizardShortcut("org.codehaus.groovy.eclipse.ui.groovyProjectWizard");
		layout.addNewWizardShortcut("org.grails.ide.eclipse.ui.wizard.newGrailsProjectWizard");
		layout.addNewWizardShortcut("org.eclipse.wst.web.ui.internal.wizards.SimpleWebProjectWizard");
		layout.addNewWizardShortcut("org.eclipse.jst.servlet.ui.project.facet.WebProjectWizard");

		// new perspectives
		layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");
		layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
		layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaHierarchyPerspective");
		layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaBrowsingPerspective");
		layout.addPerspectiveShortcut("org.grails.ide.eclipse.perspective");

	}
}
