/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

public class NewSpringProjectWizardModel {
	public final LiveVariable<String> projectName = new LiveVariable<String>();

	public final LiveVariable<String> projectLocation = new LiveVariable<String>();

	public final LiveVariable<Template> selectedTemplate = new LiveVariable<Template>();

}
