/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.core.internal.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.xml.XMLWriter;

/**
 * This class saves the description of a Spring Beans project to an XML file.
 * @author Torsten Juergeleit
 */
public class BeansProjectDescriptionWriter
								  implements IBeansProjectDescriptionConstants {
	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
												   "/project/description/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	public static void write(IProject project,
							 final BeansProjectDescription description) {
		final IFile file = project.getFile(new Path(
				IBeansProject.DESCRIPTION_FILE));
		if (DEBUG) {
			System.out.println("Writing project description to " +
							   file.getLocation().toString());
		}
		Job currentJob = Platform.getJobManager().currentJob();
		if (currentJob != null && currentJob.getRule().contains(file)) {
			write(file, description);
		} else {

			// Write project description via a separate workspace job so we
			// ommit the problem with the locked workspace
			WorkspaceJob job = new WorkspaceJob(BeansCorePlugin
					.getFormattedMessage("BeansProjectDescription.write",
							file.getFullPath().toString())) {
	
				public boolean belongsTo(Object family) {
					return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
				}
	
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					write(file, description);
					return Status.OK_STATUS;
				}
			};
			job.setRule(file.getProject());
			job.schedule();
		}
	}

	protected static void write(IFile file,
				BeansProjectDescription description) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				XMLWriter writer = new XMLWriter(os);
				write(description, writer);
				writer.flush();
				writer.close();
			} finally {
				os.close();
			}
			if (!file.exists()) {
				file.create(new ByteArrayInputStream(os.toByteArray()),
													 IResource.FORCE, null);
			} else {
				file.setContents(new ByteArrayInputStream(os.toByteArray()),
								 IResource.FORCE, null);
			}
		} catch (IOException e) {
			BeansCorePlugin.log("Error writing " + file.getFullPath(), e);
		} catch (CoreException e) {
			BeansCorePlugin.log(e.getStatus());
		}
	}

	protected static void write(BeansProjectDescription description,
								XMLWriter writer) throws IOException {
		writer.startTag(PROJECT_DESCRIPTION, null);
		write(CONFIG_EXTENSIONS, CONFIG_EXTENSION,
			  description.getConfigExtensions(), writer);
		write(CONFIGS, CONFIG, description.getConfigNames(), writer);
		write(CONFIG_SETS, description.getConfigSets(), writer);
		writer.endTag(PROJECT_DESCRIPTION);
	}

	protected static void write(IBeansConfigSet configSet, XMLWriter writer)
															throws IOException {
		writer.startTag(CONFIG_SET, null);
		writer.printSimpleTag(NAME, configSet.getElementName());
		writer.printSimpleTag(OVERRIDING, new Boolean(
					   configSet.isAllowBeanDefinitionOverriding()).toString());
		writer.printSimpleTag(INCOMPLETE,
							  new Boolean(configSet.isIncomplete()).toString());
		write(CONFIGS, CONFIG, configSet.getConfigs(), writer);
		writer.endTag(CONFIG_SET);
	}

	protected static void write(String name, Collection collection,
								XMLWriter writer) throws IOException {
		writer.startTag(name, null);
		for (Iterator iter = collection.iterator(); iter.hasNext(); ) {
			write(iter.next(), writer);
		}
		writer.endTag(name);
	}

	protected static void write(Object obj, XMLWriter writer)
															throws IOException {
		if (obj instanceof IBeansConfigSet) {
			write((BeansConfigSet) obj, writer);
		}
	}

	protected static void write(String name, String elementTagName,
						  String[] array, XMLWriter writer) throws IOException {
		writer.startTag(name, null);
		for (int i = 0; i < array.length; i++) {
			writer.printSimpleTag(elementTagName, array[i]);
		}
		writer.endTag(name);
	}

	protected static void write(String name, String elementTagName,
				   Collection collection, XMLWriter writer) throws IOException {
		writer.startTag(name, null);
		for (Iterator iter = collection.iterator(); iter.hasNext(); ) {
			writer.printSimpleTag(elementTagName, iter.next());
		}
		writer.endTag(name);
	}
}
