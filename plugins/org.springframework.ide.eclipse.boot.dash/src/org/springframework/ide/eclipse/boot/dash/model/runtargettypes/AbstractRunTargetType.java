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
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

/**
 * @author Kris De Volder
 */
public abstract class AbstractRunTargetType implements RunTargetType {

	private String name;
	private int sortingOrder;

	public AbstractRunTargetType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RunTargetType("+getName()+")";
	}

	protected int getSortingOrder() {
		if (sortingOrder==0) {
			int idx = 1;
			for (RunTargetType t : getAllTypesInSortingOrder()) {
				if (t==this) {
					sortingOrder = idx;
					break;
				}
				idx++;
			}
		}
		if (sortingOrder==0) {
			throw new IllegalStateException("Bug: Sorting order for "+this+" is not defined. "
					+ "It should be added to 'RunTargetTypes.ALL' ?");
		}
		return sortingOrder;
	}

	protected RunTargetType[] getAllTypesInSortingOrder() {
		return RunTargetTypes.ALL;
	}

	@Override
	public int compareTo(RunTargetType other) {
		if (other instanceof AbstractRunTargetType) {
			return ((AbstractRunTargetType)other).getSortingOrder() - this.getSortingOrder();
		} else {
			//someone didn't use AbstractRunTargetType as base class its up to them to implement comparing then.
			return -(other.compareTo(this));
		}
	}

}
