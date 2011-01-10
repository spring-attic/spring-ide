/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

/**
 * @author Christian Dupuis
 */
public class MethodReference {

	List<IAopReference> aspects = new ArrayList<IAopReference>();

	List<IAopReference> advices = new ArrayList<IAopReference>();

	IMember member;

	public List<IAopReference> getAdvices() {
		return advices;
	}

	public void setAdvices(List<IAopReference> advices) {
		this.advices = advices;
	}

	public List<IAopReference> getAspects() {
		return aspects;
	}

	public void setAspects(List<IAopReference> aspects) {
		this.aspects = aspects;
	}

	public IMember getMember() {
		return member;
	}

	public void setMember(IMember member) {
		this.member = member;
	}
}
