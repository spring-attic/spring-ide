/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */

package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

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