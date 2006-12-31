/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.beans.ui.navigator.Activator;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * This class is a content provider for the {@link CommonNavigator} which knows
 * about the beans core model's {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansNavigatorContentProvider extends BeansModelContentProvider
		implements ICommonContentProvider {

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID =
			Activator.PLUGIN_ID + ".projectExplorerContent";
	public static final String BEANS_EXPLORER_CONTENT_PROVIDER_ID =
		Activator.PLUGIN_ID + ".beansExplorerContent";

	private String providerID;

    public void elementChanged(ModelChangeEvent event) {
		IModelElement element = event.getElement();

		if (element instanceof IBeansProject) {
			if (providerID.equals(PROJECT_EXPLORER_CONTENT_PROVIDER_ID)) {
				refreshViewerForElement(((IBeansProject) element)
						.getElementResource());
			} else {
				super.elementChanged(event);
			}
		} else if (element instanceof IBeansConfig) {
			IBeansConfig config = (IBeansConfig) element;
			refreshViewerForElement(config.getElementResource());

			// For a changed Spring beans config in the Eclipse Project Explorer
			// refresh all corresponding bean classes
			if (providerID.equals(PROJECT_EXPLORER_CONTENT_PROVIDER_ID)) {
				refreshBeanClasses(config);
			}
		} else {
			super.elementChanged(event);
		}
	}

    /**
	 * Refreshes the config file and all bean classes of a given beans config
	 */
	protected void refreshBeanClasses(IBeansConfig config) {
		Set<String> classes = config.getBeanClasses();
		for (String clazz : classes) {
			IType type = BeansModelUtils.getJavaType(config
					.getElementResource().getProject(), clazz);
			if (type != null) {
				refreshViewerForElement(type);
			}
		}
	}

	public void init(ICommonContentExtensionSite config) {
		providerID = config.getExtension().getId();
	}

	public void saveState(IMemento aMemento) {
	}

	public void restoreState(IMemento aMemento) {
	}

	public String toString() {
		return String.valueOf(providerID);
	}
}
