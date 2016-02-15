/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import static org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesCompletionEngine.debug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.ide.eclipse.boot.util.FileUtil;

/**
 * Load a {@link ConfigMetadataRepository} from the content of an eclipse
 * projects classpath.
 *
 * @author Kris De Volder
 */
public class StsConfigMetadataRepositoryJsonLoader {

	public static final String ADDITIONAL_SPRING_CONFIGURATION_METADATA_JSON = "META-INF/additional-spring-configuration-metadata.json";

	/**
	 * The default classpath location for config metadata.
	 */
	public static final String[] META_DATA_LOCATIONS = {
		"META-INF/spring-configuration-metadata.json",
		ADDITIONAL_SPRING_CONFIGURATION_METADATA_JSON
	};

	private ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();

	/**
	 * Load the {@link ConfigMetadataRepository} with the metadata of the current
	 * classpath using the {@link #DEFAULT_LOCATION_PATTERN}. If the same config
	 * metadata items is held within different resources, the first that is
	 * loaded is kept which means the result is not deterministic.
	 */
	public ConfigurationMetadataRepository load(IJavaProject project) throws Exception {
		debug(">> load ConfigurationMetadataRepository for "+project.getElementName());
		IClasspathEntry[] classpath = project.getResolvedClasspath(true);
		for (IClasspathEntry e : classpath) {
			int ekind = e.getEntryKind();
			int ckind = e.getContentKind();
			IPath path = e.getPath();
			if (ekind==IClasspathEntry.CPE_LIBRARY && ckind==IPackageFragmentRoot.K_BINARY) {
				//jar file dependency
				File jarFile = path.toFile();
				if (FileUtil.isJarFile(jarFile)) {
					loadFromJar(jarFile);
				}
			} else if (ekind==IClasspathEntry.CPE_PROJECT) {
				loadFromProjectDependency(e);
			} else {
				debug("Skipped: "+ekind(ekind)+" "+ckind(ckind)+": "+path);
			}
		}
		loadFromOutputFolder(project);
		ConfigurationMetadataRepository repository = builder.build();
		debug("<< load ConfigurationMetadataRepository for "+project.getElementName()+": "+repository.getAllProperties().size()+" properties");
		return repository;
	}

	private void loadFromProjectDependency(IClasspathEntry entry) {
		try {
			String pname = entry.getPath().segment(0);
			if (pname!=null) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
				if (p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
					loadFromOutputFolder(JavaCore.create(p));
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}

	private void loadFromOutputFolder(IJavaProject project) {
		try {
			IPath outputLoc = project.getOutputLocation();
			if (outputLoc!=null) {
				IFolder outputFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(outputLoc);
				for (String mdLoc : META_DATA_LOCATIONS) {
					IFile mdf = outputFolder.getFile(new Path(mdLoc));
					loadFromJsonFile(mdf);
				}
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}

	private void loadFromJsonFile(IFile mdf) {
		if (mdf.exists()) {
			InputStream is = null;
			try {
				is = mdf.getContents(true);
				loadFromInputStream(is);
			} catch (Exception e) {
				SpringPropertiesEditorPlugin.log(e);
			} finally {
				if (is!=null) {
					try {
						is.close();
					} catch (IOException e) {
						//ignore
					}
				}
			}
		}
	}

	private void loadFromJar(File f) {
		debug("load from jar: "+f);
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(f);
			//jarDump(jarFile);
			for (String loc : META_DATA_LOCATIONS) {
				ZipEntry e = jarFile.getEntry(loc);
				if (e!=null) {
					loadFrom(jarFile, e);
				}
			}
		} catch (Throwable e) {
			SpringPropertiesEditorPlugin.log(e);
		} finally {
			if (jarFile!=null) {
				try {
					jarFile.close();
				} catch (IOException e) {
				}
			}
		}
	}


	private void loadFrom(JarFile jarFile, ZipEntry ze) {
		InputStream is = null;
		try {
			is = jarFile.getInputStream(ze);
			loadFromInputStream(is);
		} catch (Throwable e) {
			SpringPropertiesEditorPlugin.log(e);
		} finally {
			if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void loadFromInputStream(InputStream is) throws IOException {
		builder.withJsonResource(is);
	}

	/// Debug utils
	private String ckind(int ckind) {
		switch (ckind) {
		case IPackageFragmentRoot.K_SOURCE:
			return "SRC";
		case IPackageFragmentRoot.K_BINARY:
			return "BIN";
		default:
			return ""+ckind;
		}
	}

	private String ekind(int ekind) {
		switch (ekind) {
		case IClasspathEntry.CPE_SOURCE:
			return "SRC";
		case IClasspathEntry.CPE_LIBRARY:
			return "LIB";
		case IClasspathEntry.CPE_PROJECT:
			return "PRJ";
		case IClasspathEntry.CPE_VARIABLE:
			return "VAR";
		case IClasspathEntry.CPE_CONTAINER:
			return "CON";
		default:
			return ""+ekind;
		}
	}

//	private void jarDump(JarFile jarFile) {
//		Enumeration<JarEntry> entries = jarFile.entries();
//		while (entries.hasMoreElements()) {
//			JarEntry e = entries.nextElement();
//			System.out.println(e.getName());
//		}
//	}

}
