/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugStrategyManager;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypeFactory;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.util.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.util.TreeAwareFilter;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class BootDashViewModel extends AbstractDisposable {

	private JmxSshTunnelManager jmxSshTunnels = new JmxSshTunnelManager();
	private LiveSetVariable<RunTarget> runTargets;
	private BootDashModelManager models;
	private Set<RunTargetType> runTargetTypes;
	private RunTargetPropertiesManager manager;
	private ToggleFiltersModel toggleFiltersModel;
	private BootDashElementsFilterBoxModel filterBox;
	private LiveExpression<Filter<BootDashElement>> filter;
	private ProcessTracker devtoolsProcessTracker;
	private List<RunTargetType> orderedRunTargetTypes;
	private Comparator<BootDashModel> modelComparator;
	private Comparator<RunTarget> targetComparator;
	private BootDashModelContext context;

	private static List<RunTargetType> createRunTargetTypes(BootDashModelContext context) {
		SimpleDIContext injections = context.injections;
		ImmutableList.Builder<RunTargetType> rtTypes = ImmutableList.builder();
		rtTypes.add(RunTargetTypes.LOCAL);
		for (RunTargetTypeFactory f : injections.getBeans(RunTargetTypeFactory.class)) {
			rtTypes.add(f.create(context));
		}
		return rtTypes.build();
	}

	/**
	 * Create an 'empty' BootDashViewModel with no run targets. Targets can be
	 * added by adding them to the runTarget's LiveSet.
	 */
	public BootDashViewModel(SimpleDIContext injections)
	{
		context = injections.getBean(BootDashModelContext.class);
		List<RunTargetType> runTargetTypes = createRunTargetTypes(context);
		runTargets = new LiveSetVariable<>(new LinkedHashSet<RunTarget>(), AsyncMode.SYNC);
		models = new BootDashModelManager(context, this, runTargets);

		manager = new RunTargetPropertiesManager(context, runTargetTypes);
		List<RunTarget> existingtargets = manager.getStoredTargets();
		runTargets.addAll(existingtargets);
		runTargets.addListener(manager);

		this.orderedRunTargetTypes = runTargetTypes;
		this.targetComparator = new RunTargetComparator(orderedRunTargetTypes);
		this.modelComparator = new BootModelComparator(targetComparator);

		this.runTargetTypes = new LinkedHashSet<>(orderedRunTargetTypes);
		filterBox = new BootDashElementsFilterBoxModel();
		toggleFiltersModel = new ToggleFiltersModel(context);
		LiveExpression<Filter<BootDashElement>> baseFilter = filterBox.getFilter();
		LiveExpression<Filter<BootDashElement>> treeAwarefilter = baseFilter.apply(new Function<Filter<BootDashElement>, Filter<BootDashElement>>() {
			public Filter<BootDashElement> apply(Filter<BootDashElement> input) {
				return new TreeAwareFilter(input);
			}
		});
		filter = Filters.compose(treeAwarefilter, toggleFiltersModel.getFilter());
		addDisposableChild(filter);

		devtoolsProcessTracker = DevtoolsUtil.createProcessTracker(this);
	}

	public LiveSetVariable<RunTarget> getRunTargets() {
		return runTargets;
	}

	@Override
	public void dispose() {
		models.dispose();
		devtoolsProcessTracker.dispose();
		filterBox.dispose();
	}

	public void addElementStateListener(ElementStateListener l) {
		models.addElementStateListener(l);
	}

	public void removeElementStateListener(ElementStateListener l) {
		models.removeElementStateListener(l);
	}

	public LiveExpression<ImmutableSet<BootDashModel>> getSectionModels() {
		return models.getModels();
	}

	public Set<RunTargetType> getRunTargetTypes() {
		return runTargetTypes;
	}

	public void removeTarget(RunTarget toRemove, UserInteractions userInteraction) {
		if (toRemove != null) {
			String name = toRemove.getName();
			boolean found = runTargets.getValues().contains(toRemove);
			if (found && userInteraction.confirmOperation("Deleting run target: " + name,
					"Are you sure that you want to delete " + name
							+ "? All information regarding this target will be permanently removed.")) {
				runTargets.remove(toRemove);
				toRemove.dispose();
			}
		}
	}

	public void updateTargetPropertiesInStore() {
		manager.store(getRunTargets().getValue());
	}

	public ToggleFiltersModel getToggleFilters() {
		return toggleFiltersModel;
	}

	public BootDashElementsFilterBoxModel getFilterBox() {
		return filterBox;
	}

	public LiveExpression<Filter<BootDashElement>> getFilter() {
		return filter;
	}

	public RunTarget getRunTargetById(String targetId) {
		for (BootDashModel m : getSectionModels().getValue()) {
			RunTarget target = m.getRunTarget();
			if (target.getId().equals(targetId)) {
				return target;
			}
		};
		return null;
	}

	public BootDashModel getSectionByTargetId(String targetId) {
		for (BootDashModel m : getSectionModels().getValue()) {
			if (m.getRunTarget().getId().equals(targetId)) {
				return m;
			}
		};
		return null;
	}

	public Comparator<BootDashModel> getModelComparator() {
		return this.modelComparator;
	}

	public Comparator<RunTarget> getTargetComparator() {
		return this.targetComparator;
	}

	public BootDashModelContext getContext() {
		return context;
	}

	public JmxSshTunnelManager getJmxSshTunnelManager() {
		return jmxSshTunnels;
	}

	public SshTunnelFactory getSshTunnelFactory() {
		return context.getSshTunnelFactory();
	}

}
