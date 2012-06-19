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
package org.springframework.ide.eclipse.config.ui.editors.namespaces;

import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterDetailsBlock;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NamespacesMasterDetailsBlock extends AbstractConfigMasterDetailsBlock {

	public NamespacesMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite container) {
		return new NamespacesMasterPart(page, container);
	}

	@Override
	public AbstractConfigDetailsPart getPage(Object key) {
		if (key instanceof INamespaceDefinition) {
			return new NamespacesDetailsPart(getMasterPart());
		}
		return null;
	}

}
