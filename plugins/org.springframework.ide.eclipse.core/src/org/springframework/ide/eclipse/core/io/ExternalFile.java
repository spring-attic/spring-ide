/*******************************************************************************
 * Copyright (c) 2008 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.util.StringUtils;

/**
 * {@link IFile} implementation which makes workspace external resources accessible for the Spring IDE core model using
 * {@link IResource} as core resource abstraction.
 * <p>
 * NOTE: This implementation can't be used by third parties.
 * @author Christian Dupuis
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class ExternalFile extends AbstractResource implements IFile {

	private final File file;

	private final String entryName;

	private final IProject project;

	private final Map<Long, ExternalMarker> markers = new ConcurrentHashMap<Long, ExternalMarker>();

	private final AtomicLong markerId = new AtomicLong();

	public ExternalFile(File file, String entryName, IProject project) {
		this.file = file;
		this.project = project;
		this.entryName = entryName;
	}

	public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void appendContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
			throws CoreException {
		// no-op
	}

	public void create(InputStream source, boolean force, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void create(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void createLink(URI location, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public String getCharset() throws CoreException {
		return null;
	}

	public String getCharset(boolean checkImplicit) throws CoreException {
		return null;
	}

	public String getCharsetFor(Reader reader) throws CoreException {
		return null;
	}

	public IContentDescription getContentDescription() throws CoreException {
		return null;
	}

	public InputStream getContents() throws CoreException {
		try {
			ZipFile file = new ZipFile(this.file);
			String cleanedEntryName = entryName;
			if (cleanedEntryName.length() > 1 && cleanedEntryName.charAt(0) == '/') {
				cleanedEntryName = cleanedEntryName.substring(1);
			}
			ZipEntry entry = file.getEntry(cleanedEntryName);
			if (entry == null) {
				throw new CoreException(SpringCore.createErrorStatus("Invalid path '" + cleanedEntryName + "'", null));
			}
			
			return InputStreamUtils.getWrappedInputStream(file, entry);
		}			
		catch (IOException e) {
			throw new CoreException(SpringCore.createErrorStatus(e.getMessage(), e));
		}
	}

	public InputStream getContents(boolean force) throws CoreException {
		return getContents();
	}

	public int getEncoding() throws CoreException {
		return 0;
	}

	public IPath getFullPath() {
		return new Path(getFilename());
	}

	public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
		return new IFileState[0];
	}

	public String getName() {
		return entryName;
	}

	public boolean isReadOnly() {
		return true;
	}

	public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor)
			throws CoreException {
		// no-op
	}

	public void setCharset(String newCharset) throws CoreException {
		// no-op
	}

	public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void setContents(InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
			throws CoreException {
		// no-op
	}

	public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor)
			throws CoreException {
		// no-op
	}

	public void accept(IResourceVisitor visitor) throws CoreException {
		// no-op
	}

	public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException {
		// no-op
	}

	public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms) throws CoreException {
		// no-op
	}

	public void accept(IResourceProxyVisitor visitor, int depth, int memberFlags) throws CoreException {
		// no-op
	}

	public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
		// no-op
	}

	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	@Override
	public Resource createRelative(String relativePath) throws IOException {
		String pathToUse = StringUtils.applyRelativePath(entryName, relativePath);
		return new EclipsePathMatchingResourcePatternResolver(project).getResource(pathToUse);
	}

	public IMarker createMarker(String type) throws CoreException {
		long id = markerId.incrementAndGet();
		ExternalMarker marker = new ExternalMarker(id, type, this);
		markers.put(id, marker);
		return marker;
	}

	public IResourceProxy createProxy() {
		return null;
	}

	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException {
		// no-op
	}

	public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		// no-op
	}

	public boolean exists() {
		return file.exists();
	}

	public IMarker findMarker(long id) throws CoreException {
		return markers.get(id);
	}

	public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException {
		Set<IMarker> newMarkers = new HashSet<IMarker>();
		for (ExternalMarker marker : this.markers.values()) {
			if (marker.getType().equals(type) || (includeSubtypes && marker.isSubtypeOf(type))) {
				newMarkers.add(marker);
			}
		}
		return (IMarker[]) newMarkers.toArray(new IMarker[newMarkers.size()]);
	}

	public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth) throws CoreException {
		return 0;
	}

	public String getFileExtension() {
		return file.getName();
	}

	public long getLocalTimeStamp() {
		return file.lastModified();
	}

	public IPath getLocation() {
		return new Path(file.getAbsolutePath());
	}

	public URI getLocationURI() {
		return file.toURI();
	}

	public IMarker getMarker(long id) {
		return markers.get(id);
	}

	public long getModificationStamp() {
		return file.lastModified();
	}

	public IContainer getParent() {
		return project;
	}

	public Map getPersistentProperties() throws CoreException {
		return Collections.EMPTY_MAP;
	}

	public String getPersistentProperty(QualifiedName key) throws CoreException {
		return key.getQualifier();
	}

	public IProject getProject() {
		return this.project;
	}

	public IPath getProjectRelativePath() {
		return new Path(file.getAbsolutePath());
	}

	public IPath getRawLocation() {
		return new Path(getFilename());
	}

	public URI getRawLocationURI() {
		return file.toURI();
	}

	public ResourceAttributes getResourceAttributes() {
		return null;
	}

	public Map getSessionProperties() throws CoreException {
		return null;
	}

	public Object getSessionProperty(QualifiedName key) throws CoreException {
		return null;
	}

	public int getType() {
		return IResource.FILE;
	}

	public IWorkspace getWorkspace() {
		return project.getWorkspace();
	}

	public boolean isAccessible() {
		return true;
	}

	public boolean isDerived() {
		return false;
	}

	public boolean isDerived(int options) {
		return false;
	}

	public boolean isHidden() {
		return false;
	}

	public boolean isLinked() {
		return false;
	}

	public boolean isLinked(int options) {
		return false;
	}

	public boolean isLocal(int depth) {
		return false;
	}

	public boolean isPhantom() {
		return false;
	}

	public boolean isSynchronized(int depth) {
		return false;
	}

	public boolean isTeamPrivateMember() {
		return false;
	}

	public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor) throws CoreException {
	}

	public void move(IProjectDescription description, boolean force, boolean keepHistory, IProgressMonitor monitor)
			throws CoreException {
	}

	public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException {
	}

	public void revertModificationStamp(long value) throws CoreException {
	}

	public void setDerived(boolean isDerived) throws CoreException {
	}

	public void setHidden(boolean isHidden) throws CoreException {
	}

	public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException {
	}

	public long setLocalTimeStamp(long value) throws CoreException {
		return 0;
	}

	public void setPersistentProperty(QualifiedName key, String value) throws CoreException {
	}

	public void setReadOnly(boolean readOnly) {
	}

	public void setResourceAttributes(ResourceAttributes attributes) throws CoreException {
	}

	public void setSessionProperty(QualifiedName key, Object value) throws CoreException {
	}

	public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException {
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
	}

	public Object getAdapter(Class adapter) {
		if (adapter == ZipEntryStorage.class) {
			return new ZipEntryStorage(this, this.entryName);
		}
		else if (adapter == IResource.class) {
			return this;
		}
		return null;
	}

	public boolean contains(ISchedulingRule rule) {
		return this == rule;
	}

	public boolean isConflicting(ISchedulingRule rule) {
		return this == rule;
	}

	public String getDescription() {
		return "external resource [" + getFilename() + "]";
	}

	@Override
	public String getFilename() throws IllegalStateException {
		return file.getAbsolutePath() + ZipEntryStorage.DELIMITER + entryName;
	}

	public InputStream getInputStream() throws IOException {
		try {
			return getContents();
		}
		catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public File getFile() throws IOException {
		return file;
	}
	
	public boolean isHidden(int options) {
		return false;
	}

	public boolean isTeamPrivateMember(int options) {
		return false;
	}

	public boolean hasFilters() {
		return false;
	}

	public boolean isVirtual() {
		return true;
	}

	public void setDerived(boolean arg0, IProgressMonitor arg1) throws CoreException {
	}


	class ExternalMarker implements IMarker {

		private final Map<String, Object> attributes = new HashMap<String, Object>();

		private final long creationTime = System.currentTimeMillis();

		private final long id;

		private final ExternalFile resource;

		private final String type;

		ExternalMarker(long id, String type, ExternalFile resource) {
			this.id = id;
			this.type = type;
			this.resource = resource;
		}

		public void delete() throws CoreException {
			resource.markers.remove(id);
		}

		public boolean exists() {
			return true;
		}

		public Object getAttribute(String attributeName) throws CoreException {
			return attributes.get(attributeName);
		}

		public int getAttribute(String attributeName, int defaultValue) {
			return (attributes.containsKey(attributeName) ? (Integer) attributes.get(attributeName) : defaultValue);
		}

		public String getAttribute(String attributeName, String defaultValue) {
			return (attributes.containsKey(attributeName) ? (String) attributes.get(attributeName) : defaultValue);
		}

		public boolean getAttribute(String attributeName, boolean defaultValue) {
			return (attributes.containsKey(attributeName) ? (Boolean) attributes.get(attributeName) : defaultValue);
		}

		public Map getAttributes() throws CoreException {
			return attributes;
		}

		public Object[] getAttributes(String[] attributeNames) throws CoreException {
			return null;
		}

		public long getCreationTime() throws CoreException {
			return creationTime;
		}

		public long getId() {
			return id;
		}

		public IResource getResource() {
			return resource;
		}

		public String getType() throws CoreException {
			return type;
		}

		public boolean isSubtypeOf(String superType) throws CoreException {
			return ((Workspace) getWorkspace()).getMarkerManager().isSubtype(getType(), type);
		}

		public void setAttribute(String attributeName, int value) throws CoreException {
			attributes.put(attributeName, value);
		}

		public void setAttribute(String attributeName, Object value) throws CoreException {
			attributes.put(attributeName, value);
		}

		public void setAttribute(String attributeName, boolean value) throws CoreException {
			attributes.put(attributeName, value);
		}

		@SuppressWarnings("unchecked")
		public void setAttributes(Map attributes) throws CoreException {
			this.attributes.putAll(attributes);
		}

		public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
			for (int i = 0; i < attributeNames.length; i++) {
				setAttribute(attributeNames[i], values[i]);
			}
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

	}

	public IPathVariableManager getPathVariableManager() {
		return ResourcesPlugin.getWorkspace().getPathVariableManager();
	}

	public boolean isFiltered() {
		return false;
	}

}
