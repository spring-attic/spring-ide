/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.DependencyGroup;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * @author Kris De Volder
 */
public class EditStartersModel implements OkButtonHandler {

	public static final Object JOB_FAMILY = "EditStartersModel.JOB_FAMILY";

	private final ISpringBootProject project;
	private final PopularityTracker popularities;
	private final DefaultDependencies defaultDependencies;

	/**
	 * Will be used to remember the set of initially selected dependencies (i.e. those that are already
	 * present in the project when the dialog is opened.
	 */
	private final List<Dependency> initialDependencies = new ArrayList<>();

	public final HierarchicalMultiSelectionFieldModel<Dependency> dependencies = new HierarchicalMultiSelectionFieldModel<Dependency>(Dependency.class, "dependencies")
			.label("Dependencies:");

	private HashSet<MavenId> activeStarters;
	private SpringBootStarters starters;

	public EditStartersModel(IProject selectedProject) throws Exception {
		this(
				selectedProject,
				SpringBootCore.getDefault(),
				BootWizardActivator.getDefault().getPreferenceStore()
		);
	}

	public boolean isSupported() {
		String notSupportedMsg = getNotSupportedMessage();
		return notSupportedMsg == null;
	}

	public String getNotSupportedMessage() {
		if (starters==null) {
			String url = BootPreferences.getInitializrUrl();
			boolean isDefault = BootPreferences.getDefaultInitializrUrl().equals(url);
			return "Could not obtain starter dependencies information. This information is obtained by accessing the '"+ url + "' webservice. "
					+ (isDefault ? "Are you connected to the internet?" : "Initializr URL can be specified via Preferences (Spring -> Boot -> Initializr)");
		}
		return null;
	}

	/**
	 * Create EditStarters dialog model and initialize it based on a project selection.
	 */
	public EditStartersModel(IProject selectedProject, SpringBootCore springBootCore, IPreferenceStore store) throws Exception {
		this.popularities = new PopularityTracker(store);
		this.defaultDependencies = new DefaultDependencies(store);
		this.project = springBootCore.project(selectedProject);
		discoverOptions(dependencies);
	}

	public String getBootVersion() {
		return starters.getBootVersion();
	}


	public String getProjectName() {
		return project.getProject().getName();
	}

	public void performOk() {
		Job job = new Job("Modifying starters for "+getProjectName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					List<Dependency> selected = dependencies.getCurrentSelection();
					List<SpringBootStarter> selectedStarters = new ArrayList<>(selected.size());
					for (Dependency dep : selected) {
						String id = dep.getId();
						SpringBootStarter starter = starters.getStarter(id);
						if (starter!=null) {
							selectedStarters.add(starter);
						}
					}
					project.setStarters(selectedStarters);
					for (Dependency s : selected) {
						if (!initialDependencies.contains(s)) {
							popularities.incrementUsageCount(s);
						}
					}
					return Status.OK_STATUS;
				} catch (Exception e) {
					Log.log(e);
					return ExceptionUtil.status(e);
				}
			}
			@Override
			public boolean belongsTo(Object family) {
				return family==JOB_FAMILY;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
	}

	/**
	 * Dynamically discover input fields and 'style' options by parsing initializr form.
	 */
	private void discoverOptions(HierarchicalMultiSelectionFieldModel<Dependency> dependencies) throws Exception {
		starters = project.getStarterInfos();

		Set<MavenId> activeStarters = getActiveStarters();

		if (starters!=null) {
			for (DependencyGroup dgroup : starters.getDependencyGroups()) {
				String catName = dgroup.getName();
				for (Dependency dep : dgroup.getContent()) {
					if (starters.contains(dep.getId())) {
						dependencies.choice(catName, dep.getName(), dep, dep.getDescription(), LiveExpression.constant(true));
						MavenId mavenId = starters.getMavenId(dep.getId());
						boolean selected = activeStarters.contains(mavenId);
						if (selected) {
							initialDependencies.add(dep);
						}
						dependencies.setSelection(catName, dep, selected);
					}
				}
			}
		}
	}

