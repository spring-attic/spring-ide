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
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import java.awt.GridLayout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.progress.UIJob;
import org.springframework.ide.eclipse.gettingstarted.browser.BrowserFactory;
import org.springframework.ide.eclipse.gettingstarted.browser.STSBrowserViewer;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.wizard.GSImportWizard;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPage;

/**
 * Alternate implementation of GuidesDashBoardPage. Uses a narrow column on the left to
 * display the guides info and on the larger area on the right is an embedded browser
 * widget that shows a preview of the Guide you clicked on.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class GuidesDashboardPageWithPreview extends ADashboardPage {

	private static final String SMALL_ARROW_IMAGE = "rss/overlay-incoming.png";
	
	/**
	 * Determines whether all nodes are intially expanded or not.
	 */
	private static final boolean EXPAND_INITIALLY = true;
	
	/**
	 * If enabled, expanding one element will automatically collapse the others.
	 */
	private static final boolean EXPAND_SINGLE = false;

	private static final String HOME_URL = "http://www.springsource.org/news-events";
	
	private ScrolledForm leftForm; //Left panel
	private Composite guidesComposite; //left panel's body all content should go in here.
//	private Composite browserComposite; //right panel
	private FormToolkit toolkit;

	private RefreshJob refreshJob;

	private Browser browser;

	private Form form;
	
	private Expandor expandor = EXPAND_SINGLE ? new Expandor() : null;
	
	private class Expandor {
		
		private ExpandableComposite expanded = null;

		//To manage the expansion state of the guides. We only keep one open at a time. 
		//So one opening another one the previous one must be collapsed first.
		
		public void expand(ExpandableComposite it) {
			if (this.expanded==it) {
				return; //already expanded. nothing to do.
			}
			if (this.expanded!=null) {
				this.expanded.setExpanded(false);
			}
			this.expanded = it;
			it.setExpanded(true);
			refreshUILayout();
		}

	}

	public GuidesDashboardPageWithPreview() {
	}
	
	@Override
	public void dispose() {
		if (toolkit!=null) {
			toolkit.dispose();
		}
		super.dispose();
	}

	@Override
	protected void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
//		form.setExpandHorizontal(false);
//		form.setExpandVertical(false);
		
		Composite body = form.getBody();
		body.setLayout(new FillLayout());
		
//		body = toolkit.createComposite(body);
//		body.setLayout(new FillLayout());
		
		final SashForm sash = new SashForm(body, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sash);
//		sash.addDragDetectListener(new DragDetectListener() {
//			@Override
//			public void dragDetected(DragDetectEvent e) {
//				refreshUILayout();
//			}
//		});
		
//		form.getBody().addControlListener(new ControlListener() {
//			@Override
//			public void controlResized(ControlEvent e) {
//				Rectangle bounds = form.getBody().getBounds();
//				sash.setLayoutData(new GridData(bounds.width-14, bounds.height-32));
//			}
//			
//			@Override
//			public void controlMoved(ControlEvent e) {
//			}
//		});

		//It seems trying to make the composite with the toolkit with SWT.V_SCROLL
		// doesn't work. You get a compostie with scrollbars but the contents
		// inside the composite doesn't move with the scrollbars.
		//The solution seems to be to use a scrolledForm instead.
		
		leftForm = toolkit.createScrolledForm(sash);
//		leftForm.setExpandHorizontal(false);
		leftForm.getBody().setLayout(new TableWrapLayout());
		
		Section guidesSection = toolkit.createSection(leftForm.getBody(), 
	//			Section.DESCRIPTION|
				Section.TITLE_BAR|
				Section.TWISTIE|
				(EXPAND_INITIALLY ? Section.EXPANDED : 0)
		);
		
		guidesSection.setText("Getting Started Guides");
		guidesSection.setDescription(GettingStartedGuide.GUIDE_DESCRIPTION_TEXT);
		guidesComposite = toolkit.createComposite(guidesSection);
		guidesComposite.setLayout(new TableWrapLayout());
		guidesSection.setClient(guidesComposite);
		guidesSection.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
			}
			public void expansionStateChanged(ExpansionEvent e) {
				refreshUILayout();
			}
		});
		
