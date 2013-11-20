/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.SimpleProject;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentLocation;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.content.core.util.DescriptorReader;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.internal.content.core.DescriptorMatcher;

/**
 * Manages the content of Simple Projects that are bundled in the Wizard Plugin.
 * Addition content locations for Simple Projects can also be added to the
 * manager. The manager unzips the simple project files in a local installation
 * directory, and also read the files and adds them into a simple project data
 * model.
 * 
 */
public class SimpleProjectContentManager {

	private final List<ContentLocation> contentLocations = new ArrayList<ContentLocation>();

	private List<ContentItem> projectItems = null;

	private static SimpleProjectContentManager manager;

	public static final String INSTALL_DIRECTORY = "simpleprojects";

	public static SimpleProjectContentManager getManager() {
		if (manager == null) {
			manager = new SimpleProjectContentManager();
			ContentLocation location = WizardPlugin.getDefault().getTemplateContentLocation();
			manager.addContentLocation(location);
		}
		return manager;
	}

	/**
	 * Adds a non-null content location, if not already present. Initialises the
	 * content manager afterward.
	 * @param contentLocation
	 */
	public void addContentLocation(ContentLocation contentLocation) {
		if (contentLocation != null && !contentLocations.contains(contentLocation)) {
			contentLocations.add(contentLocation);
		}
	}

	/**
	 * Directory where simple project zip files are unzipped. Usually it is
	 * located in the workspace .metadata/.sts directory. If the installation
	 * directory does not exist, it will create it, as well as its parents if
	 * necessary.
	 * @return installation directory.
	 */
	public File getInstallDirectory() {
		ContentManager manager = ContentPlugin.getDefault().getManager();
		// Install Simple projects in a separate directory so that they do not
		// get managed by the content manager
		// for templates, since simple projects get treated differently.
		// However, the parent of the content manager
		// install directory should be the same (i.e. [workspace]/.metadata/.sts
		File file = new File(manager.getDataDirectory(), INSTALL_DIRECTORY);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	/**
	 * Creates content items for each simple project descriptor listed in a
	 * descriptor.xml file. The descriptor.xml file is indicated via a content
	 * location that can be set in the content manager. A content item is a
	 * lightweight representation of a descriptor that does not actually point
	 * to the simple project zip file, but rather contains metadata of the
	 * simple project. The list of content items is only read once per runtime
	 * session, as content items locations are not meant to be dynamically
	 * changed, unlike project templates. Simple project files are not handled
	 * or unzipped during this process.
	 * @return
	 * @throws CoreException if error occurred when reading descriptors from a
	 * content location.
	 */
	public List<ContentItem> readFromContentLocations() throws CoreException {

		if (projectItems != null && !projectItems.isEmpty()) {
			return projectItems;
		}

		projectItems = new ArrayList<ContentItem>();
		for (ContentLocation location : contentLocations) {
			File file = location.getContentLocationFile();
			if (file != null && file.exists()) {
				projectItems.addAll(read(file));
			}
		}

		return projectItems;

	}

	protected List<ContentItem> read(File file) throws CoreException {
		DescriptorMatcher matcher = new DescriptorMatcher(getInstallDirectory());
		DescriptorReader reader = new DescriptorReader();
		reader.read(file);
		List<Descriptor> descriptors = reader.getDescriptors();
		List<ContentItem> items = new ArrayList<ContentItem>();

		for (Descriptor descriptor : descriptors) {
			// Verify if the descriptor ID and version match an expected pattern
			// for the descriptor's
			// files. See the matcher for the actual matching criteria.
			if (!matcher.match(descriptor)) {
				continue;
			}
			ContentItem item = new ContentItem(descriptor.getId());
			item.setLocalDescriptor(descriptor);
			items.add(item);
		}
		return items;
	}

	/**
	 * Resolves a list of Simple Projects, each with loaded project data. This
	 * iterates through simple project descriptors, if necessary unzipping
	 * project data files, if they weren't already unzipped in a previous run in
	 * the installation directory for simple projects. I also loads simple
	 * project files into a data object that it sets in each simple project.
	 * Throws exception if errors occur in any of these stages.
	 * @param monitor
	 * @return List of simple projects resolved from simple project descriptors
	 * in a content location.
	 * @throws CoreException
	 */
	public List<SimpleProject> getSimpleProjects(IProgressMonitor monitor) throws CoreException {

		File baseDir = getInstallDirectory();

		if (baseDir == null || !baseDir.exists()) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							WizardPlugin.PLUGIN_ID,
							"Unable to unzip Spring simple project zip files, possibly due to write permission error when creating simple project content directory in: "
									+ ContentPlugin.getDefault().getManager().getDataDirectory().toString()));
		}

		List<ContentItem> projectItems = readFromContentLocations();

		List<SimpleProject> projects = new ArrayList<SimpleProject>();
		for (ContentItem item : projectItems) {
			if (SimpleProjectFactory.isSimpleProject(item)) {

				SimpleProject simpleProject = SimpleProjectFactory.getSimpleProject(item);
				if (simpleProject != null) {
					BundleContentLoader loader = new BundleContentLoader(item, WizardPlugin.getDefault().getBundle(),
							this);

					loader.load(monitor);

					// Now get the project install directory so that the project
					// data can be read from it
					File projectDir = new File(baseDir, item.getPath());

					TemplateProjectData data = new TemplateProjectData(projectDir);
					data.read();

					// Important that the data be set, otherwise the simple
					// project cannot be processed
					simpleProject.setTemplateData(data);

					projects.add(simpleProject);
				}

			}

		}

		return projects;

	}

	public boolean exists(File projectToLookFor) {
		File dir = getInstallDirectory();
		File[] children = dir.listFiles();
		if (children == null || children.length <= 0) {
			return false;
		}

		for (File childDirectory : children) {
			if (childDirectory.isDirectory() && childDirectory.getName().equals(projectToLookFor.getName())) {
				// If the installation directory for the simple project contains
				// one of the project files, and matches the versioned name of
				// the project to look for, assume it exists
				if ((new File(childDirectory, IContentConstants.TEMPLATE_DATA_FILE_NAME).exists())) {
					return true;
				}
			}
		}
		return false;
	}

}
