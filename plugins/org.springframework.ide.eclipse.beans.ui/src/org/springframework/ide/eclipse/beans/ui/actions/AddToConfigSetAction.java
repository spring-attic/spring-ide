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
package org.springframework.ide.eclipse.beans.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesConfigSet;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;

/**
 * @author Leo Dos Santos
 */
public class AddToConfigSetAction extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator locator;
	
	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		if (locator != null) {
			ISelectionService service = (ISelectionService) locator.getService(ISelectionService.class);
			ISelection selection = service.getSelection();
			if (selection instanceof StructuredSelection) {
				Object element = ((StructuredSelection) selection).getFirstElement();
				if (element instanceof IBeansConfig) {
					IBeansConfig config = (IBeansConfig) element;
					IBeansProject beansProject = BeansModelUtils.getProject(config);
					Set<IBeansConfigSet> configSets = beansProject.getConfigSets();
					for (IBeansConfigSet configSet : configSets) {
						if (!configSet.hasConfig(config.getElementName())) {
							IContributionItem contribution = new ActionContributionItem(
									new AddToConfigSetDynamicAction(configSet, config));
							items.add(contribution);
						}
					}
				}
			}
		}
		return items.toArray(new IContributionItem[] {});
	}

	public void initialize(IServiceLocator serviceLocator) {
		this.locator = serviceLocator;
	}

	private class AddToConfigSetDynamicAction extends Action {
		
		private IBeansConfigSet configSet;
		
		private IBeansConfig config;
		
		private AddToConfigSetDynamicAction(IBeansConfigSet configSet, IBeansConfig config) {
			super(configSet.getElementName());
			this.configSet = configSet;
			this.config = config;
			setImageDescriptor(ImageDescriptor.createFromImage(BeansModelImages.getImage(configSet)));
		}

		@Override
		public void run() {
			IBeansProject beansProject = BeansModelUtils.getProject(config);
			if (beansProject != null) {
				PropertiesProject modelProject = new PropertiesProject(new PropertiesModel(), beansProject);
				PropertiesConfigSet propSet = (PropertiesConfigSet) modelProject.getConfigSet(configSet.getElementName());
				propSet.addConfig(config.getElementName());
				modelProject.saveDescription();
				BeansModelLabelDecorator.update();
			}
		}
		
	}
	
}
