/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.util.ArrayList;
import java.util.List;

public class TemplateCategory implements ITemplateElement {

	private final String name;

	private final List<ITemplateElement> children;

	public TemplateCategory(String name) {
		this.name = name;
		this.children = new ArrayList<ITemplateElement>();
	}

	public String getName() {
		return name;
	}

	public void addChild(ITemplateElement element) {
		this.children.add(element);
	}

	public List<ITemplateElement> getChildren() {
		return this.children;
	}

}
