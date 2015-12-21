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

/**
 * @author Terry Denny
 * @author Kaitlin Duck Sherwood
 */
package org.springframework.ide.eclipse.wizard.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateElement;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.TemplateCategory;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;


public class TemplateContentProvider implements ITreeContentProvider {

	public TemplateContentProvider() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public boolean hasChildren(Object element) {
		return element instanceof TemplateCategory && (((TemplateCategory) element).getChildren().size() > 0);
	}

	public Object getParent(Object element) {
		// return category if available
		return null;
	}

	public Object[] getElements(Object inputElement) {
		List<ITemplateElement> elements = new ArrayList<ITemplateElement>();

		List<Object> templates;
		Template template;

		if ((inputElement instanceof List)) {
			templates = (List<Object>) inputElement;

			// create categories as needed and sort into tree
			Map<String, TemplateCategory> pathToCategory = new HashMap<String, TemplateCategory>();
			for (Object obj : templates) {
				if ((obj instanceof Template)) {
					template = (Template) obj;

					Descriptor descriptor = template.getItem().getLocalDescriptor();
					if (descriptor == null) {
						descriptor = template.getItem().getRemoteDescriptor();
					}
					String categoryPath = descriptor.getCategory();
					if (categoryPath == null) {
						elements.add(template);
					}
					else {
						String[] categoryPaths = categoryPath.split("/");
						TemplateCategory lastCategory = null;
						String path = "";

						for (String categoryName : categoryPaths) {
							boolean topLevel;

							if (path.length() == 0) {
								path = categoryName;
								topLevel = true;
							}
							else {
								path += "/" + categoryName;
								topLevel = false;
							}
							TemplateCategory category = pathToCategory.get(path);
							if (category == null) {
								category = new TemplateCategory(categoryName);
								pathToCategory.put(path, category);

								if (topLevel) {
									elements.add(category);
								}
								else if (lastCategory != null) {
									lastCategory.addChild(category);
								}

							}

							lastCategory = category;
						}

						if (lastCategory != null) {
							lastCategory.addChild(template);
						}
					}
				}
			}
		}
		return elements.toArray();

	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TemplateCategory) {
			return ((TemplateCategory) parentElement).getChildren().toArray();
		}
		return null;
	}

}
