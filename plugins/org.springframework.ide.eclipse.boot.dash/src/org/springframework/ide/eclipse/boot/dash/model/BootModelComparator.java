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

import java.util.Comparator;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;

public class BootModelComparator implements Comparator<BootDashModel> {

	private final List<RunTargetType> runTargetTypes;

	public BootModelComparator(List<RunTargetType> runTargetTypes) {
		this.runTargetTypes = runTargetTypes;
	}

	public int compare(BootDashModel model1, BootDashModel model2) {

		RunTargetType rtType1 = model1.getRunTarget().getType();
		RunTargetType rtType2 = model2.getRunTarget().getType();

		int runTargetTypeIndex1 = 0;
		int runTargetTypeIndex2 = 0;

		for (int i = 0; i < runTargetTypes.size(); i++) {
			RunTargetType fromList = runTargetTypes.get(i);
			if (rtType1.getName().equals(fromList.getName())) {
				runTargetTypeIndex1 = i;
			}
			// Note handle case where BOTH models have the same run target
			// type. Therefore even if model1 run target type is found, also
			// check
			// if model2 type matches
			// the current existing run target type in the list of run
			// target types
			if (rtType2.getName().equals(fromList.getName())) {
				runTargetTypeIndex2 = i;
			}
		}

		if (runTargetTypeIndex1 == runTargetTypeIndex2) {
			return model1.getRunTarget().compareTo(model2.getRunTarget());
		} else {
			return runTargetTypeIndex1 - runTargetTypeIndex2;
		}
	}
}
