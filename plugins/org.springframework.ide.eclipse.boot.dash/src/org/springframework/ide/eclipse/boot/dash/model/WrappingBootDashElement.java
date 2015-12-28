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
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSets;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.TypeLookup;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.DisposeListener;

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

	private BootDashModel bootDashModel;
	private TypeLookup typeLookup;
	private ListenerList disposeListeners = new ListenerList();

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
				return new LinkedHashSet<String>(Arrays.asList(tags));
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return new LinkedHashSet<String>();
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

	/**
	 * This default implementation only computes a 'fake' summary and is
	 * suitable only for nodes that do not have any children.
	 */
	@Override
	public ImmutableSet<String> getDefaultRequestMappingPaths() {
		String path = getDefaultRequestMappingPath();
		if (StringUtil.hasText(path)) {
			return ImmutableSet.of(path);
		}
		return ImmutableSet.of();
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
		try {
			URI target = getActuatorUrl();
			if (target!=null) {
				ActuatorClient client = new ActuatorClient(target, getTypeLookup());
				return client.getRequestMappings();
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	/**
	 * Subclass should override to determine where to access the actuator for an app.
	 * The Default implementation just returns null. Any functionality that depends on this
	 * should in turn be disabled / return null.
	 */
	protected URI getActuatorUrl() {
		return null;
	}
}
