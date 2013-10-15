/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.internal;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.ARTIFACT_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCIES;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.GROUP_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.childEquals;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChilds;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getTextValue;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.performOnDOMDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.Operation;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.OperationTuple;
import org.eclipse.m2e.core.ui.internal.editing.PomHelper;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.StarterId;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class MavenSpringBootProject extends SpringBootProject {
	
	//TODO: all of this code completely ignores the version infos in SpringBootStarter objects.
	// This is ok assuming that versions always follow the 'managed' version in parent pom.
	// If that is not the case then... ??

	private static final List<SpringBootStarter> NO_STARTERS = Arrays
			.asList(new SpringBootStarter[0]);

	private IProject project;

	public MavenSpringBootProject(IProject project) {
		Assert.isNotNull(project);
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	/**
	 * @return List of maven coordinates for known boot starters. These are
	 *         discovered dynamically based on project contents. E.g. for maven
	 *         projects we examine the 'dependencyManagement' section of the
	 *         project's effective pom.
	 * 
	 * @throws CoreException
	 */
	@Override
	public List<SpringBootStarter> getKnownStarters() throws CoreException {
		MavenProject mp = getMavenProject();
		DependencyManagement depMan = mp.getDependencyManagement();
		if (depMan != null) {
			List<Dependency> deps = depMan.getDependencies();
			return getStarters(deps);
		}
		return NO_STARTERS;
	}

	private MavenProject getMavenProject() throws CoreException {
		IMavenProjectRegistry pr = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mpf = pr.getProject(project);
		MavenProject mp = mpf.getMavenProject(new NullProgressMonitor());
		return mp;
	}

	private IFile getPomFile() {
		return project.getFile(new Path("pom.xml"));
	}

	@Override
	public List<SpringBootStarter> getBootStarters() throws CoreException {
		return getStarters(getMavenProject().getDependencies());
	}

	private List<SpringBootStarter> getStarters(List<Dependency> deps) {
		if (deps != null) {
			ArrayList<SpringBootStarter> starters = new ArrayList<SpringBootStarter>();
			for (Dependency _dep : deps) {
				IMavenCoordinates dep = new MavenCoordinates(_dep.getGroupId(),
						_dep.getArtifactId(), _dep.getVersion());
				if (SpringBootStarter.isStarter(dep)) {
					starters.add(new SpringBootStarter(dep));
				}
			}
			return starters;
		}
		return NO_STARTERS;
	}

	@Override
	public void removeStarter(final SpringBootStarter starter)
			throws CoreException {
		try {
			List<SpringBootStarter> starters = getBootStarters();
			boolean changed = starters.remove(starter);
			if (changed) {
				setStarters(starters);
			}
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void addStarter(final SpringBootStarter starter)
			throws CoreException {
		try {
			List<SpringBootStarter> starters = getBootStarters();
			boolean changed = starters.add(starter);
			if (changed) {
				setStarters(starters);
			}
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void setStarters(Collection<SpringBootStarter> _starters) throws CoreException {
		try {
			final Set<StarterId> starters = new HashSet<StarterId>();
			for (SpringBootStarter s : _starters) {
				starters.add(s.getId());
			}
			
			IFile file = getPomFile();
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				public void process(Document document) {
					Element depsEl = getChild(
							document.getDocumentElement(), DEPENDENCIES);
					List<Element> children = findChilds(depsEl, DEPENDENCY);
					for (Element c : children) {
						//We only care about 'starter' dependencies. Leave everything else alone.
						// Also... don't touch nodes that are already there, unless they are to
						// be removed. This way we don't mess up versions, comments or other stuff
						// that a user may have inserted via manual edits.
						String aid = getTextValue(findChild(c, ARTIFACT_ID));
						String gid = getTextValue(findChild(c, GROUP_ID));
						if (aid!=null && gid!=null) { //ignore invalid entries that don't have gid or aid
							if (aid.startsWith(SpringBootStarter.AID_PREFIX)) {
								StarterId id = new StarterId(gid, aid);
								boolean keep = starters.remove(id);
								if (!keep) {
									depsEl.removeChild(c);
								}
							}
						}
					}
					
					//if 'starters' is not empty at this point, it contains remaining ids we have not seen 
					// in the pom, so we need to add them.
					for (StarterId s : starters) {
						PomHelper.createDependency(depsEl,
								s.getGroupId(), s.getArtifactId(),
								null);						
					}
				}
			}));
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}
}
