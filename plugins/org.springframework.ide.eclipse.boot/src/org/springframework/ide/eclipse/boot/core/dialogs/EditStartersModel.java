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
package org.springframework.ide.eclipse.boot.core.dialogs;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.HierarchicalMultiSelectionFieldModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.MultiSelectionFieldModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.PopularityTracker;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.DependencyGroup;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;

public class EditStartersModel implements OkButtonHandler {

	public static final Object JOB_FAMILY = "EditStartersModel.JOB_FAMILY";

	private final ISpringBootProject project;
	private final PopularityTracker popularities;

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
				WizardPlugin.getDefault().getPreferenceStore()
		);
	}

	/**
	 * Create EditStarters dialog model and initialize it based on a project selection.
	 */
	public EditStartersModel(IProject selectedProject, SpringBootCore springBootCore, IPreferenceStore store) throws Exception {
		this.popularities = new PopularityTracker(store);
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
					BootActivator.log(e);
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
