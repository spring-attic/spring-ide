/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertyIndex;
import org.springframework.ide.eclipse.boot.properties.editor.StsConfigMetadataRepositoryJsonLoader;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry;

/**
 * Support for Reconciling, Content Assist and Hover Text in spring properties
 * file all make use of a per-project index of spring properties metadata extracted
 * from project's classpath. This Index manager is responsible for keeping at most
 * one index per-project and to keep the index up-to-date.
 *
 * @author Kris De Volder
 */
public class SpringPropertiesIndexManager extends ListenerManager<Listener<SpringPropertiesIndexManager>> implements ClasspathListener {

	//TODO: More precise cache flushing?
	// right now, any detected change that may affect the cached metadata results.
	// in clearing the entire cache.
	// Probably this is okay, since reading the data is pretty fast.

	private Map<String, SpringPropertyIndex> indexes = null;
	final private ValueProviderRegistry valueProviders;

	public SpringPropertiesIndexManager(ValueProviderRegistry valueProviders) {
		this.valueProviders = valueProviders;
		SpringPropertiesEditorPlugin.getClasspathListeners().addListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new LiveMetadataListener(), IResourceChangeEvent.POST_CHANGE);
	}

	public synchronized FuzzyMap<PropertyInfo> get(IJavaProject jp) {
		String key = jp.getElementName();
		if (indexes==null) {
			indexes = new HashMap<>();
		}
		SpringPropertyIndex index = indexes.get(key);
		if (index==null) {
			index = new SpringPropertyIndex(valueProviders, jp);
			indexes.put(key, index);
		}
		return index;
	}

	@Override
	public synchronized void classpathChanged(IJavaProject jp) {
		clear();
	}

	private void clear() {
		if (indexes!=null) {
			indexes.clear();
			for (Listener<SpringPropertiesIndexManager> l : getListeners()) {
				l.changed(this);
			}
		}
	}


	/**
	 * Called by LiveMetadataListener when a change to live json metadata file in the
	 * output folder of a IJavaProject is detected.
	 *
	 * @param The project on which the metadata change was detected.
	 * @param jsonFile The IFile in project's output folder that was changed.
	 */
	public synchronized void liveMetadataChanged(IJavaProject jp, IFile jsonFile) {
		clear();
	}

	private class LiveMetadataListener implements IResourceChangeListener, IResourceDeltaVisitor {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				event.getDelta().accept(this);
			} catch (Exception e) {
				SpringPropertiesEditorPlugin.log(e);
			}
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource r = delta.getResource();
			int t = r.getType();
			switch (t) {
			case IResource.PROJECT:
				IProject p = (IProject)r;
				if (p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject jp = JavaCore.create(p);
					IPath outputPath = jp.getOutputLocation().removeFirstSegments(1); //output path without project name.
					for (String metaLoc : StsConfigMetadataRepositoryJsonLoader.PROJECT_META_DATA_LOCATIONS) {
						IResourceDelta metaDelta = delta.findMember(outputPath.append(metaLoc));
						IFile jsonFile = getFile(metaDelta);
						if (jsonFile!=null) {
							//interesting change found.
							liveMetadataChanged(jp, jsonFile);
						}
					}
				} else {
					//TODO: not accessible (closed or deleted?)
					//  Do we need to do something with these?
				}
				return false;
			default:
				break;
			}
			return true;
		}

		/**
		 * @return IFile if this delta's resource is a file or null otherwise.
		 */
		private IFile getFile(IResourceDelta delta) {
			if (delta!=null) {
				IResource r = delta.getResource();
				if (r!=null) {
					int t = r.getType();
					if ((t|IResource.FILE)!=0) {
						return (IFile)r;
					}
				}
			}
			return null;
		}
	}

}
