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
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertiesMapper;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

public class RunTargetPropertiesManager implements ValueListener<Set<RunTarget>> {

	private final IScopedPropertyStore<RunTargetType> propertiesStore;
	private final SecuredCredentialsStore credentialsStore;
	private final RunTargetType[] types;

	public static final String RUN_TARGET_KEY = "runTargets";

	public RunTargetPropertiesManager(BootDashModelContext context, RunTargetType[] types) {
		this.propertiesStore = context.getRunTargetProperties();
		this.credentialsStore = context.getSecuredCredentialsStore();
		this.types = types;
	}

	public List<RunTarget> getStoredTargets() {

		List<RunTarget> targets = new ArrayList<RunTarget>();
		PropertiesMapper<List<Map<String, String>>> mapper = new PropertiesMapper<List<Map<String, String>>>();
		for (RunTargetType type : types) {
			if (type==RunTargetTypes.LOCAL) {
				targets.add(RunTargets.LOCAL);
			} else if (type.canInstantiate()) {
				String runTypesVal = propertiesStore.get(type, RUN_TARGET_KEY);
				if (runTypesVal != null) {
					List<Map<String, String>> asList = mapper.convert(runTypesVal);
					if (asList != null) {
						for (Map<String, String> runTargetPropMap : asList) {
							TargetProperties targProps = new TargetProperties(runTargetPropMap, type);

							// Load the password from secure storage
							String password = credentialsStore
									.getPassword(runTargetPropMap.get(TargetProperties.RUN_TARGET_ID));
							targProps.put(TargetProperties.PASSWORD_PROP, password);

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
	public synchronized void gotValue(LiveExpression<Set<RunTarget>> exp, Set<RunTarget> value) {
		Map<RunTargetType, List<RunTargetWithProperties>> propertiesToPersist = new HashMap<RunTargetType, List<RunTargetWithProperties>>();

		// Only persist run target properties that can be instantiated
		for (RunTargetType type : types) {
			if (type.canInstantiate()) {
				propertiesToPersist.put(type, new ArrayList<RunTargetWithProperties>());
			}
		}

		// Update the map of properties to persist based on the actual existing
		// set of runtargets
		if (value != null) {

			for (RunTarget target : value) {

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
				secureStorage(targProps);
			}
			try {
				String serialisedVal = mapper.convertToString(asStringMap);
				if (serialisedVal != null) {
					propertiesStore.put(entry.getKey(), RUN_TARGET_KEY, serialisedVal);
				}
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}
	}

	public synchronized void secureStorage(TargetProperties targProps) {
		// Only support changing passwords for now as its the only thing that
		// requires secure storage. Other target properties should be immutable
		if (targProps.getPassword() != null && targProps.getRunTargetId() != null) {
			credentialsStore.setPassword(targProps.getPassword(), targProps.getRunTargetId());
		}
	}

}
