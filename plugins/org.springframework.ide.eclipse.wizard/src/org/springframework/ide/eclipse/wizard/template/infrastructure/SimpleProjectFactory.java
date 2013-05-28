/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import org.springframework.ide.eclipse.wizard.template.SimpleProject;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;

public class SimpleProjectFactory {

	public static final String SIMPLE_MAVEN_TEMPLATE_ID = "org.springframework.templates.simple.spring.maven";

	public static final String SIMPLE_WEB_MAVEN_TEMPLATE_ID = "org.springframework.templates.simple.spring.web.maven";

	public static final String SIMPLE_JAVA_TEMPLATE_ID = "org.springframework.templates.simple.java";

	public static SimpleProject getSimpleProject(ContentItem item) {
		String templateId = item.getId();
		if (SIMPLE_JAVA_TEMPLATE_ID.equals(templateId)) {
			return new SimpleProject(item, null, true);
		}
		else if (SIMPLE_MAVEN_TEMPLATE_ID.equals(templateId)) {
			return new SimpleProject(item, null, false);
		}
		else if (SIMPLE_WEB_MAVEN_TEMPLATE_ID.equals(templateId)) {
			return new SimpleProject(item, null, false);
		}
		return null;
	}

	public static boolean isSimpleProject(ContentItem item) {
		return item != null
				&& (item.getId().equals(SIMPLE_JAVA_TEMPLATE_ID) || item.getId().equals(SIMPLE_MAVEN_TEMPLATE_ID) || item
						.getId().equals(SIMPLE_WEB_MAVEN_TEMPLATE_ID));
	}

}