//		guidesComposite = leftForm.getBody();
		
//		browserComposite = toolkit.createComposite(sash); // right section of sash
		STSBrowserViewer browserViewer = BrowserFactory.create(sash);
//		browserComposite = browserViewer;
//		browserComposite.setLayout(new FillLayout());
		
		browser = browserViewer.getBrowser();
		browserViewer.setHomeUrl(HOME_URL);
		browser.setUrl(HOME_URL);
		
		sash.setWeights(new int[] { 1, 2});
		
		
//		this.composite = toolkit.createComposite(form, SWT.NONE);
//		composite.setLayout(new GridLayout(2, false));
//		this.guidesComposite = new Composite(composite, SWT.NONE);
//		GridDataFactory.fillDefaults().grab(false, true).hint(SWT.DEFAULT, 200).applyTo(guidesComposite);
////		createGuides(guidesComposite);
//		
//		Label hello = new Label(guidesComposite, SWT.NONE);
//		hello.setText("Hello");
//		GridDataFactory.fillDefaults().applyTo(hello);
		
		createGuides(guidesComposite);
	}
	
	/**
	 * Create the main content of the 'Guides' section.
	 */
	private void createGuides(Composite guidesComposite) {
		for (Control child : guidesComposite.getChildren()) {
			child.dispose();
		}
		refreshGuides();
	}

	/**
	 * Schedules a job to retrieve getting started content and then once it has the content...
	 * update the UI with the new content.
	 */
	private void refreshGuides() {
		if (this.refreshJob==null) {
			this.refreshJob = new RefreshJob();
		}
		refreshJob.schedule();
	}
	
	private class RefreshJob extends Job {
		
		public RefreshJob() {
			super("Update Guides");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final GettingStartedGuide[] guides = GettingStartedContent.getInstance().getGuides();
			//Then next bit of work actually needs to run in the UI Thread because it
			// does widget creation stuff.
			UIJob uiJob = new UIJob("Display guides") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						//TODO: erase prior content if there's any already there.
						for (final GettingStartedGuide guide : guides) {
							displayGuide(guidesComposite, guide);
	//							Composite slot = toolkit.createComposite(guidesComposite, SWT.WRAP);
								
	//							TableWrapLayout twl = new TableWrapLayout();
	//							twl.numColumns = 2;
	//							slot.setLayout(twl);
	
						}
//						guidesComposite.layout(true, true);
						refreshUILayout();
					} catch (SWTException e) {
						//ignore.. we get this if someone closes the dash while this job is being scheduled / run.
						//The widgets are already disposed and can't really be refreshed anymore so its ok to 
						//just ignore these errors.
					}
					return Status.OK_STATUS;
				}
			};
			uiJob.schedule();
			return Status.OK_STATUS;
			
		}

	}

	

	private void displayGuide(final Composite parent, final GettingStartedGuide guide) {
		if (!parent.isDisposed()) {
			//Avoid errors trying to update content in an already closed dashboard.
			
			final ExpandableComposite composite = toolkit.createExpandableComposite(parent, 
					ExpandableComposite.CLIENT_INDENT |
					ExpandableComposite.TWISTIE | 
					(EXPAND_INITIALLY ? ExpandableComposite.EXPANDED : 0)
			);
			
			composite.addExpansionListener(new IExpansionListener() {
				
				@Override
				public void expansionStateChanging(ExpansionEvent e) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					if (EXPAND_SINGLE && composite.isExpanded()) {
						expandor.expand(composite); //actually calling to track current expanded element and collapse others.
					}
					refreshUILayout();
				}
			});
			
			composite.setText(guide.getDisplayName());
			TableWrapData td = new TableWrapData();
			composite.setLayoutData(td);
			
//			Composite guideDetails = toolkit.createComposite(composite);
//			guideDetails.setLayout(new GridLayout(2, false));
			
			String desc = guide.getDescription();
			if (desc==null || desc.trim().equals("")) {
				desc = "<No Description>";
			}
			
			Composite client = toolkit.createComposite(composite);
			composite.setClient(client);
			TableWrapLayout twl = new TableWrapLayout();
			//Make layout more compact. Don't add more empty space around this composite!
			twl.leftMargin = 0;
			twl.rightMargin = 0;
			twl.topMargin = 0;
			twl.bottomMargin = 0;
			client.setLayout(twl);
			composite.setLayoutData(td);
			
			FormText description = toolkit.createFormText(client, true);
			description.setText(desc, false, false);
			
			FormText links = toolkit.createFormText(client, true);
//			links.setText("<form><p>Greetings: <a href=\"hello\">Hello</a></p></form>", true, false);
			links.setText("<form><p>" +
					"<a href=\"read\">Browse</a> " +
					"<a href=\"import\">Import</a>" +
					"</p></form>",
					/*parseTags*/true,
					/*expandUrls*/ false
			);
			links.setLayoutData(new TableWrapData(TableWrapData.FILL));
			links.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					Object target = e.getHref();
					if ("read".equals(target)) {
						browser.setUrl(guide.getHomePage().toString());
					} else if ("import".equals(target)) {
						GSImportWizard.open(getShell(), guide);
					}
				}
			});
			
