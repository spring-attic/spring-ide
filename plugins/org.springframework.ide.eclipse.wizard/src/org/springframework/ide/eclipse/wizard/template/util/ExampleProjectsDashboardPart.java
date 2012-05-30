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
package org.springframework.ide.eclipse.wizard.template.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPart;


/**
 * @author Kaitlin Duck Sherwood
 */

// modified from TutorialDashboardPart
public class ExampleProjectsDashboardPart extends AbstractDashboardPart {

	private Composite composite;

	private Composite hyperlinksComposite;

	private IPreferenceChangeListener preferencesListener;

	@Override
	public Control createPartContent(Composite parent) {
		FormToolkit toolkit = getToolkit();
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setText(NLS.bind("Example Projects", null));

		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		section.setLayoutData(tableWrapData);

		TableWrapData tableWrapData2 = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		ScrolledPageBook pageBook = toolkit.createPageBook(section, SWT.V_SCROLL | SWT.WRAP);
		pageBook.setLayoutData(tableWrapData2);

		composite = toolkit.createComposite(pageBook, SWT.WRAP);
		composite.setLayout(new TableWrapLayout());

		section.setClient(pageBook);

		String explanatoryText = NLS.bind("These projects show how Spring features are used.  "
				+ "You can import these projects into your workspace by clicking on them.  "
				// FIXME: give a warning if they try to do Maven but don't have
				// Maven.
				+ "Note that if you try to import a project which needs Maven and you do not have Maven, "
				+ "it will download but not build.", null);
		Label explanatoryLabel = toolkit.createLabel(composite, explanatoryText, SWT.WRAP);

		TableWrapData tableWrapData3 = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		explanatoryLabel.setLayoutData(tableWrapData3);

		createLinkToPreferencesPage();

		createSampleLinks();

		pageBook.setContent(composite);

		this.preferencesListener = new IEclipsePreferences.IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				createSampleLinks();
			}
		};

		InstanceScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID).addPreferenceChangeListener(preferencesListener);
		DefaultScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID).addPreferenceChangeListener(preferencesListener);

		return section;
	}

	private Map<String, URI> getExampleLocations() {

		Map<String, URI> sampleProjects = new HashMap<String, URI>();

		ExampleProjectsPreferenceModel model = ExampleProjectsPreferenceModel.getInstance();
		ArrayList<NameUrlPair> elements = model.getElements();
		for (NameUrlPair nameUrlPair : elements) {
			try {
				sampleProjects.put(nameUrlPair.getName(), nameUrlPair.getUrl().toURI());
			}
			catch (URISyntaxException e) {
				// This shouldn't happen -- errors should get caught before
				// the URI makes it into the preferences store. Ignore and
				// carry on.
			}
		}

		return sampleProjects;

	}

	private void createLinkToPreferencesPage() {
		FormToolkit toolkit = getToolkit();

		Hyperlink configureLink = toolkit.createHyperlink(composite, "Edit list of example projects", SWT.WRAP);
		configureLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getControl().getShell(),
						ExampleProjectsPreferencePage.EXAMPLE_PREFERENCES_PAGE_ID, null, null);
				dialog.open();
			}
		});

	}

	private void createSampleLinks() {
		FormToolkit toolkit = getToolkit();
		if (composite.isDisposed()) {
			return;
		}
		if (hyperlinksComposite == null) {
			hyperlinksComposite = toolkit.createComposite(composite, SWT.WRAP);
			TableWrapLayout layout = new TableWrapLayout();
			hyperlinksComposite.setLayout(layout);
		}
		else {
			// clear the old links out
			Menu m = hyperlinksComposite.getMenu();
			setMenu(hyperlinksComposite, null);
			for (Control childHyperlink : hyperlinksComposite.getChildren()) {
				childHyperlink.dispose();
			}
			setMenu(hyperlinksComposite, m);
		}

		Map<String, URI> sampleProjects = getExampleLocations();

		Object sortedArray[] = sampleProjects.keySet().toArray();
		if (sortedArray.length > 0) {

			Arrays.sort(sortedArray);
			for (final Object key : sortedArray) {
				if (key instanceof String) {
					Composite slot = toolkit.createComposite(hyperlinksComposite, SWT.WRAP);
					TableWrapLayout twl = new TableWrapLayout();
					twl.numColumns = 2;
					slot.setLayout(twl);

					String keyString = (String) key;
					ImageHyperlink hyperlink = toolkit.createImageHyperlink(slot, SWT.WRAP);
					hyperlink.setText(keyString);
					hyperlink.setHref(sampleProjects.get(key));
					hyperlink.setImage(StsUiImages.getImage(StsUiImages.DOWNLOAD_OVERLAY));
					TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
					hyperlink.setLayoutData(tableWrapData);

					Label urlLabel = toolkit.createLabel(slot, sampleProjects.get(key).toString(), SWT.WRAP);
					RGB rgb = new RGB(150, 150, 150);
					Color color = new Color(null, rgb);
					urlLabel.setForeground(color);
					TableWrapData tableWrapData2 = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
					urlLabel.setLayoutData(tableWrapData2);

					color.dispose();

					hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
						@Override
						public void linkActivated(HyperlinkEvent link) {
							final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							if (link.getHref() instanceof URI) {
								URI uri = (URI) link.getHref();
								String projectName = link.getLabel();

								if (promptForDownload(shell, uri)) {
									ExampleProjectsImporterJob job = new ExampleProjectsImporterJob(uri, projectName,
											shell);
									job.schedule();
								}
							}
						}
					});
				}
			}
		}

		if (composite.getParent() instanceof ScrolledPageBook) {
			ScrolledPageBook sc = ((ScrolledPageBook) composite.getParent());
			sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			sc.layout(true, true);
		}
		composite.layout(true, true);
	}

	public static void setMenu(Composite composite, Menu menu) {
		if (!composite.isDisposed()) {
			composite.setMenu(menu);
			for (Control child : composite.getChildren()) {
				child.setMenu(menu);
				if (child instanceof Composite) {
					setMenu((Composite) child, menu);
				}
			}
		}
	}

	protected static boolean promptForDownload(Shell shell, URI aURI) {
		String message = "This operation will download a project and import it into your workspace.  Do you want to import the project at {0} into your workspace?";
		return MessageDialog.openQuestion(shell, "Import", NLS.bind(message, aURI));
	}

	@Override
	public void refresh() {
		super.refresh();
		if (composite == null || composite.isDisposed()) {
			return;
		}
		composite.setRedraw(false);
		composite.layout(true, true);
		composite.setRedraw(true);
	}

}
