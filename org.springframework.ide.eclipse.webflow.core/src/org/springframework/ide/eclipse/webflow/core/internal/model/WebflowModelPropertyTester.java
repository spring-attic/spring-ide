/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * This <code>PropertyTester</code> is used to check properties of the
 * {@link IWebflowModel} in <code><test property="..."/></code> expressions.
 * <p>
 * Currently the following properties are supported:
 * <ul>
 * <li><strong>isWebflowConfig</strong> checks if a given <code>IFile</code>
 * is a {@link IWebflowConfig} file</li>
 * </ul>
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IFile && "isWebflowConfig".equals(property)) {
			IFile file = (IFile) receiver;
			IWebflowProject project = Activator.getModel().getProject(file.getProject());
			boolean isWebflowConfig = project.getConfig(file) != null;
			return expectedValue == null ? isWebflowConfig
					: isWebflowConfig == ((Boolean) expectedValue).booleanValue();
		}
		return false;
	}
}
