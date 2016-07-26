/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.TypeLookup;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.dash.util.Utils;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.web.client.RestTemplate;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Abstract base class that is convenient to implement {@link BootDashElement}.
 * @author Kris De Volder
 */
public abstract class WrappingBootDashElement<T> extends AbstractDisposable implements BootDashElement {

	public static final String TAGS_KEY = "tags";

	private static final String DEFAULT_RM_PATH_KEY = "default.request-mapping.path";
	public static final String DEFAULT_RM_PATH_DEFAULT = "/";

	protected final T delegate;

	private CancelationTokens cancelationTokens = new CancelationTokens();

	private BootDashModel bootDashModel;
	private TypeLookup typeLookup;
	private ListenerList disposeListeners = new ListenerList();

	private LiveExpression<ImmutableList<RequestMapping>> liveRequestMappings;

	@SuppressWarnings("rawtypes")
	private ValueListener elementStateNotifier = new ValueListener() {
		public void gotValue(LiveExpression exp, Object value) {
			getBootDashModel().notifyElementChanged(WrappingBootDashElement.this);
		}
	};

	private ValueListener<?> elementNotifier;

	@Override
	public BootDashColumn[] getColumns() {
		return getTarget().getDefaultColumns();
	}

	public WrappingBootDashElement(BootDashModel bootDashModel, T delegate) {
		this.bootDashModel = bootDashModel;
		this.delegate = delegate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((delegate == null) ? 0 : delegate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		WrappingBootDashElement other = (WrappingBootDashElement) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return delegate.toString();
	}

	protected TypeLookup getTypeLookup() {
		if (typeLookup==null) {
			typeLookup = new TypeLookup() {
				public IType findType(String fqName) {
					try {
						IJavaProject jp = getJavaProject();
						if (jp!=null) {
							return jp.findType(fqName, new NullProgressMonitor());
						}
					} catch (Exception e) {
						BootDashActivator.log(e);
					}
					return null;
				}
			};
		}
		return typeLookup;
	}

	public abstract PropertyStoreApi getPersistentProperties();

	@Override
	public LinkedHashSet<String> getTags() {
		try {
			String[] tags = getPersistentProperties().get(TAGS_KEY, (String[])null);
			if (tags!=null) {
				return new LinkedHashSet<>(Arrays.asList(tags));
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return new LinkedHashSet<>();
	}

	@Override
	public void setTags(LinkedHashSet<String> newTags) {
		try {
			if (newTags==null || newTags.isEmpty()) {
				getPersistentProperties().put(TAGS_KEY, (String[])null);
			} else {
				getPersistentProperties().put(TAGS_KEY, newTags.toArray(new String[newTags.size()]));
			}
			bootDashModel.notifyElementChanged(this);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	public final String getDefaultRequestMappingPath() {
		String storedValue = getPersistentProperties().get(DEFAULT_RM_PATH_KEY);
		if (storedValue!=null) {
			return storedValue;
		}
		//inherit a default value from parent node?
		Object parent = getParent();
		if (parent instanceof BootDashElement) {
			String inheritedValue = ((BootDashElement) parent).getDefaultRequestMappingPath();
			return inheritedValue;
		}
		return null;
	}

	@Override
	public final void setDefaultRequestMappingPath(String defaultPath) {
		try {
			getPersistentProperties().put(DEFAULT_RM_PATH_KEY, defaultPath);
			bootDashModel.notifyElementChanged(this);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

	@Override
	public boolean hasDevtools() {
		return false;
	}

	public BootDashModel getBootDashModel() {
		return bootDashModel;
	}

	@Override
	public IJavaProject getJavaProject() {
		return getProject() != null ? JavaCore.create(getProject()) : null;
	}

	@Override
	public ObservableSet<BootDashElement> getChildren() {
		return LiveSets.emptySet(BootDashElement.class);
	}

	@Override
	public ImmutableSet<BootDashElement> getCurrentChildren() {
		return getChildren().getValue();
	}

	@Override
	public void onDispose(DisposeListener listener) {
		this.disposeListeners.add(listener);
	}

	@Override
	public void dispose() {
		for (Object l : disposeListeners.getListeners()) {
			((DisposeListener)l).disposed(this);
		}
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		//Default implementation for BDEs that do not have any relation to launch configs
		//Subclass should override when elements relate to launch configs.
		return ImmutableSet.of();
	}

	/**
	 * Gets a summary of the 'livePorts' for this node and its children. This default implementation
	 * is provided for nodes that only have a single port. Nodes that need to compute an actual
	 * summary should override this.
	 */
	@Override
	public ImmutableSet<Integer> getLivePorts() {
		int port = getLivePort();
		if (port>0) {
			return ImmutableSet.of(port);
		} else {
			return ImmutableSet.of();
		}
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		final LiveExpression<URI> actuatorUrl = getActuatorUrl();
		synchronized (this) {
			if (liveRequestMappings==null) {
				liveRequestMappings = new AsyncLiveExpression<ImmutableList<RequestMapping>>(null, "Fetch request mappings for '"+getName()+"'") {
					protected ImmutableList<RequestMapping> compute() {
						URI target = actuatorUrl.getValue();
						if (target!=null) {
							ActuatorClient client = new ActuatorClient(target, getTypeLookup(), getRestTemplate());
							List<RequestMapping> list = client.getRequestMappings();
							if (list!=null) {
								return ImmutableList.copyOf(client.getRequestMappings());
							}
						}
						return null;
					}

				};
				liveRequestMappings.dependsOn(actuatorUrl);
				addElementState(liveRequestMappings);
				addDisposableChild(liveRequestMappings);
			}
		}
		return liveRequestMappings.getValue();
	}

	protected RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	/**
	 * Ensure that element state notifications are fired when a given liveExp's value changes.
	 */
	@SuppressWarnings("unchecked")
	private void addElementState(LiveExpression<?> state) {
		state.addListener(elementStateNotifier);
	}

	/**
	 * Subclass should override to determine where to access the actuator for an app.
	 * The Default implementation just returns null. Any functionality that depends on this
	 * should in turn be disabled / return null.
	 */
	protected LiveExpression<URI> getActuatorUrl() {
		return LiveExpression.constant(null);
	}

	@Override
	public String getUrl() {
		return Utils.createUrl(getLiveHost(), getLivePort(), getDefaultRequestMappingPath());
	}

	private synchronized ValueListener<?> getElementNotifier() {
		if (elementNotifier==null) {
			elementNotifier = new ValueListener<Object>() {
				@Override
				public void gotValue(LiveExpression<Object> exp, Object value) {
					getBootDashModel().notifyElementChanged(WrappingBootDashElement.this);
				}
			};
		}
		return elementNotifier;
	}

	/**
	 * Attach a listener to a given liveExp so that the model's 'notifyElementChanged' is called
	 * any time the liveExps value changes.
	 */
	@SuppressWarnings("unchecked")
	protected void addElementNotifier(LiveExpression<?> exp) {
		@SuppressWarnings("rawtypes")
		ValueListener notifier = getElementNotifier();
		exp.addListener(notifier);
	}

	public CancelationToken createCancelationToken() {
		return cancelationTokens.create();
	}

	public void cancelOperations() {
		cancelationTokens.cancelAll();
	}
}
