/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.gettingstarted.guides;

import java.net.URI;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.dashboard.internal.ui.IdeUiPlugin;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPart;
//import org.springframework.ide.eclipse.wizard.WizardPlugin;

/**
 * @author Kris De Volder
 */
public class GettingStartedGuidesDashboardPart extends AbstractDashboardPart {

//	private static final String BROWSER_ID = GettingStartedGuidesDashboardPart.class.getName();
	
	private static final String SMALL_ARROW_IMAGE = "rss/overlay-incoming.png";
	
	private Composite composite;
	private Composite guidesComposite;

	@Override
	public Control createPartContent(Composite parent) {
		FormToolkit toolkit = getToolkit();
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setText(NLS.bind("Guides", null));

		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		section.setLayoutData(tableWrapData);

		TableWrapData tableWrapData2 = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		ScrolledPageBook pageBook = toolkit.createPageBook(section, SWT.V_SCROLL | SWT.WRAP);
		pageBook.setLayoutData(tableWrapData2);

		composite = toolkit.createComposite(pageBook, SWT.WRAP);
		composite.setLayout(new TableWrapLayout());

		section.setClient(pageBook);

		String explanatoryText = NLS.bind("A guide is a short focussed tutorial "
				+ "on how to use Spring to accomplish a specific task. " 
				+ "It has a 'start' code set, a 'complete' code" 
				+ "set and a readme file explaining how you get from "
				+ "one to the other.",
				null
		);
				
		Label explanatoryLabel = toolkit.createLabel(composite, explanatoryText, SWT.WRAP);

		TableWrapData tableWrapData3 = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		explanatoryLabel.setLayoutData(tableWrapData3);

		//createLinkToPreferencesPage();

		createGuides();

		pageBook.setContent(composite);

//		this.preferencesListener = new IEclipsePreferences.IPreferenceChangeListener() {
//			public void preferenceChange(PreferenceChangeEvent event) {
//				createSampleLinks();
//			}
//		};
//
//		InstanceScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID).addPreferenceChangeListener(preferencesListener);
//		DefaultScope.INSTANCE.getNode(WizardPlugin.PLUGIN_ID).addPreferenceChangeListener(preferencesListener);

		return section;
	}

//	/**
//	 * This is the method that gets called to import sample project into the
//	 * workspace when a sample project link is clicked. It has been separated
//	 * into a public static method to make it easily accessible to testing code.
//	 * 
//	 * @return Job that is performing the import. The job has already been
//	 * Scheduled. Testing code may want to wait for the Job to finish before
//	 * proceeding to do stuff with the imported project.
//	 */
//	public static Job importSample(String projectName, URI uri, final Shell shell) {
//		ExampleProjectsImporterJob job = new ExampleProjectsImporterJob(uri, projectName, shell);
//		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
//		job.schedule();
//		return job;
//	}

	/**
	 * Create the main content of the 'Guides' section.
	 */
	private void createGuides() {
		FormToolkit toolkit = getToolkit();
		if (composite.isDisposed()) {
			return;
		}
		if (guidesComposite == null) {
			guidesComposite = toolkit.createComposite(composite, SWT.WRAP);
			TableWrapLayout layout = new TableWrapLayout();
			guidesComposite.setLayout(layout);
		}
		else {
			// clear the old links out
			Menu m = guidesComposite.getMenu();
			setMenu(guidesComposite, null);
			for (Control child : guidesComposite.getChildren()) {
				child.dispose();
			}
			setMenu(guidesComposite, m);
		}
		
		
		//TODO: running this in UI thread may be a bad idea. E.g. slow network connection
		// may make UI thread hang. Should popuplate contents of guides section with
		// a background job.
		GettingStartedGuide[] guides = GettingStartedGuides.getInstance().getAll();
		for (final GettingStartedGuide guide : guides) {
			displayGuide(guidesComposite, guide);
//				Composite slot = toolkit.createComposite(guidesComposite, SWT.WRAP);
				
//				TableWrapLayout twl = new TableWrapLayout();
//				twl.numColumns = 2;
//				slot.setLayout(twl);

		}
//
//
//					hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
//						@Override
//						public void linkActivated(HyperlinkEvent link) {
//							final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//							if (link.getHref() instanceof URI) {
//								URI uri = (URI) link.getHref();
//								String projectName = link.getLabel();
//
//								if (promptForDownload(shell, uri)) {
//									importSample(projectName, uri, shell);
//								}
//							}
//						}
//
//					});
//				}
//			}
//		}

		if (composite.getParent() instanceof ScrolledPageBook) {
			ScrolledPageBook sc = ((ScrolledPageBook) composite.getParent());
			sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			sc.layout(true, true);
		}
		composite.layout(true, true);
	}

	private void displayGuide(Composite composite, final GettingStartedGuide guide) {
		FormToolkit toolkit = getToolkit();
		String name = guide.getName();
		final ImageHyperlink link = toolkit.createImageHyperlink(composite, SWT.NONE);
		link.setText(name);
		link.setHref(guide.getHomePage());
		link.setImage(IdeUiPlugin.getImage(SMALL_ARROW_IMAGE));
		TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		link.setLayoutData(tableWrapData);
		
		link.setToolTipText(""+guide.getHomePage());
		
		String description = guide.getDescription();
		if (description==null || "".equals(description.trim())) {
			description = "<no description>";
		}
		Label descriptionLabel = toolkit.createLabel(composite, description, SWT.WRAP);
		//descriptionLabel.setForeground(color);
		TableWrapData descrData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
		descrData.indent = 20;
		descriptionLabel.setLayoutData(descrData);
		
		//Make the link clickable
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent evt) {
				try {
					System.out.println(guide.getHomePage());
					URL url = guide.getHomePage();
					System.out.println("url="+url);
					if (url!=null) {
						//PlatformUI.getWorkbench().getBrowserSupport().createBrowser(BROWSER_ID).openURL(url.toURL());
						//Use external browser for now because internal browser crashes eclipse a lot.
						//Also internal browser is unable to open the url because the repos are still private.
						//External browser works if already logged in to github as a user that has access to the
						// private repos.
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
					}
				} catch (Exception e) {
					GettingStartedActivator.log(e);
				}
			}
		});
		
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
