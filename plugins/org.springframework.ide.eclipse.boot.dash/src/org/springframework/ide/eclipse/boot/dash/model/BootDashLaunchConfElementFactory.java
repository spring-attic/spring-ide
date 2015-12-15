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

import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.MapMaker;

/**
 * @author Kris De Volder
 */
public class BootDashLaunchConfElementFactory implements Disposable {

	private LocalBootDashModel model;

	private Map<ILaunchConfiguration, BootDashLaunchConfElement> cache;

	private ILaunchConfigurationListener listener;

	private ILaunchManager launchManager;

	public BootDashLaunchConfElementFactory(LocalBootDashModel bootDashModel, ILaunchManager lm) {
		this.cache = new MapMaker()
				.concurrencyLevel(1) //single thread only so don't waste space for 'connurrencyLevel' support
				.makeMap();
		this.model = bootDashModel;
		this.launchManager = lm;
		lm.addLaunchConfigurationListener(listener = new ILaunchConfigurationListener() {

			@Override
			public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
				disposed(configuration);
			}

			@Override
			public void launchConfigurationChanged(ILaunchConfiguration configuration) {
			}

			@Override
			public void launchConfigurationAdded(ILaunchConfiguration configuration) {
			}
		});
	}

	public synchronized BootDashLaunchConfElement createOrGet(ILaunchConfiguration c) {
		try {
			if (c!=null) {
				ILaunchConfigurationType type = c.getType();
				if (type!=null && BootLaunchConfigurationDelegate.TYPE_ID.equals(type.getIdentifier())) {
					BootDashLaunchConfElement el = cache.get(c);
					if (el==null) {
						cache.put(c, el = new BootDashLaunchConfElement(model, c));
					}
					return el;
				}
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	/**
	 * Should be called by client code when conf is no longer interesting. This is to
	 * avoid the cache this factory maintains to grow unboundedly as launch confs get
	 * created / deleted.
	 */
	private synchronized void disposed(ILaunchConfiguration conf) {
		Map<ILaunchConfiguration, BootDashLaunchConfElement> c = cache;
		if (c!=null) {
			c.remove(conf);
		}

	}

	@Override
	public void dispose() {
		cache = null;
		if (listener!=null) {
			launchManager.removeLaunchConfigurationListener(listener);
			listener = null;
			launchManager = null;
		}
	}

}
