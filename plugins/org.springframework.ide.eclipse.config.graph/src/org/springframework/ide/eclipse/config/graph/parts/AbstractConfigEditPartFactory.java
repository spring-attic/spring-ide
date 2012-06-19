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
package org.springframework.ide.eclipse.config.graph.parts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 */
public abstract class AbstractConfigEditPartFactory implements EditPartFactory {

	private final AbstractConfigGraphicalEditor editor;

	public AbstractConfigEditPartFactory(AbstractConfigGraphicalEditor editor) {
		super();
		this.editor = editor;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = createEditPartFromModel(context, model);
		if (part != null) {
			return part;
		}
		else {
			for (IConfigurationElement config : editor.getAdapterDefinitions()) {
				try {
					Object obj = config
							.createExecutableExtension(PageAdaptersExtensionPointConstants.ATTR_EDITPART_FACTORY);
					if (obj instanceof EditPartFactory) {
						EditPartFactory factory = (EditPartFactory) obj;
						part = factory.createEditPart(context, model);
						if (part != null) {
							return part;
						}
					}
				}
				catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
							Messages.AbstractConfigEditPartFactory_ERROR_CREATING_GRAPH, e));
				}
			}
		}
		return null;
	}

	protected abstract EditPart createEditPartFromModel(EditPart context, Object model);

}
