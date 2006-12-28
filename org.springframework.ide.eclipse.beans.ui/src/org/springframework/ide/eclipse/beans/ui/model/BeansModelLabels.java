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

package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This class provides labels for the beans core model's
 * {@link IModelElement elements}.
 * 
 * @author Torsten Juergeleit
 */
public final class BeansModelLabels extends BeansUILabels {

	public static String getElementLabel(IModelElement element, int flags) {
		StringBuffer buf = new StringBuffer(60);
		getElementLabel(element, flags, buf);
		return buf.toString();
	}

	public static String getElementLabel(IModelElement element, int flags,
			StringBuffer buf) {
		if (isFlagged(flags, PREPEND_PATH)) {
			appendPathLabel(element, flags, buf);
			buf.append(CONCAT_STRING);
		}
		if (element instanceof IBeansConfig) {
			appendBeansConfigLabel(element, flags, buf);
		} else {
			buf.append(element.getElementName());
		}
		if (isFlagged(flags, APPEND_PATH)) {
			buf.append(CONCAT_STRING);
			appendPathLabel(element, flags, buf);
		}
		return buf.toString();
	}

	protected static void appendBeansConfigLabel(IModelElement element,
			int flags, StringBuffer buf) {
		if (isFlagged(flags, DESCRIPTION)) {
			IBeansConfig config = (IBeansConfig) element;
			String configName = config.getElementName();
			if (config.isElementArchived()) {
				ZipEntryStorage storage = new ZipEntryStorage(config);
				buf.append(storage.getFullPath());
				buf.append(" - ");
				buf.append(storage.getZipResource().getName());
			} else {
				buf.append(new Path(configName).lastSegment());
			}
		} else {
			buf.append("beans");
		}
	}

	protected static void appendPathLabel(IModelElement element, int flags,
			StringBuffer buf) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			String path;
			if (element instanceof IBeansConfig
					&& isFlagged(flags, DESCRIPTION)) {
				path = resource.getProject().getName();
			} else {
				path = resource.getFullPath().makeRelative().toString();
			}
			buf.append(path);
		}
	}
}
