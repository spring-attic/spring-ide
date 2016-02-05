/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertiesMapper;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

public class RunTargetPropertiesManager implements ValueListener<ImmutableSet<RunTarget>> {

	private final BootDashModelContext context;
	private final RunTargetType[] types;

	public static final String RUN_TARGET_KEY = "runTargets";

	public RunTargetPropertiesManager(BootDashModelContext context, RunTargetType[] types) {
		this.context = context;
		this.types = types;
	}

	public List<RunTarget> getStoredTargets() {

		List<RunTarget> targets = new ArrayList<RunTarget>();
		PropertiesMapper<List<Map<String, String>>> mapper = new PropertiesMapper<List<Map<String, String>>>();
		for (RunTargetType type : types) {
			if (type==RunTargetTypes.LOCAL) {
				targets.add(RunTargets.LOCAL);
			} else if (type.canInstantiate()) {
				String runTypesVal = context.getRunTargetProperties().get(type, RUN_TARGET_KEY);
				if (runTypesVal != null) {
					List<Map<String, String>> asList = mapper.convert(runTypesVal);
					if (asList != null) {
						for (Map<String, String> runTargetPropMap : asList) {
							TargetProperties targProps = new TargetProperties(runTargetPropMap, type, context);
							RunTarget target = type.createRunTarget(targProps);
							if (target != null) {
								targets.add(target);
							}
						}
					}
				}
			}
		}

		return targets;
	}

	@Override
	public void gotValue(LiveExpression<ImmutableSet<RunTarget>> exp, ImmutableSet<RunTarget> value) {
		store(value);
	}

	public synchronized void store(Set<RunTarget> targets) {
		Map<RunTargetType, List<RunTargetWithProperties>> propertiesToPersist = new HashMap<RunTargetType, List<RunTargetWithProperties>>();

		// Only persist run target properties that can be instantiated
		for (RunTargetType type : types) {
			if (type.canInstantiate()) {
				propertiesToPersist.put(type, new ArrayList<RunTargetWithProperties>());
			}
		}

		// Update the map of properties to persist based on the actual existing
		// set of runtargets
		if (targets != null) {

			for (RunTarget target : targets) {

				if (target instanceof RunTargetWithProperties) {
					RunTargetWithProperties targetsWithProps = (RunTargetWithProperties) target;
					TargetProperties targetProperties = targetsWithProps.getTargetProperties();
					RunTargetType type = targetProperties.getRunTargetType();
					List<RunTargetWithProperties> listMaps = propertiesToPersist.get(type);
					if (listMaps != null) {
						listMaps.add(targetsWithProps);
					}
				}
			}
		}

		// Persist the properties, and if necessary, any passwords
		PropertiesMapper<List<Map<String, String>>> mapper = new PropertiesMapper<List<Map<String, String>>>();
		for (Entry<RunTargetType, List<RunTargetWithProperties>> entry : propertiesToPersist.entrySet()) {

			List<Map<String, String>> asStringMap = new ArrayList<Map<String, String>>();
			for (RunTargetWithProperties storedProp : entry.getValue()) {
				TargetProperties targProps = storedProp.getTargetProperties();
				asStringMap.add(targProps.getPropertiesToPersist());
			}
			try {
				String serialisedVal = mapper.convertToString(asStringMap);
				if (serialisedVal != null) {
					context.getRunTargetProperties().put(entry.getKey(), RUN_TARGET_KEY, serialisedVal);
				}
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}
	}

}
