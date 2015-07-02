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

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class RunTargetBootDashModel {

	private LiveSet<BootDashModel> models;
	private Map<RunTarget, BootDashModel> asMap;
	private BootDashModelFactory factory;
	private LiveExpression<Set<RunTarget>> targets;

	public RunTargetBootDashModel(BootDashModelContext context, LiveExpression<Set<RunTarget>> targets) {
		this.targets = targets;
	}

	public LiveSet<BootDashModel> getModels() {
		if (models == null) {
			models = new LiveSet<BootDashModel>();
			factory = new BootDashModelFactory();
			asMap = new HashMap<RunTarget, BootDashModel>();
			models.dependsOn(RunTargets.getTargets());
			targets.addListener(new RunTargetChangeListener());
		}
		return models;
	}

	class RunTargetChangeListener implements ValueListener<Set<RunTarget>> {

		@Override
		public void gotValue(LiveExpression<Set<RunTarget>> exp, Set<RunTarget> value) {

			if (value != null && !value.isEmpty()) {
				for (RunTarget target : value) {
					if (!asMap.containsKey(target)) {
						BootDashModel model = factory.getModel(target);
						if (model != null) {
							asMap.put(target, model);
							models.add(model);
						}
					}
				}
			}
		}
	}

}
