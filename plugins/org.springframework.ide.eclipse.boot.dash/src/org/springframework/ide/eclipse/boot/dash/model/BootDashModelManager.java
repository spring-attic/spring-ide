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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * An instance of this class manages a LiveSet of BootDashModels, one model per RunTarget.
 * New models are created or disposed to keep them in synch with the RunTargets.
 *
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public class BootDashModelManager implements Disposable {

	private LiveSet<BootDashModel> models;
	private Map<RunTarget, BootDashModel> asMap;
	private LiveExpression<Set<RunTarget>> targets;
	private RunTargetChangeListener targetListener;
	private ListenerList elementStateListeners = new ListenerList();
	private ElementStateListener upstreamElementStateListener;
	private BootDashModelContext context;

	public BootDashModelManager(BootDashModelContext context, LiveExpression<Set<RunTarget>> targets) {
		this.context = context;
		this.targets = targets;
	}

	public LiveExpression<Set<BootDashModel>> getModels() {
		if (models == null) {
			models = new LiveSet<BootDashModel>();
			asMap = new HashMap<RunTarget, BootDashModel>();
			models.dependsOn(targets);
			targets.addListener(targetListener = new RunTargetChangeListener());
		}
		return models;
	}

	class RunTargetChangeListener implements ValueListener<Set<RunTarget>> {

		@Override
		public void gotValue(LiveExpression<Set<RunTarget>> exp, Set<RunTarget> value) {

			if (value != null && !value.isEmpty()) {
				for (RunTarget target : value) {
					if (!asMap.containsKey(target)) {
						BootDashModel model = target.createElementsTabelModel(context);
						if (model != null) {
							asMap.put(target, model);
							models.add(model);
						}
					}
				}
			}
		}
	}

	public void dispose() {
		if (targetListener!=null) {
			targets.removeListener(targetListener);
			targetListener = null;
		}
		if (models!=null) {
			for (BootDashModel m : models.getValues()) {
				if (upstreamElementStateListener!=null) {
					m.removeElementStateListener(upstreamElementStateListener);
				}
				m.dispose();
			}
			upstreamElementStateListener = null;
			models = null;
		}
	}

	public void addElementStateListener(ElementStateListener l) {
		elementStateListeners.add(l);
		ensureUpstreamStateListener();
	}

	private synchronized void ensureUpstreamStateListener() {
		if (upstreamElementStateListener==null) {
			upstreamElementStateListener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					for (Object o : elementStateListeners.getListeners()) {
						((ElementStateListener)o).stateChanged(e);
					} ;
				}
			};
			getModels().addListener(new ValueListener<Set<BootDashModel>>() {
				public void gotValue(LiveExpression<Set<BootDashModel>> exp, Set<BootDashModel> models) {
					for (BootDashModel m : models) {
						m.addElementStateListener(upstreamElementStateListener);
					}
				}
			});
		}
	}

	public void removeElementStateListener(ElementStateListener l) {
		elementStateListeners.remove(l);
	}

}
