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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.ConfigGraphPlugin;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class AbstractConfigPaletteFactory {

	private class AdapterComparator implements Comparator<PaletteDrawer> {

		public int compare(PaletteDrawer o1, PaletteDrawer o2) {
			return o1.getLabel().compareTo(o2.getLabel());
		}

	}

	// TODO: move to ConfigCoreUtils??
	public static Image createScaledImage(Image image, double scale) {
		int width = image.getBounds().width;
		int height = image.getBounds().height;
		return new Image(image.getDevice(), image.getImageData()
				.scaledTo((int) (width * scale), (int) (height * scale)));
	}

	private final AbstractConfigGraphicalEditor editor;

	private PaletteRoot root;

	private List<PaletteDrawer> adapterDrawers;

	public AbstractConfigPaletteFactory(AbstractConfigGraphicalEditor editor) {
		this.editor = editor;
		createPaletteRoot();
	}

	private List<PaletteDrawer> createAdapterDrawers() {
		adapterDrawers = new ArrayList<PaletteDrawer>();
		for (IConfigurationElement config : editor.getAdapterDefinitions()) {
			String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
			if (editor.getEditor().containsNamespaceUri(uri)) {
				try {
					Object obj = config
							.createExecutableExtension(PageAdaptersExtensionPointConstants.ATTR_PALETTE_FACTORY);
					String label = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_LABEL);
					if (obj instanceof IPaletteFactory) {
						IPaletteFactory factory = (IPaletteFactory) obj;
						PaletteDrawer drawer = factory.createPaletteDrawer(getDiagram(), uri);
						if (drawer != null) {
							drawer.setLabel(label);
							drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
							adapterDrawers.add(drawer);
						}
					}
				}
				catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, ConfigGraphPlugin.PLUGIN_ID,
							Messages.AbstractConfigPaletteFactory_ERROR_CREATING_PALETTE, e));
				}
			}
		}
		Collections.sort(adapterDrawers, new AdapterComparator());
		return adapterDrawers;
	}

	protected List<PaletteEntry> createCategories() {
		List<PaletteEntry> categories = new ArrayList<PaletteEntry>();
		categories.add(createControlGroup());
		categories.addAll(createComponentDrawers());
		categories.addAll(createAdapterDrawers());
		return categories;
	}

	protected abstract List<PaletteDrawer> createComponentDrawers();

	protected abstract List<PaletteEntry> createConnectionTools();

	protected PaletteContainer createControlGroup() {
		PaletteGroup group = new PaletteGroup(Messages.AbstractConfigPaletteFactory_CONTROL_GROUP_TITLE);
		List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

		ToolEntry tool = new SelectionToolEntry();
		entries.add(tool);
		root.setDefaultEntry(tool);
		// tool = new MarqueeToolEntry();
		// entries.add(tool);

		List<PaletteEntry> connections = createConnectionTools();
		if (connections != null && connections.size() > 0) {
			PaletteSeparator sep = new PaletteSeparator();
			sep.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
			entries.add(sep);
			entries.addAll(connections);
		}

		group.addAll(entries);
		return group;
	}

	private void createPaletteRoot() {
		root = new PaletteRoot();
		root.addAll(createCategories());
	}

	public AbstractConfigGraphDiagram getDiagram() {
		return editor.getDiagram();
	}

	public PaletteRoot getPaletteRoot() {
		return root;
	}

	public void updatePalette() {
		for (PaletteDrawer drawer : adapterDrawers) {
			root.remove(drawer);
		}
		root.addAll(createAdapterDrawers());
	}

}