	private Set<MavenId> getActiveStarters() throws Exception {
		if (this.activeStarters==null) {
			this.activeStarters = new HashSet<MavenId>();
			List<IMavenCoordinates> deps = project.getDependencies();
			if (deps!=null) {
				for (IMavenCoordinates coords : deps) {
					String gid = coords.getGroupId();
					String aid = coords.getArtifactId();
					if (aid!=null && gid!=null) {
						this.activeStarters.add(new MavenId(gid, aid));
					}
				}
			}
		}
		return activeStarters;
	}

	/**
	 * Retrieves the most popular dependencies based on the number of times they have
	 * been used to create a project. This similar to how it works in {@link NewSpringBootWizardModel}
	 * except that we add the initially selected elements regardless of their usage count and
	 * then use the usage count to backfill any remaining spots.
	 *
	 * @param howMany is an upper limit on the number of most popular items to be returned.
	 * @return An array of the most popular dependencies. May return fewer items than requested.
	 */
	public List<CheckBoxModel<Dependency>> getMostPopular(int howMany) {
		ArrayList<CheckBoxModel<Dependency>> result = new ArrayList<>();
		Set<Dependency> seen = new HashSet<>();
		for (CheckBoxModel<Dependency> cb : dependencies.getAllBoxes()) {
			if (cb.getSelection().getValue()) {
				if (seen.add(cb.getValue())) {
					result.add(cb);
				}
			}
		}
		if (result.size() < howMany) {
			//space for adding some not yet selected 'popular' selections
			List<CheckBoxModel<Dependency>> popular = popularities.getMostPopular(dependencies, howMany);
			Iterator<CheckBoxModel<Dependency>> iter = popular.iterator();
			while (result.size() < howMany && iter.hasNext()) {
				CheckBoxModel<Dependency> cb = iter.next();
				if (seen.add(cb.getValue())) {
					result.add(cb);
				}
			}
		}
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Retrieves currently set default dependencies
	 * @return list of default dependencies check-box models
	 */
	public List<CheckBoxModel<Dependency>> getDefaultDependencies() {
		return defaultDependencies.getDependencies(dependencies);
	}
	
	/**
	 * Retrieves frequently used dependencies based on currently set default dependencies and the most popular dependencies
	 * 
	 * @param numberOfMostPopular max number of most popular dependencies
	 * @return list of frequently used dependencies
	 */
	public List<CheckBoxModel<Dependency>> getFrequentlyUsedDependencies(int numberOfMostPopular) {
		List<CheckBoxModel<Dependency>> dependencies = getDefaultDependencies();
		Set<String> defaultDependecyIds = defaultDependencies.getDependciesIdSet();
		getMostPopular(numberOfMostPopular).stream().filter(checkboxModel -> {
			return !defaultDependecyIds.contains(checkboxModel.getValue().getId());
		}).forEach(dependencies::add);
		// Sort alphabetically
		dependencies.sort(new Comparator<CheckBoxModel<Dependency>>() {
			@Override
			public int compare(CheckBoxModel<Dependency> d1, CheckBoxModel<Dependency> d2) {
				return d1.getLabel().compareTo(d2.getLabel());
			}
		});
		return dependencies;
	}


	/**
	 * Convenience method for easier scripting of the wizard model (used in testing). Not used
	 * by the UI itself. If the dependencyId isn't found in the wizard model then an IllegalArgumentException
	 * will be raised.
	 */
	public void removeDependency(String dependencyId) {
		for (String catName : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> cat = dependencies.getContents(catName);
			for (Dependency dep : cat.getChoices()) {
				if (dependencyId.equals(dep.getId())) {
					cat.unselect(dep);
					return; //dep found and unselected
				}
			}
		}
		throw new IllegalArgumentException("No such dependency: "+dependencyId);
	}

	/**
	 * Convenience method for easier scripting of the wizard model (used in testing). Not used
	 * by the UI itself. If the dependencyId isn't found in the wizard model then an IllegalArgumentException
	 * will be raised.
	 */
	public void addDependency(String dependencyId){
		for (String catName : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> cat = dependencies.getContents(catName);
			for (Dependency dep : cat.getChoices()) {
				if (dependencyId.equals(dep.getId())) {
					cat.select(dep);
					return; //dep found and added to selection
				}
			}
		}
		throw new IllegalArgumentException("No such dependency: "+dependencyId);
	}

}
