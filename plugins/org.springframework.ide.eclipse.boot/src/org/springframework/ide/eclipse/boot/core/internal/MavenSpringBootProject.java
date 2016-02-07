/*******************************************************************************
 * Copyright (c) 2013, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.internal;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.ARTIFACT_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.CLASSIFIER;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCIES;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY_MANAGEMENT;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.GROUP_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.NAME;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.OPTIONAL;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.SCOPE;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.TYPE;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.URL;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.VERSION;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.childEquals;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.createElement;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.createElementWithText;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChilds;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.format;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getTextValue;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.performOnDOMDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.Operation;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.OperationTuple;
import org.eclipse.m2e.core.ui.internal.editing.PomHelper;
import org.springframework.ide.eclipse.boot.core.Bom;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.Repo;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class MavenSpringBootProject extends SpringBootProject {

	//TODO: properly handle pom manipulation when pom file is open / dirty in an editor.
	// minimum requirement: detect and prohibit by throwing an error.

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static final String REPOSITORIES = "repositories";
	private static final String REPOSITORY = "repository";
	private static final String SNAPSHOTS = "snapshots";

	private static final String ENABLED = "enabled";

	private IProject project;

	public MavenSpringBootProject(IProject project, InitializrService initializr) {
		super(initializr);
		Assert.isNotNull(project);
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	private MavenProject getMavenProject() throws CoreException {
		IMavenProjectRegistry pr = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mpf = pr.getProject(project);
		if (mpf!=null) {
			return mpf.getMavenProject(new NullProgressMonitor());
		}
		return null;
	}

	private IFile getPomFile() {
		return project.getFile(new Path("pom.xml"));
	}

	@Override
	public List<IMavenCoordinates> getDependencies() throws CoreException {
		MavenProject mp = getMavenProject();
		if (mp!=null) {
			return toMavenCoordinates(mp.getDependencies());
		}
		return Collections.emptyList();
	}



	private List<IMavenCoordinates> toMavenCoordinates(List<Dependency> dependencies) {
		ArrayList<IMavenCoordinates> converted = new ArrayList<>(dependencies.size());
		for (Dependency d : dependencies) {
			converted.add(new MavenCoordinates(d.getGroupId(), d.getArtifactId(), d.getClassifier(), d.getVersion()));
		}
		return converted;
	}

	/**
	 * Determine the 'managed' version, if any, associate with a given dependency.
	 * @return Version string or null.
	 */
	private String getManagedVersion(IMavenCoordinates dep) {
		try {
			MavenProject mp = getMavenProject();
			if (mp!=null) {
				DependencyManagement managedDeps = mp.getDependencyManagement();
				if (managedDeps!=null) {
					List<Dependency> deps = managedDeps.getDependencies();
					if (deps!=null && !deps.isEmpty()) {
						for (Dependency d : deps) {
							if ("jar".equals(d.getType())) {
								if (dep.getArtifactId().equals(d.getArtifactId()) && dep.getGroupId().equals(d.getGroupId())) {
									return d.getVersion();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	@Override
	public void addMavenDependency(final IMavenCoordinates dep, final boolean preferManagedVersion) throws CoreException {
		addMavenDependency(dep, preferManagedVersion, false);
	}

	@Override
	public void addMavenDependency(
			final IMavenCoordinates dep,
			final boolean preferManagedVersion, final boolean optional
	) throws CoreException {
		try {
			IFile file = getPomFile();
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				public void process(Document document) {
					Element depsEl = getChild(
							document.getDocumentElement(), DEPENDENCIES);
					if (depsEl==null) {
						//TODO: handle this case
					} else {
						String version = dep.getVersion();
						String managedVersion = getManagedVersion(dep);
						if (managedVersion!=null) {
							//Decide whether we can/should inherit the managed version or override it.
							if (preferManagedVersion || managedVersion.equals(version)) {
								version = null;
							}
						} else {
							//No managed version. We have to include a version in xml added to the pom.
						}
						Element xmlDep = PomHelper.createDependency(depsEl,
								dep.getGroupId(),
								dep.getArtifactId(),
								version
						);
						if (optional) {
							createElementWithText(xmlDep, OPTIONAL, "true");
							format(xmlDep);
						}
					}
				}
			}));
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void setStarters(Collection<SpringBootStarter> _starters) throws CoreException {
		try {
			final Set<MavenId> starters = new HashSet<MavenId>();
			for (SpringBootStarter s : _starters) {
				starters.add(s.getMavenId());
			}

			IFile file = getPomFile();
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				public void process(Document pom) {
					Element depsEl = getChild(
							pom.getDocumentElement(), DEPENDENCIES);
					List<Element> children = findChilds(depsEl, DEPENDENCY);
					for (Element c : children) {
						//We only care about 'starter' dependencies. Leave everything else alone.
						// Also... don't touch nodes that are already there, unless they are to
						// be removed. This way we don't mess up versions, comments or other stuff
						// that a user may have inserted via manual edits.
						String aid = getTextValue(findChild(c, ARTIFACT_ID));
						String gid = getTextValue(findChild(c, GROUP_ID));
						if (aid!=null && gid!=null) { //ignore invalid entries that don't have gid or aid
							if (isKnownStarter(new MavenId(gid, aid))) {
								MavenId id = new MavenId(gid, aid);
								boolean keep = starters.remove(id);
								if (!keep) {
									depsEl.removeChild(c);
								}
							}
						}
					}

					//if 'starters' is not empty at this point, it contains remaining ids we have not seen
					// in the pom, so we need to add them.
					for (MavenId mid : starters) {
						SpringBootStarter starter = getStarter(mid);
						createDependency(depsEl, starter.getDependency(), starter.getScope());
						createBomIfNeeded(pom, starter.getBom());
						createRepoIfNeeded(pom, starter.getRepo());
					}
				}


			}));
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void removeMavenDependency(final MavenId mavenId) {
		IFile file = getPomFile();
		try {
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				@Override
				public void process(Document pom) {
					Element depsEl = getChild(
							pom.getDocumentElement(), DEPENDENCIES);
					if (depsEl!=null) {
						Element dep = findChild(depsEl, DEPENDENCY,
								childEquals(GROUP_ID, mavenId.getGroupId()),
								childEquals(ARTIFACT_ID, mavenId.getArtifactId())
						);
						if (dep!=null) {
							depsEl.removeChild(dep);
						}
					}
				}
			}));
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	@Override
	public Job updateProjectConfiguration() {
		Job job = new UpdateMavenProjectJob(new IProject[] {
				getProject()
		});
		job.schedule();
		return job;
 	}

	@Override
	public String getBootVersion() {
		try {
			MavenProject mp = getMavenProject();
			if (mp!=null) {
				return getBootVersion(mp.getDependencies());
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return SpringBootCore.getDefaultBootVersion();
	}

	private String getBootVersion(List<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			if (dep.getArtifactId().startsWith("spring-boot") && dep.getGroupId().equals("org.springframework.boot")) {
				return dep.getVersion();
			}
		}
		return SpringBootCore.getDefaultBootVersion();
	}

	private void createRepoIfNeeded(Document pom, Repo repo) {
		if (repo!=null) {
			addReposIfNeeded(pom, Collections.singletonList(repo));
		}
	}

	private void createBomIfNeeded(Document pom, Bom bom) {
		if (bom!=null) {
			Element bomList = ensureDependencyMgmtSection(pom);
			Element existing = PomEdits.findChild(bomList, DEPENDENCY,
					childEquals(GROUP_ID, bom.getGroupId()),
					childEquals(ARTIFACT_ID, bom.getArtifactId())
			);
			if (existing==null) {
				createBom(bomList, bom);
				addReposIfNeeded(pom, bom.getRepos());
			}
		}
	}

	private Element ensureDependencyMgmtSection(Document pom) {
		/* Ensure that this exists in the pom:
	<dependencyManagement>
		<dependencies> <---- RETURNED
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-starter-parent</artifactId>
				<version>Brixton.M3</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
		 */
		boolean needFormatting = false;
		Element doc = pom.getDocumentElement();
		Element depman = findChild(doc, DEPENDENCY_MANAGEMENT);
		if (depman==null) {
			depman = createElement(doc, DEPENDENCY_MANAGEMENT);
			needFormatting = true;
		}
		Element deplist = findChild(depman, DEPENDENCIES);
		if (deplist==null) {
			deplist = createElement(depman, DEPENDENCIES);
		}
		if (needFormatting) {
			format(depman);
		}
		return deplist;
	}

	private static Element createBom(Element parentList, Bom bom) {
		/*
	<dependencyManagement>
		<dependencies> <---- parentList
			<dependency> <---- create and return
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-starter-parent</artifactId>
				<version>Brixton.M3</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
		 */

		String groupId = bom.getGroupId();
		String artifactId = bom.getArtifactId();
		String version = bom.getVersion();
		String classifier = bom.getClassifier();
		String type = "pom";
		String scope = "import";

		Element dep = createElement(parentList, DEPENDENCY);

		if(groupId != null) {
			createElementWithText(dep, GROUP_ID, groupId);
		}
		createElementWithText(dep, ARTIFACT_ID, artifactId);
		if(version != null) {
			createElementWithText(dep, VERSION, version);
		}
		createElementWithText(dep, TYPE, type);
		if (scope !=null && !scope.equals("compile")) {
			createElementWithText(dep, SCOPE, scope);
		}
		if (classifier!=null) {
			createElementWithText(dep, CLASSIFIER, classifier);
		}
		format(dep);
		return dep;
	}

	/**
	 * creates and adds new dependency to the parent. formats the result.
	 */
	private Element createDependency(Element parentList, IMavenCoordinates info, String scope) {
		Element dep = createElement(parentList, DEPENDENCY);
		String groupId = info.getGroupId();
		String artifactId = info.getArtifactId();
		String version = info.getVersion();
		String classifier = info.getClassifier();

		if(groupId != null) {
			createElementWithText(dep, GROUP_ID, groupId);
		}
		createElementWithText(dep, ARTIFACT_ID, artifactId);
		if(version != null) {
			createElementWithText(dep, VERSION, version);
		}
		if (classifier != null) {
			createElementWithText(dep, CLASSIFIER, classifier);
		}
		if (scope!=null && !scope.equals("compile")) {
			createElementWithText(dep, SCOPE, scope);
		}
		format(dep);
		return dep;
	}

	private void addReposIfNeeded(Document pom, List<Repo> repos) {
		//Example:
		//	<repositories>
		//		<repository>
		//			<id>spring-snapshots</id>
		//			<name>Spring Snapshots</name>
		//			<url>https://repo.spring.io/snapshot</url>
		//			<snapshots>
		//				<enabled>true</enabled>
		//			</snapshots>
		//		</repository>
		//		<repository>
		//			<id>spring-milestones</id>
		//			<name>Spring Milestones</name>
		//			<url>https://repo.spring.io/milestone</url>
		//			<snapshots>
		//				<enabled>false</enabled>
		//			</snapshots>
		//		</repository>
		//	</repositories>

		if (repos!=null && !repos.isEmpty()) {
			Element doc = pom.getDocumentElement();
			Element repoList = findChild(doc, REPOSITORIES);
			if (repoList==null) {
				repoList = createElement(doc, REPOSITORIES);
				format(repoList);
			}
			for (Repo repo : repos) {
				String id = repo.getId();
				Element repoEl = findChild(repoList, REPOSITORY, childEquals(ID, id));
				if (repoEl==null) {
					repoEl = createElement(repoList, REPOSITORY);
					createElementWithTextMaybe(repoEl, ID, id);
					createElementWithTextMaybe(repoEl, NAME, repo.getName());
					createElementWithTextMaybe(repoEl, URL, repo.getUrl());
					Boolean isSnapshot = repo.getSnapshotEnabled();
					if (isSnapshot!=null) {
						Element snapshot = createElement(repoEl, SNAPSHOTS);
						createElementWithText(snapshot, ENABLED, isSnapshot.toString());
					}
					format(repoEl);
				}
			}
		}
	}

	private void createElementWithTextMaybe(Element parent, String name, String text) {
		if (StringUtils.hasText(text)) {
			createElementWithText(parent, name, text);
		}
	}

}
