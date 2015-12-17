/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * Concrete BootDashElement that wraps an IProject
 *
 * @author Kris De Volder
 */
public class BootProjectDashElement extends AbstractLaunchConfigurationsDashElement<IProject> {

	private IScopedPropertyStore<IProject> projectProperties;

	private LaunchConfDashElementFactory childFactory;
	private ObservableSet<BootDashElement> rawChildren;
	private ObservableSet<BootDashElement> children;

	public BootProjectDashElement(IProject project, LocalBootDashModel context, IScopedPropertyStore<IProject> projectProperties,
			BootProjectDashElementFactory factory, LaunchConfDashElementFactory childFactory) {
		super(context, project);
		this.projectProperties = projectProperties;
		this.childFactory = childFactory;
	}

	@Override
	public IProject getProject() {
		return delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	protected IPropertyStore createPropertyStore() {
		return PropertyStoreFactory.createForScope(delegate, projectProperties);
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		return ImmutableSet.copyOf(BootLaunchConfigurationDelegate.getLaunchConfigs(delegate));
	}

	@Override
	public ILaunchConfiguration getPreferredConfig() {
		return getBootDashModel().getPreferredConfig(this);
	}

	@Override
	public void setPreferredConfig(ILaunchConfiguration config) {
		getBootDashModel().setPreferredConfig(this, config);
	}

	@Override
	public ObservableSet<BootDashElement> getChildren() {
		if (this.children==null) {
			final ObservableSet<BootDashElement> all = getAllChildren();
			children = new ObservableSet<BootDashElement>() {
				{
					dependsOn(all);
				}

				@Override
				protected ImmutableSet<BootDashElement> compute() {
					ImmutableSet<BootDashElement> elements = all.getValues();
					if (elements.size()>1) {
						return elements;
					} else {
						return ImmutableSet.of();
					}
				}
			};
			addDisposableChild(children);
		}
		return children;
	}

	/**
	 * All children including 'invisible ones' that may be hidden from the children returned
	 * by getChildren.
	 */
	public ObservableSet<BootDashElement> getAllChildren() {
		if (rawChildren==null) {
			rawChildren = LiveSets.map(getBootDashModel().launchConfTracker.getConfigs(delegate),
					new Function<ILaunchConfiguration, BootDashElement>() {
						public BootDashElement apply(ILaunchConfiguration input) {
							return childFactory.createOrGet(input);
						}
					}
			);
			rawChildren.addListener(new ValueListener<ImmutableSet<BootDashElement>>() {
				public void gotValue(LiveExpression<ImmutableSet<BootDashElement>> exp, ImmutableSet<BootDashElement> value) {
					getBootDashModel().notifyElementChanged(BootProjectDashElement.this);
				}
			});
			addDisposableChild(rawChildren);
		}
		return this.rawChildren;
	}

	@Override
	protected ImmutableSet<ILaunch> getLaunches() {
		return ImmutableSet.copyOf(BootLaunchUtils.getBootLaunches(getProject()));
	}

}
