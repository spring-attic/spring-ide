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
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;

/**
 * The base label provider class for the viewer in
 * {@link AbstractConfigMasterPart}. Clients are encouraged to use
 * {@link SpringConfigLabelProvider}.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class AbstractConfigLabelProvider implements ITableLabelProvider, ILabelProvider {

}
