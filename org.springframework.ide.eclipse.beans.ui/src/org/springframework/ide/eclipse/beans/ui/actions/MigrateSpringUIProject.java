/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescription;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectNature;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionHandler;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUILabelDecorator;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MigrateSpringUIProject implements IObjectActionDelegate {

	private static final String SPRINGUI_CORE_PLUGIN_ID =
											 "org.springframework.eclipse.core";
	private static final String SPRINGUI_NATURE_ID = SPRINGUI_CORE_PLUGIN_ID +
																".springnature";
	private static final String SPRINGUI_BUILDER_ID = SPRINGUI_CORE_PLUGIN_ID +
															 ".springvalidator";
	private static final String SPRINGUI_MARKER_ID = SPRINGUI_CORE_PLUGIN_ID +
															   ".problemmarker";
	private static final String SPRINGUI_PROJECT_DESCRIPTION = ".springProject";

	private IWorkbenchPart targetPart;
	private List selected = new ArrayList();

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
    }

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		if (selection instanceof IStructuredSelection) {
			boolean enabled = true;
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				if (obj instanceof IProject) {
					IProject project = (IProject) obj;
					if (!project.isOpen()) {
						enabled = false;
						break;
					} else {
						selected.add(project);
					}
				} else {
					enabled = false;
					break;
				}
			}
			action.setEnabled(enabled);
		}
	}

	public void run(IAction action) {
		Iterator iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = (IProject) iter.next();
			migrateProject(project);
		}

		// Refresh label decoration for Spring beans project and config files
		BeansUILabelDecorator.update();
	}

	/**
	 * Removes SpringUI's nature and builder (validation) from given project.
	 * The SpringUI project description is migrated to the Beans project format.
	 * All SpringUI problem markers are deleted and finally the Beans project
	 * nature is added.
	 */
	private void migrateProject(IProject project) {

		// remove SpringUI nature and buidler
		BeansCoreUtils.removeProjectBuilder(project, SPRINGUI_BUILDER_ID);
		BeansCoreUtils.removeProjectNature(project, SPRINGUI_NATURE_ID);

		// migrate SpringUI's project description
		IFile file = project.getFile(new Path(SPRINGUI_PROJECT_DESCRIPTION));
		if (file.isAccessible()) {
			BeansProjectDescription description = read(file);
			if (description != null) {
				deleteProblemMarkers(project, description);
				BeansProjectDescriptionWriter.write(project, description);					
			}
			try {
				file.delete(true, null);
			} catch (CoreException e) {
				BeansUIPlugin.log(e);
			}
		}

		// finally add beans project nature
		BeansCoreUtils.addProjectNature(project, BeansProjectNature.NATURE_ID);
	}

	private void deleteProblemMarkers(IProject project,
									  BeansProjectDescription description) {
		Iterator iter = description.getConfigNames().iterator();
		while (iter.hasNext()) {
			IFile file = project.getFile(new Path((String) iter.next()));
			if (file.isAccessible()) {
				try {
					file.deleteMarkers(SPRINGUI_MARKER_ID, false,
									   IResource.DEPTH_ZERO);
				} catch (CoreException e) {
					BeansCorePlugin.log(e);
				}
			}
		}
	}

	private BeansProjectDescription read(IFile file)  {
		BufferedInputStream is = null;
		try {
			IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
			BeansProjectDescriptionHandler handler =
								 new SpringUIProjectDescriptionHandler(project);
			is = new BufferedInputStream(file.getContents());
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);
				SAXParser parser = factory.newSAXParser();
				parser.parse(new InputSource(is), handler);
			} catch (ParserConfigurationException e) {
				handler.log(IStatus.ERROR, e);
			} catch (SAXException e) {
				handler.log(IStatus.WARNING, e);
			} catch (IOException e) {
				handler.log(IStatus.WARNING, e);
			}
			IStatus status = handler.getStatus(); 
			switch (status.getSeverity()) {
				case IStatus.ERROR :
					BeansCorePlugin.log(status);
					return null;
	
				case IStatus.WARNING :
				case IStatus.INFO :
					BeansCorePlugin.log(status);
	
				case IStatus.OK :
				default :
					return handler.getDescription();
			}
		} catch (CoreException e) {
			BeansCorePlugin.log(e.getStatus());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	private class SpringUIProjectDescriptionHandler
										extends BeansProjectDescriptionHandler {
		public String PROJECT_DESCRIPTION = "springProjectDescription";
		
		public SpringUIProjectDescriptionHandler(IBeansProject project) {
			super(project);
		}

		public void startElement(String uri, String elementName, String qname,
								 Attributes attributes) throws SAXException {
			//clear the character buffer at the start of every element
			charBuffer.setLength(0);
			switch (state) {
				case S_INITIAL :
					if (elementName.equals(PROJECT_DESCRIPTION)) {
						state = S_PROJECT_DESC;
						description = new BeansProjectDescription(project);
					} else {
						throw new SAXParseException("No Spring project description",
													locator);
					}
					break;

				default :
					super.startElement(uri, elementName, qname, attributes);
					break;
			}
		}
	}
}
