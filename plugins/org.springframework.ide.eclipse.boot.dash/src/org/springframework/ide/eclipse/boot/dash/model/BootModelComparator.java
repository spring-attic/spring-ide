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
import org.springframework.ide.eclipse.boot.dash.util.OrderBasedComparator;

public class BootModelComparator implements Comparator<BootDashModel> {

	private Comparator<RunTargetType> typeComparator;

	public BootModelComparator(Comparator<RunTargetType> typeComparator) {
		this.typeComparator = typeComparator;
	}

	public BootModelComparator(List<RunTargetType> runTargetTypes) {
		this(new OrderBasedComparator(runTargetTypes.toArray(new RunTargetType[runTargetTypes.size()])));
	}

	public int compare(BootDashModel model1, BootDashModel model2) {

		RunTargetType rtType1 = model1.getRunTarget().getType();
		RunTargetType rtType2 = model2.getRunTarget().getType();

		int result = typeComparator.compare(rtType1, rtType2);
		if (result==0) {
			result = model1.getRunTarget().getId().compareTo(model2.getRunTarget().getId());
		}
		return result;
	}
}
