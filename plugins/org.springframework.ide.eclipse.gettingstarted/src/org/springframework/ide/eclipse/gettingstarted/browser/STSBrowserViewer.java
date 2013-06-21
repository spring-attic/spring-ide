/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *    Jacek Pospychala - jacek.pospychala@pl.ibm.com - fix for bug 224887
 *    Kris De Volder - Renamed to 'STSBrowserViewer and 
 *                     modified to use as browser for STS dashboard.
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.browser;

//Most of this code copied from org.eclipse.ui.internal.browser.BrowserViewer
// modified to reuse as a browser for STS dashboard.

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.browser.BrowserText;
import org.eclipse.ui.internal.browser.BusyIndicator;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.ui.internal.browser.Messages;
import org.eclipse.ui.internal.browser.ToolbarLayout;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WebBrowserUtil;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;

/**
 * A Web browser widget. It extends the Eclipse SWT Browser widget by adding an
 * optional toolbar complete with a URL combo box, history, back & forward, and
 * refresh buttons.
 * <p>
 * Use the style bits to choose which toolbars are available within the browser
 * composite. You can access the embedded SWT Browser directly using the
 * getBrowser() method.
 * </p>
 * <p>
 * Additional capabilities are available when used as the internal Web browser,
 * including status text and progress on the Eclipse window's status line, or
 * moving the toolbar capabilities up into the main toolbar.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LOCATION_BAR, BUTTON_BAR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>None</dd>
 * </dl>
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class STSBrowserViewer extends Composite {
    /**
     * Style parameter (value 1) indicating that the URL and Go button will be
     * on the local toolbar.
     */
    public static final int LOCATION_BAR = 1 << 1;

    /**
     * Style parameter (value 2) indicating that the toolbar will be available
     * on the web browser. This style parameter cannot be used without the
     * LOCATION_BAR style.
     */
    public static final int BUTTON_BAR = 1 << 2;
	 
	 protected static final String PROPERTY_TITLE = "title"; //$NON-NLS-1$

    private static final int MAX_HISTORY = 50;

    public Clipboard clipboard;

    public Combo combo;

    protected boolean showToolbar;

    protected boolean showURLbar;

    protected ToolItem back;

    protected ToolItem forward;

    protected BusyIndicator busy;

    protected boolean loading;

    protected static java.util.List<String> history;

    protected Browser browser;
    
    protected BrowserText text;

    protected boolean newWindow;

    protected IBrowserViewerContainer container;

    protected String title;

    protected int progressWorked = 0;
	 
	 protected List<PropertyChangeListener> propertyListeners;

    /**
     * Under development - do not use
     */
    public static interface ILocationListener {
        public void locationChanged(String url);

        public void historyChanged(String[] history2);
    }

    public ILocationListener locationListener;

    /**
     * Under development - do not use
     */
    public static interface IBackNextListener {
        public void updateBackNextBusy();
    }

    public IBackNextListener backNextListener;

	private String homeUrl;

	/**
     * Creates a new Web browser given its parent and a style value describing
     * its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in the class
     * header or class <code>SWT</code> which is applicable to instances of
     * this class, or must be built by <em>bitwise OR</em>'ing together (that
     * is, using the <code>int</code> "|" operator) two or more of those
     * <code>SWT</code> style constants. The class description lists the style
     * constants that are applicable to the class. Style bits are also inherited
     * from superclasses.
     * </p>
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public STSBrowserViewer(Composite parent, int style) {
        super(parent, SWT.NONE);
		  
        if ((style & LOCATION_BAR) != 0)
            showURLbar = true;

        if ((style & BUTTON_BAR) != 0)
            showToolbar = true;

        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        setLayout(layout);
        //No! Don't setLayoutData its really up to the client to decide how this component will be used / layed out.
        //The component itself should only control how its children are laid out!
        //setLayoutData(new GridData(GridData.FILL_BOTH));
        clipboard = new Clipboard(parent.getDisplay());
        
        if (showToolbar || showURLbar) {
            Composite toolbarComp = new Composite(this, SWT.NONE);
            toolbarComp.setLayout(new ToolbarLayout());
            toolbarComp.setLayoutData(new GridData(
                  GridData.VERTICAL_ALIGN_BEGINNING
                  | GridData.FILL_HORIZONTAL));

            if (showToolbar)
                createToolbar(toolbarComp);
            
				if (showURLbar)
                createLocationBar(toolbarComp);

				if (showToolbar | showURLbar) {
				    busy = new BusyIndicator(toolbarComp, SWT.NONE);
				    busy.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				    busy.addMouseListener(new MouseListener() {
						public void mouseDoubleClick(MouseEvent e) {
							// ignore
						}

						public void mouseDown(MouseEvent e) {
							home();
//							setURL("http://www.eclipse.org"); //$NON-NLS-1$
						}

						public void mouseUp(MouseEvent e) {
							// ignore
						}
				    });
				}
//				PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
//                  ContextIds.WEB_BROWSER); 
        }

        // create a new SWT Web browser widget, checking once again to make sure
        // we can use it in this environment
        //if (WebBrowserUtil.canUseInternalWebBrowser())
        try {
            this.browser = new Browser(this, SWT.NONE);
        }
        catch (SWTError e) {
        	GettingStartedActivator.log(e);
//          if (e.code!=SWT.ERROR_NO_HANDLES) {
                WebBrowserUtil.openError(Messages.errorCouldNotLaunchInternalWebBrowser);
                return;
//         }
//         text = new BrowserText(this, this, e);
        }

        if (showURLbar)
            updateHistory();
        if (showToolbar)
            updateBackNextBusy();

         if (browser!=null) {
            browser.setLayoutData(new GridData(GridData.FILL_BOTH));
//            PlatformUI.getWorkbench().getHelpSystem().setHelp(browser,
//                    ContextIds.WEB_BROWSER);
        }
        else
            text.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        addBrowserListeners();
        //listen();
    }

    /**
     * Returns the underlying SWT browser widget.
     * 
     * @return the underlying browser
     */
    public Browser getBrowser() {
        return browser;
    }

    public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
    
    /**
     * Navigate to the home URL.
     */
    public void home() {
    	if (homeUrl!=null) {
    		browser.setUrl(homeUrl);
    	} else {
    		browser.setText(""); //$NON-NLS-1$
    	}
    }

    /**
     * Loads a URL.
     * 
     * @param url
     *            the URL to be loaded
     * @return true if the operation was successful and false otherwise.
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the url is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #getURL()
     */
    public void setURL(String url) {
    	setURL(url, true);
    }

    protected void updateBackNextBusy() {
    	if (!back.isDisposed()) {
            back.setEnabled(isBackEnabled());
    	}
    	if (!forward.isDisposed()) {
            forward.setEnabled(isForwardEnabled());
    	}
    	if (!busy.isDisposed()) {
            busy.setBusy(loading);
        }

        if (backNextListener != null)
            backNextListener.updateBackNextBusy();
    }

    protected void updateLocation() {
        if (locationListener != null)
            locationListener.historyChanged(null);

        if (locationListener != null)
            locationListener.locationChanged(null);
    }

    /**
     *
     */
    private void addBrowserListeners() {
        if (browser==null) return;
        // respond to ExternalBrowserInstance StatusTextEvents events by
        // updating the status line
        browser.addStatusTextListener(new StatusTextListener() {
            public void changed(StatusTextEvent event) {
					//System.out.println("status: " + event.text); //$NON-NLS-1$
                if (container != null) {
                    IStatusLineManager status = container.getActionBars()
                            .getStatusLineManager();
                    status.setMessage(event.text);
                }
            }
        });

        // Add listener for new window creation so that we can instead of
        // opening a separate
        // new window in which the session is lost, we can instead open a new
        // window in a new
        // shell within the browser area thereby maintaining the session.
        browser.addOpenWindowListener(new OpenWindowListener() {
            public void open(WindowEvent event) {
                Shell shell2 = new Shell(getShell(), SWT.SHELL_TRIM );
                shell2.setLayout(new FillLayout());
                shell2.setText(Messages.viewWebBrowserTitle);
                shell2.setImage(getShell().getImage());
                if (event.location != null)
                    shell2.setLocation(event.location);
                if (event.size != null)
                    shell2.setSize(event.size);
				int style = 0;
				if (showURLbar)
					style += LOCATION_BAR;
				if (showToolbar)
					style += BUTTON_BAR;
                STSBrowserViewer browser2 = new STSBrowserViewer(shell2, style);
                browser2.newWindow = true;
                event.browser = browser2.browser;
            }
        });
		  
		  browser.addVisibilityWindowListener(new VisibilityWindowListener() {
				public void hide(WindowEvent e) {
					// ignore
				}
				
				public void show(WindowEvent e) {
					Browser browser2 = (Browser)e.widget;
					if (browser2.getParent().getParent() instanceof Shell) {
						Shell shell = (Shell) browser2.getParent().getParent();
						if (e.location != null)
							shell.setLocation(e.location);
						if (e.size != null)
							shell.setSize(shell.computeSize(e.size.x, e.size.y));
						shell.open();
					}
				}
			});

        browser.addCloseWindowListener(new CloseWindowListener() {
            public void close(WindowEvent event) {
                // if shell is not null, it must be a secondary popup window,
                // else its an editor window
                if (newWindow)
                    getShell().dispose();
                else
                    container.close();
            }
        });

        browser.addProgressListener(new ProgressListener() {
            public void changed(ProgressEvent event) {
					//System.out.println("progress: " + event.current + ", " + event.total); //$NON-NLS-1$ //$NON-NLS-2$
                if (event.total == 0)
                    return;

                boolean done = (event.current == event.total);

                int percentProgress = event.current * 100 / event.total;
                if (container != null) {
                    IProgressMonitor monitor = container.getActionBars()
                            .getStatusLineManager().getProgressMonitor();
                    if (done) {
                        monitor.done();
                        progressWorked = 0;
                    } else if (progressWorked == 0) {
                        monitor.beginTask("", event.total); //$NON-NLS-1$
                        progressWorked = percentProgress;
                    } else {
                        monitor.worked(event.current - progressWorked);
                        progressWorked = event.current;
                    }
                }

                if (showToolbar) {
                    if (!busy.isBusy() && !done)
                        loading = true;
                    else if (busy.isBusy() && done) // once the progress hits
                        // 100 percent, done, set
                        // busy to false
                        loading = false;

						  //System.out.println("loading: " + loading); //$NON-NLS-1$
                    updateBackNextBusy();
                    updateHistory();
                }
            }

            public void completed(ProgressEvent event) {
                if (container != null) {
                    IProgressMonitor monitor = container.getActionBars()
                            .getStatusLineManager().getProgressMonitor();
                    monitor.done();
                }
                if (showToolbar) {
                    loading = false;
                    updateBackNextBusy();
                    updateHistory();
                }
            }
        });

        if (showURLbar) {
            browser.addLocationListener(new LocationListener() {
                public void changed(LocationEvent event) {
                    if (!event.top)
                        return;
                    if (combo != null) {
                        if (!"about:blank".equals(event.location)) { //$NON-NLS-1$
                            combo.setText(event.location);
                            addToHistory(event.location);
                            updateHistory();
                        }// else
                        //    combo.setText(""); //$NON-NLS-1$
                    }
                }

                public void changing(LocationEvent event) {
                    // do nothing
                }
            });
        }

        browser.addTitleListener(new TitleListener() {
            public void changed(TitleEvent event) {
					 String oldTitle = title;
                title = event.title;
					 firePropertyChangeEvent(PROPERTY_TITLE, oldTitle, title);
            }
        });
    }
	 
	 /**
		 * Add a property change listener to this instance.
		 *
		 * @param listener java.beans.PropertyChangeListener
		 */
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			if (propertyListeners == null)
				propertyListeners = new ArrayList<PropertyChangeListener>();
			propertyListeners.add(listener);
		}

		/**
		 * Remove a property change listener from this instance.
		 *
		 * @param listener java.beans.PropertyChangeListener
		 */
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			if (propertyListeners != null)
				propertyListeners.remove(listener);
		}

		/**
		 * Fire a property change event.
		 */
		protected void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
			if (propertyListeners == null)
				return;

			PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
			//Trace.trace("Firing: " + event + " " + oldValue);
			try {
				int size = propertyListeners.size();
				PropertyChangeListener[] pcl = new PropertyChangeListener[size];
				propertyListeners.toArray(pcl);
				
				for (int i = 0; i < size; i++)
					try {
						pcl[i].propertyChange(event);
					} catch (Exception e) {
						// ignore
					}
			} catch (Exception e) {
				// ignore
			}
		}

    /**
     * Navigate to the next session history item. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #back
     */
    public boolean forward() {
        if (browser==null)
            return false;
        return browser.forward();
    }

    /**
     * Navigate to the previous session history item. Convenience method that
     * calls the underlying SWT browser.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #forward
     */
    public boolean back() {
        if (browser==null)
            return false;
        return browser.back();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the previous
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's back command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @see #back
     */
    public boolean isBackEnabled() {
        if (browser==null)
            return false;
        return browser.isBackEnabled();
    }

    /**
     * Returns <code>true</code> if the receiver can navigate to the next
     * session history item, and <code>false</code> otherwise. Convenience
     * method that calls the underlying SWT browser.
     * 
     * @return the receiver's forward command enabled state
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * @see #forward
     */
    public boolean isForwardEnabled() {
        if (browser==null)
            return false;
        return browser.isForwardEnabled();
    }

    /**
     * Stop any loading and rendering activity. Convenience method that calls
     * the underlying SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void stop() {
        if (browser!=null)
            browser.stop();
    }

    /**
     * 
     */
    private boolean navigate(String url) {
    //    Trace.trace(Trace.FINER, "Navigate: " + url); //$NON-NLS-1$
        if (url != null && url.equals(getURL())) {
            refresh();
            return true;
        }
        if (browser!=null)
            return browser.setUrl(url, null, new String[] {"Cache-Control: no-cache"}); //$NON-NLS-1$
        return text.setUrl(url);
    }
 
    /**
     * Refresh the current page. Convenience method that calls the underlying
     * SWT browser.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     */
    public void refresh() {
        if (browser!=null)
            browser.refresh();
        else
            text.refresh();
		  try {
			  Thread.sleep(50);
		  } catch (Exception e) {
			  // ignore
		  }
    }

    private void setURL(String url, boolean browse) {
    //    Trace.trace(Trace.FINEST, "setURL: " + url + " " + browse); //$NON-NLS-1$ //$NON-NLS-2$
        if (url == null) {
            home();
            return;
        }

        if ("eclipse".equalsIgnoreCase(url)) //$NON-NLS-1$
            url = "http://www.eclipse.org"; //$NON-NLS-1$
        else if ("wtp".equalsIgnoreCase(url)) //$NON-NLS-1$
            url = "http://www.eclipse.org/webtools/"; //$NON-NLS-1$

        if (browse)
            navigate(url);

        addToHistory(url);
        updateHistory();
    }

    protected void addToHistory(String url) {
        if (history == null)
            history = WebBrowserPreference.getInternalWebBrowserHistory();
        int found = -1;
        int size = history.size();
        for (int i = 0; i < size; i++) {
            String s = history.get(i);
            if (s.equals(url)) {
                found = i;
                break;
            }
        }

        if (found == -1) {
            if (size >= MAX_HISTORY)
                history.remove(size - 1);
            history.add(0, url);
            WebBrowserPreference.setInternalWebBrowserHistory(history);
        } else if (found != 0) {
            history.remove(found);
            history.add(0, url);
            WebBrowserPreference.setInternalWebBrowserHistory(history);
        }
    }

    /**
     *
     */
    public void dispose() {
        super.dispose();

        showToolbar = false;

        if (busy != null)
            busy.dispose();
        busy = null;

        browser = null;
        text = null;
        if (clipboard!=null)
        	clipboard.dispose();
        clipboard=null;

        removeSynchronizationListener();
    }

    protected ToolBar createLocationBar(Composite parent) {
        combo = new Combo(parent, SWT.DROP_DOWN);

        updateHistory();

        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent we) {
                try {
                    if (combo.getSelectionIndex() != -1 && !combo.getListVisible()) {
                        setURL(combo.getItem(combo.getSelectionIndex()));
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        });
        combo.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                setURL(combo.getText());
            }
        });
        
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

        ToolItem go = new ToolItem(toolbar, SWT.NONE);
        go.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_GO));
        go.setHotImage(ImageResource.getImage(ImageResource.IMG_CLCL_NAV_GO));
        go.setDisabledImage(ImageResource
                .getImage(ImageResource.IMG_DLCL_NAV_GO));
        go.setToolTipText(Messages.actionWebBrowserGo);
        go.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setURL(combo.getText());
            }
        });
		  
		  return toolbar;
    }

    protected ToolBar createToolbar(Composite parent) {
		  ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

		// create 'home' action
		ToolItem home = new ToolItem(toolbar, SWT.NONE);
		home.setImage(Images.getImage(Images.IMG_NAV_HOME));
		home.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				home();
			}
		});
		  
        // create back and forward actions
        back = new ToolItem(toolbar, SWT.NONE);
        back.setImage(ImageResource
                .getImage(ImageResource.IMG_ELCL_NAV_BACKWARD));
        back.setHotImage(ImageResource
                .getImage(ImageResource.IMG_CLCL_NAV_BACKWARD));
        back.setDisabledImage(ImageResource
                .getImage(ImageResource.IMG_DLCL_NAV_BACKWARD));
        back.setToolTipText(Messages.actionWebBrowserBack);
        back.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                back();
            }
        });

        forward = new ToolItem(toolbar, SWT.NONE);
        forward.setImage(ImageResource
                .getImage(ImageResource.IMG_ELCL_NAV_FORWARD));
        forward.setHotImage(ImageResource
                .getImage(ImageResource.IMG_CLCL_NAV_FORWARD));
        forward.setDisabledImage(ImageResource
                .getImage(ImageResource.IMG_DLCL_NAV_FORWARD));
        forward.setToolTipText(Messages.actionWebBrowserForward);
        forward.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                forward();
            }
        });

        // create refresh, stop, and print actions
        ToolItem stop = new ToolItem(toolbar, SWT.NONE);
        stop.setImage(ImageResource.getImage(ImageResource.IMG_ELCL_NAV_STOP));
        stop.setHotImage(ImageResource
                .getImage(ImageResource.IMG_CLCL_NAV_STOP));
        stop.setDisabledImage(ImageResource
                .getImage(ImageResource.IMG_DLCL_NAV_STOP));
        stop.setToolTipText(Messages.actionWebBrowserStop);
        stop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                stop();
            }
        });

        ToolItem refresh = new ToolItem(toolbar, SWT.NONE);
        refresh.setImage(ImageResource
                .getImage(ImageResource.IMG_ELCL_NAV_REFRESH));
        refresh.setHotImage(ImageResource
                .getImage(ImageResource.IMG_CLCL_NAV_REFRESH));
        refresh.setDisabledImage(ImageResource
                .getImage(ImageResource.IMG_DLCL_NAV_REFRESH));
        refresh.setToolTipText(Messages.actionWebBrowserRefresh);
        refresh.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });
		  
		  return toolbar;
    }

    /**
     * Returns the current URL. Convenience method that calls the underlying SWT
     * browser.
     * 
     * @return the current URL or an empty <code>String</code> if there is no
     *         current URL
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * @see #setURL(String)
     */
    public String getURL() {
        if (browser!=null)
            return browser.getUrl();
        return text.getUrl();
    }

    public boolean setFocus() {
        if (browser!=null) {
            browser.setFocus();
            updateHistory();
            return true;
        }
        return super.setFocus();
    }

    /**
     * Update the history list to the global/shared copy.
     */
    protected void updateHistory() {
        if (combo == null || combo.isDisposed())
            return;

        String temp = combo.getText();
        if (history == null)
            history = WebBrowserPreference.getInternalWebBrowserHistory();

        String[] historyList = new String[history.size()];
        history.toArray(historyList);
        combo.setItems(historyList);

        combo.setText(temp);
    }

    public IBrowserViewerContainer getContainer() {
        return container;
    }

    public void setContainer(IBrowserViewerContainer container) {
    	if (container==null && this.container!=null) {
    		IStatusLineManager manager = this.container.getActionBars().getStatusLineManager();
    		if (manager!=null) 
    			manager.getProgressMonitor().done();
    	}
        this.container = container;
    }

    protected File file;
    protected long timestamp;
    protected Thread fileListenerThread;
    protected LocationListener locationListener2;
    protected Object syncObject = new Object();
    
    protected void addSynchronizationListener() {
   	 if (fileListenerThread != null)
   		 return;
   	 
   	 fileListenerThread = new Thread("Browser file synchronization") { //$NON-NLS-1$
   		 public void run() {
   			 while (fileListenerThread != null) {
   				 try {
   					 Thread.sleep(2000);
   				 } catch (Exception e) {
   					 // ignore
   				 }
   				 synchronized (syncObject) {
						 if (file != null && file.lastModified() != timestamp) {
	   					 timestamp = file.lastModified();
	   					 Display.getDefault().syncExec(new Runnable() {
	 							public void run() {
	 								refresh();
	 							}
	   					 });
						 }
					  }
   			 }
   		 }
   	 };
   	 fileListenerThread.setDaemon(true);
   	 fileListenerThread.setPriority(Thread.MIN_PRIORITY);
   	 
   	 locationListener2 = new LocationListener() {
          public void changed(LocationEvent event) {
         	 File temp = getFile(event.location);
         	 if (temp != null && temp.exists()) {
         		 synchronized (syncObject) {
         			 file = temp;
            		 timestamp = file.lastModified();
					 }
         	 } else
         		 file = null;
          }
          
          public void changing(LocationEvent event) {
             // do nothing
         }
       };
       browser.addLocationListener(locationListener2);
       
       File temp = getFile(browser.getUrl());
   	 if (temp != null && temp.exists()) {
   		file = temp;
      	timestamp = file.lastModified();
   	 }
   	 fileListenerThread.start();
    }

    protected static File getFile(String location) {
   	 if (location == null)
   		 return null;
   	 if (location.startsWith("file:/")) //$NON-NLS-1$
   		 location = location.substring(6);
   	 
   	 return new File(location);
    }

    protected void removeSynchronizationListener() {
   	 if (fileListenerThread == null)
   		 return;
   	 
   	 fileListenerThread = null;
   	 browser.removeLocationListener(locationListener2);
   	 locationListener2 = null;
    }
}
