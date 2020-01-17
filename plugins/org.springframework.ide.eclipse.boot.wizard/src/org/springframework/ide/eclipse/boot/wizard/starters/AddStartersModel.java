/*******************************************************************************
 * Copyright (c) 2015, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.DependencyGroup;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.DefaultDependencies;
import org.springframework.ide.eclipse.boot.wizard.DependencyFilterBox;
import org.springframework.ide.eclipse.boot.wizard.DependencyTooltipContent;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.MultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.boot.wizard.PopularityTracker;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 *
 */
public class AddStartersModel implements OkButtonHandler {

	public static final Object JOB_FAMILY = "EditStartersModel.JOB_FAMILY";

	public final DependencyFilterBox searchBox = new DependencyFilterBox();
	private final ISpringBootProject project;
	private final PopularityTracker popularities;
	private final DefaultDependencies defaultDependencies;

	public final HierarchicalMultiSelectionFieldModel<Dependency> dependencies = new HierarchicalMultiSelectionFieldModel<>(Dependency.class, "dependencies")
			.label("Dependencies:");

	private SpringBootStarters starters;

	protected final SpringBootCore springBootCore;

	private final LocalResource localResource;

	private CompareEditorInput compareInput;

	/**
	 * Create EditStarters dialog model and initialize it based on a project selection.
	 */
	public AddStartersModel(IProject selectedProject, SpringBootCore springBootCore, IPreferenceStore store) throws Exception {
		this.springBootCore = springBootCore;
		this.popularities = new PopularityTracker(store);
		this.defaultDependencies = new DefaultDependencies(store);
		this.project = springBootCore.project(selectedProject);

		// Left side is the local editable file
		// Right side is the read-only generated file
	    this.localResource = new LocalResource(project.getProject().getFile("pom.xml"));

		discoverOptions(dependencies);
	}

	public String getBootVersion() {
		return starters.getBootVersion();
	}


	public String getProjectName() {
		return project.getProject().getName();
	}

	@Override
	public void performOk() {

		Job job = new Job("Saving starter changes for "+getProjectName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					List<Dependency> selected = dependencies.getCurrentSelection();

					for (Dependency s : selected) {
						popularities.incrementUsageCount(s);
					}

					if (AddStartersModel.this.localResource != null) {
						AddStartersModel.this.localResource.commit(monitor);
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

		if (starters!=null) {
			for (DependencyGroup dgroup : starters.getDependencyGroups()) {
				String catName = dgroup.getName();

				// Setup template links variable values
				Map<String, String> variables = new HashMap<>();
				variables.put(InitializrServiceSpec.BOOT_VERSION_LINK_TEMPLATE_VARIABLE, starters.getBootVersion());

				//Create all dependency boxes
				for (Dependency dep : dgroup.getContent()) {
					if (starters.contains(dep.getId())) {
						dependencies.choice(catName, dep.getName(), dep,
								() -> DependencyTooltipContent.generateHtmlDocumentation(dep, variables),
								DependencyTooltipContent.generateRequirements(dep),
								LiveExpression.constant(true)
						);

						boolean selected = false;

						dependencies.setSelection(catName, dep, selected);
					}
				}
			}
		}
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

	public boolean saveDefaultDependencies() {
		return defaultDependencies.save(dependencies);
	}

	/**
	 * Retrieves frequently used dependencies.
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
//				System.out.println(dep.getId());
				if (dependencyId.equals(dep.getId())) {
					cat.select(dep);
					return; //dep found and added to selection
				}
			}
		}
		throw new IllegalArgumentException("No such dependency: "+dependencyId);
	}

	public ISpringBootProject getProject() {
		return project;
	}

	public boolean canShowDiffPage() {
		List<Dependency> currentSelection = this.dependencies.getCurrentSelection();
		return currentSelection != null && currentSelection.size() > 0;
	}

	public void onDependencyChange(Runnable runnable) {
		ValueListener<Boolean> selectionListener = (exp, val) -> {
			runnable.run();
		};

		for (String cat : dependencies.getCategories()) {
			MultiSelectionFieldModel<Dependency> dependencyGroup = dependencies.getContents(cat);
			dependencyGroup.addSelectionListener(selectionListener);
		}
	}

	public CompareEditorInput generateCompareInput() throws Exception {
		IProject project = getProject().getProject();

		ResourceNode editableLeft = this.localResource.getResourceNode();

		// Resource from Spring Initializr that is show in the right side of the compare view
		GeneratedResource right = new GeneratedResource("pom.xml", editableLeft.getImage(), getProject().generatePom(dependencies.getCurrentSelection()));

		CompareConfiguration config = new CompareConfiguration();
		config.setLeftLabel("Local file in " + project.getName() + ": " + editableLeft.getName());
		config.setLeftImage(editableLeft.getImage());
		config.setRightLabel("Spring Initializr: " + editableLeft.getName());
		config.setRightImage(right.getImage());
		config.setLeftEditable(true);

		this.compareInput = new CompareEditorInput(config) {
			@Override
			protected Object prepareInput(IProgressMonitor arg0)
					throws InvocationTargetException, InterruptedException {
				return new DiffNode(editableLeft, right);
			}
		};
		compareInput.setTitle("Merge Local File");

		new Job("Comparing project file with generated file from Spring Initializr.") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					compareInput.run(monitor);
					return Status.OK_STATUS;
				} catch (InvocationTargetException | InterruptedException e) {
					return ExceptionUtil.coreException(e).getStatus();
				}
			}

		}.schedule();

		return compareInput;
	}

	/**
	 * Wrapper around a Compare ResourceNode that commits (saves) content. The reason
	 * to have this wrapper is to hide internal implementation of ResourceNode like the BufferedResourceNode
	 *
	 */
	private class LocalResource {

		private final BufferedResourceNode resourceNode;

		private LocalResource(IFile file) {
			this.resourceNode = new BufferedResourceNode(file);
		}

		public void commit(IProgressMonitor monitor) throws Exception {
			this.resourceNode.commit(monitor);
		}

		public ResourceNode getResourceNode() {
			return this.resourceNode;
		}
	}

	private class GeneratedResource implements ITypedElement, IStreamContentAccessor {

		private String name;
		private Image image;
		private String content;

		public GeneratedResource(String name, Image image, String content) {
			super();
			this.name = name;
			this.image = image;
			this.content = content;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Image getImage() {
			return image;
		}

		@Override
		public String getType() {
			return "xml";
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content.getBytes());
		}
	}
}
