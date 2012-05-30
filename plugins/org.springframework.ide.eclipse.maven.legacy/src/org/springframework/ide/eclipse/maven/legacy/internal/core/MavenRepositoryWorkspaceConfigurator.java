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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.maven.ide.eclipse.MavenPlugin;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.maven.legacy.MavenCorePlugin;
import org.springframework.util.FileCopyUtils;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.configurator.WorkspaceLocationConfiguratorParticipant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * {@link WorkspaceLocationConfiguratorParticipant} to configure a maven repository with M2Eclipse.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.5.0
 */
public class MavenRepositoryWorkspaceConfigurator extends WorkspaceLocationConfiguratorParticipant {

	@Override
	public String getPath() {
		return "maven-repository";
	}

	@Override
	public String getVersionRange() {
		return "0.0.0";
	}

	@Override
	protected ConfigurableExtension doCreateExtension(File location, IProgressMonitor monitor) {
		if (MavenCorePlugin.IS_M2ECLIPSE_PRESENT) {
			return new M2EclipseDependentRepositoryExtension(location.getName(), location);
		}
		return null;
	}

	/**
	 * Inner class to prevent binary dependency to M2Eclipse.
	 */
	private class M2EclipseDependentRepositoryExtension extends ConfigurableExtension {

		private static final String LOCAL_REPOSITORY_ELEMENT_NAME = "localRepository";

		private static final String QUESTION = "SpringSource Tool Suite has detected a local Maven repository at:\n"
				+ "\n"
				+ "    %s\n"
				+ "\n"
				+ "Do you want to merge the contents into your global Maven repository? This will make the repository contents available from Maven builds outside of STS as well.";

		private static final String SETTINGS_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<settings></settings>";

		private File location;

		private String oldUserSettingsFileName;

		public M2EclipseDependentRepositoryExtension(String id, File location) {
			super(id);
			this.location = location;
			setLocation(location.getAbsolutePath());
			setLabel("Maven Repository");
		}

		/**
		 * {@inheritDoc}
		 */
		public IStatus configure(final IProgressMonitor monitor) {
			final String userSettingsFileName = MavenPlugin.getDefault().getMavenConfiguration().getUserSettingsFile();
			this.oldUserSettingsFileName = userSettingsFileName;

			UIJob job = new UIJob("Configure Maven Repository") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						File globalRespository = MavenPlugin.getDefault().getRepositoryRegistry().getLocalRepository()
								.getBasedir();

						if (ask(location.getCanonicalPath())) {
							PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
							Resource[] resources = resolver.getResources("file://" + location.getCanonicalPath()
									+ File.separatorChar + "**");
							for (Resource resource : resources) {
								File fileResource = resource.getFile();
								if (fileResource.isFile()) {
									String relativePath = fileResource.getCanonicalPath().substring(
											location.getCanonicalPath().length() + 1,
											fileResource.getCanonicalPath().length());
									monitor.subTask(String.format("Copying '%s'", relativePath));
									File newFileResource = new File(globalRespository, relativePath);
									if (!newFileResource.exists()) {
										newFileResource.getParentFile().mkdirs();
										FileCopyUtils.copy(fileResource, newFileResource);
									}
								}
							}
						}
						else {
							File userSettingsFile = new File(userSettingsFileName);
							// check if the file already exists; if not create new one
							if (!userSettingsFile.exists()) {
								BufferedWriter writer = new BufferedWriter(new FileWriter(getSettingsFile()));
								writer.append(SETTINGS_TEMPLATE);
								writer.close();
							}
							else {
								// if the settings.xml file exists; copy it into the workspace
								FileCopyUtils.copy(userSettingsFile, getSettingsFile());
							}

							// load file to search for existing <localRepository> node
							Document document = SpringCoreUtils.parseDocument(getSettingsFile().toURI());
							NodeList nodes = document.getDocumentElement().getChildNodes();
							boolean found = false;
							for (int i = 0; i < nodes.getLength(); i++) {
								Node node = nodes.item(i);
								if (LOCAL_REPOSITORY_ELEMENT_NAME.equals(node.getLocalName())
										|| LOCAL_REPOSITORY_ELEMENT_NAME.equals(node.getNodeName())) {
									// point to the detected maven repository
									node.setTextContent(location.getCanonicalPath());
									found = true;
									break;
								}
							}

							// if no <localRepository> node exists, create a new one
							if (!found) {
								Element node = document.createElement(LOCAL_REPOSITORY_ELEMENT_NAME);
								node.appendChild(document.createTextNode(location.getCanonicalPath()));
								document.getDocumentElement().appendChild(node);
							}

							// write the new settings file out to disk
							TransformerFactory transformerFactory = TransformerFactory.newInstance();
							Transformer transformer = transformerFactory.newTransformer();
							transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
							transformer.setOutputProperty(OutputKeys.INDENT, "yes");
							transformer.setOutputProperty(OutputKeys.ENCODING, document.getInputEncoding());

							Writer out = new OutputStreamWriter(new FileOutputStream(getSettingsFile()),
									document.getInputEncoding());
							StreamResult result = new StreamResult(out);
							DOMSource source = new DOMSource(document);
							transformer.transform(source, result);
							out.close();

							// set the new settings.xml path
							MavenPlugin.getDefault().getMavenConfiguration()
									.setUserSettingsFile(getSettingsFile().getCanonicalPath());
						}
					}
					catch (Exception e) {
						MavenCorePlugin.getDefault().getLog()
								.log(new Status(IStatus.ERROR, MavenCorePlugin.NON_LEGACY_PLUGIN_ID, 1, e.getMessage(), e));
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};

			// we need to wait until the UI job completes as otherwise the workbench might shut down
			final boolean[] done = new boolean[1];
			job.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					done[0] = true;
				}
			});

			job.schedule();

			while (!done[0]) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			}

			return Status.OK_STATUS;
		}

		/**
		 * {@inheritDoc}
		 */
		public IStatus unConfigure(IProgressMonitor monitor) {
			MavenPlugin.getDefault().getMavenConfiguration().setUserSettingsFile(oldUserSettingsFileName);
			return Status.OK_STATUS;
		}

		private boolean ask(final String repositoryPath) {
			return MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Configure Maven Repository",
					String.format(QUESTION, repositoryPath));
		}

		private File getSettingsFile() {
			return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), "settings.xml");
		}
	}

}
