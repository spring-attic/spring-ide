/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor;

import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BatchMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public static final String DOCS_SPRINGBATCH = "http://static.springsource.org/spring-batch/reference/html/index.html"; //$NON-NLS-1$ 

	public BatchMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new BatchMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		return new BatchDetailsPart(getMasterPart(), DOCS_SPRINGBATCH);
	}

}
