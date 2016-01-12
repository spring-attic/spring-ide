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
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;

/**
 * Concrete BootDashElement that wraps an IProject
 *
 * @author Kris De Volder
 */
public class BootProjectDashElement extends AbstractLaunchConfigurationsDashElement<IProject> {

	private IScopedPropertyStore<IProject> projectProperties;

	private LaunchConfDashElementFactory childFactory;
	private ObservableSet<BootDashElement> children;
	private ObservableSet<Integer> ports;

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
		return getLaunchConfigsExp().getValues();
	}

	@Override
	public ImmutableSet<Integer> getLivePorts() {
		return getLivePortsExp().getValues();
	}

	private ObservableSet<Integer> getLivePortsExp() {
		if (ports==null) {
			ports = createSortedLiveSummary(new Function<BootDashElement, Integer>() {
				@Override public Integer apply(BootDashElement element) {
					int port = element.getLivePort();
					if (port>0) {
						return port;
					}
					return null;
				}
			});
			this.dependsOn(ports);
		}
		return ports;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void dependsOn(LiveExpression<?> liveProperty) {
		liveProperty.addListener(new ValueListener() {
			public void gotValue(LiveExpression exp, Object value) {
				getBootDashModel().notifyElementChanged(BootProjectDashElement.this);
			}
		});
	}

	/**
	 * Creates a ObservableSet that is a 'summary' of some property of the children of this node. The summary
	 * is a set of all the values of the given property on all the children. The set is sorted using the element's
	 * natural ordering.
	 */
	private <T extends Comparable<T>> ObservableSet<T> createSortedLiveSummary(final Function<BootDashElement, T> getter) {
		final ObservableSet<T> summary = new ObservableSet<T>() {
			@Override
			protected ImmutableSet<T> compute() {
				Builder<T> builder = ImmutableSortedSet.naturalOrder();
				add(builder, BootProjectDashElement.this);
				for (BootDashElement child : getCurrentChildren()) {
					add(builder, child);
				}
				return builder.build();
			}

			protected void add(Builder<T> builder, BootDashElement child) {
				T v = getter.apply(child);
				if (v!=null) {
					builder.add(v);
				}
			}
		};
		final ElementStateListener elementListener = new ElementStateListener() {
			public void stateChanged(BootDashElement e) {
				summary.refresh();
			}
		};
		getBootDashModel().addElementStateListener(elementListener);
		summary.onDispose(new DisposeListener() {
			public void disposed(Disposable disposed) {
				getBootDashModel().removeElementStateListener(elementListener);
			}
		});
		addDisposableChild(summary);
		return summary;
	}

	private ObservableSet<ILaunchConfiguration> getLaunchConfigsExp() {
		return getBootDashModel().launchConfTracker.getConfigs(delegate);
	}

	/**
	 * All children including 'invisible ones' that may be hidden from the children returned
	 * by getChildren.
	 */
	@Override
	public ObservableSet<BootDashElement> getChildren() {
		if (children==null) {
			children = LiveSets.map(getBootDashModel().launchConfTracker.getConfigs(delegate),
					new Function<ILaunchConfiguration, BootDashElement>() {
						public BootDashElement apply(ILaunchConfiguration input) {
							return childFactory.createOrGet(input);
						}
					}
			);
			children.addListener(new ValueListener<ImmutableSet<BootDashElement>>() {
				public void gotValue(LiveExpression<ImmutableSet<BootDashElement>> exp, ImmutableSet<BootDashElement> value) {
					getBootDashModel().notifyElementChanged(BootProjectDashElement.this);
				}
			});
			addDisposableChild(children);
		}
		return this.children;
	}

	@Override
	public ImmutableSet<ILaunch> getLaunches() {
		return ImmutableSet.copyOf(BootLaunchUtils.getLaunches(getLaunchConfigs()));
	}

	@Override
	public Object getParent() {
		return getBootDashModel();
	}

}