//			String name = guide.getName();
//			final ImageHyperlink link = toolkit.createImageHyperlink(composite, SWT.NONE);
//			link.setText(name);
//			link.setHref(guide.getHomePage());
//			link.setImage(IdeUiPlugin.getImage(SMALL_ARROW_IMAGE));
//			TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
//			link.setLayoutData(tableWrapData);
//			
//			link.setToolTipText(""+guide.getHomePage());
//			
//			String description = guide.getDescription();
//			if (description==null || "".equals(description.trim())) {
//				description = "<no description>";
//			}
//			
//			////////////////////
//			// import button
//			
//			Button importButton = toolkit.createButton(composite, "Import", SWT.PUSH);
//			TableWrapData importData = new TableWrapData(TableWrapData.RIGHT, TableWrapData.TOP);
//			importButton.setLayoutData(importData);
//			importButton.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					GuideImportWizard.open(composite.getShell(), guide);
//				}
//			});
//			
//			/////////////////
//			//Description
//			
//			Label descriptionLabel = toolkit.createLabel(composite, description, SWT.WRAP);
//			//descriptionLabel.setForeground(color);
//			TableWrapData descrData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.MIDDLE);
//			descrData.colspan = 2;
//			descrData.indent = 20;
//			descriptionLabel.setLayoutData(descrData);
//			
//			//Make the link clickable
//			link.addHyperlinkListener(new HyperlinkAdapter() {
//				@Override
//				public void linkActivated(HyperlinkEvent evt) {
//					try {
//						System.out.println(guide.getHomePage());
//						URL url = guide.getHomePage();
//						System.out.println("url="+url);
//						if (url!=null) {
//							//PlatformUI.getWorkbench().getBrowserSupport().createBrowser(BROWSER_ID).openURL(url.toURL());
//							//Use external browser for now because internal browser crashes eclipse a lot.
//							//Also internal browser is unable to open the url because the repos are still private.
//							//External browser works if already logged in to github as a user that has access to the
//							// private repos.
//							PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
//						}
//					} catch (Exception e) {
//						GettingStartedActivator.log(e);
//					}
//				}
//			});
		}
	}
	
	
	private void refreshUILayout() {
		//Warning... a lot of trial and error to get this to work!
		// Modify with care :-)
		leftForm.reflow(true);

//		guidesComposite.layout(true, true); //Scrollbars don't appear
//		guidesComposite.getParent().layout(true, true); //Scrollbars don't appear!
//		form.reflow(true); //Don't do this!!! It will resize the left/right sash components to weird sizes.
		
	}

	@Override
	public String getName() {
		return "Side by Side";
	}

}
