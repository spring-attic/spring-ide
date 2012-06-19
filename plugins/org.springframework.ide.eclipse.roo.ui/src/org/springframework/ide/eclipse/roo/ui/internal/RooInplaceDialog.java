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
package org.springframework.ide.eclipse.roo.ui.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;


/**
 * @author Christian Dupuis
 * @since 2.5.0
 */
@SuppressWarnings("restriction")
public class RooInplaceDialog {

	/**
	 * Static inner class which sets the layout for the inplace view. Without
	 * this, the inplace view will not be populated.
	 * 
	 * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl
	 */
	private static class BorderFillLayout extends Layout {

		/** The border widths. */
		final int fBorderSize;

		/**
		 * Creates a fill layout with a border.
		 */
		public BorderFillLayout(int borderSize) {
			if (borderSize < 0) {
				throw new IllegalArgumentException();
			}
			fBorderSize = borderSize;
		}

		/**
		 * Returns the border size.
		 */
		@SuppressWarnings("unused")
		public int getBorderSize() {
			return fBorderSize;
		}

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {

			Control[] children = composite.getChildren();
			Point minSize = new Point(0, 0);

			if (children != null) {
				for (Control element : children) {
					Point size = element.computeSize(wHint, hHint, flushCache);
					minSize.x = Math.max(minSize.x, size.x);
					minSize.y = Math.max(minSize.y, size.y);
				}
			}

			minSize.x += fBorderSize * 2 + 3;
			minSize.y += fBorderSize * 2;

			return minSize;
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {

			Control[] children = composite.getChildren();
			Point minSize = new Point(composite.getClientArea().width, composite.getClientArea().height);

			if (children != null) {
				for (Control child : children) {
					child.setSize(minSize.x - fBorderSize * 2, minSize.y - fBorderSize * 2);
					child.setLocation(fBorderSize, fBorderSize);
				}
			}
		}
	}

	/**
	 * Move action for the dialog.
	 */
	private class MoveAction extends Action {

		MoveAction() {
			super("&Move", IAction.AS_PUSH_BUTTON);
		}

		@Override
		public void run() {
			performTrackerAction(SWT.NONE);
			isDeactivateListenerActive = true;
		}

	}

	/**
	 * Remember bounds action for the dialog.
	 */
	private class RememberBoundsAction extends Action {

		RememberBoundsAction() {
			super("Remember Size and &Location", IAction.AS_CHECK_BOX);
			setChecked(!getDialogSettings().getBoolean(STORE_DISABLE_RESTORE_LOCATION));
		}

		@Override
		public void run() {
			IDialogSettings settings = getDialogSettings();

			boolean newValue = !isChecked();
			// store new value
			settings.put(STORE_DISABLE_RESTORE_LOCATION, newValue);
			settings.put(STORE_DISABLE_RESTORE_SIZE, newValue);

			isDeactivateListenerActive = true;
		}
	}

	/**
	 * Resize action for the dialog.
	 */
	private class ResizeAction extends Action {

		ResizeAction() {
			super("&Resize", IAction.AS_PUSH_BUTTON);
		}

		@Override
		public void run() {
			performTrackerAction(SWT.RESIZE);
			isDeactivateListenerActive = true;
		}

	}

	/**
	 * Dialog constants telling whether this control can be resized or move.
	 */
	public static final String STORE_DISABLE_RESTORE_SIZE = "DISABLE_RESTORE_SIZE"; //$NON-NLS-1$

	public static final String STORE_DISABLE_RESTORE_LOCATION = "DISABLE_RESTORE_LOCATION"; //$NON-NLS-1$

	/**
	 * Dialog store constant for the location's x-coordinate, location's
	 * y-coordinate and the size's width and height.
	 */
	private static final String STORE_LOCATION_X = "location.x"; //$NON-NLS-1$

	private static final String STORE_LOCATION_Y = "location.y"; //$NON-NLS-1$

	private static final String STORE_SIZE_WIDTH = "size.width"; //$NON-NLS-1$

	private static final String STORE_SIZE_HEIGHT = "size.height"; //$NON-NLS-1$

	/**
	 * The name of the dialog store's section associated with the inplace
	 * XReference view.
	 */
	private final String sectionName = RooInplaceDialog.class.getName();
	
	/**
	 * Fields for text matching and filtering
	 */
	private Text filterText;

	private Font statusTextFont;

	/**
	 * Remembers the bounds for this information control.
	 */
	private Rectangle bounds;

	private Rectangle trim;

	/**
	 * Fields for view menu support.
	 */
	private ToolBar toolBar;

	private MenuManager viewMenuManager;

	private IWorkbenchPart workbenchPart;

	private IProject selectedProject;

	private Label statusField;

	private boolean isDeactivateListenerActive = false;

	private Composite composite, viewMenuButtonComposite;

	private final int shellStyle;

	private Listener deactivateListener;

	private Shell parentShell;

	private Shell dialogShell;

	private RooShellTab tab;

	private Label label;

	/**
	 * Constructor which takes the parent shell
	 */
	public RooInplaceDialog(Shell parent) {
		parentShell = parent;
		shellStyle = SWT.RESIZE;
	}

	/**
	 * Close the dialog
	 */
	public void close() {
		storeBounds();
		toolBar = null;
		viewMenuManager = null;
	}

	// --------------------- adding listeners ---------------------------

	public void dispose() {
		filterText = null;
		if (dialogShell != null) {
			if (!dialogShell.isDisposed()) {
				dialogShell.dispose();
			}
			dialogShell = null;
			parentShell = null;
			composite = null;
		}
	}

	// --------------------- creating and filling the menu
	// ---------------------------

	public Rectangle getDefaultBounds() {
		GC gc = new GC(composite);
		gc.setFont(composite.getFont());
		int width = gc.getFontMetrics().getAverageCharWidth();
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();

		Point size = new Point(60 * width, 4 * height);
		Point location = getDefaultLocation(size);
		return new Rectangle(location.x, location.y, size.x, size.y);
	}

	public boolean isOpen() {
		return dialogShell != null;
	}

	/**
	 * Open the dialog
	 */
	public void open() {
		// If the dialog is already open, dispose the shell and recreate it
		if (dialogShell != null) {
			close();
		}
		createContents();
		createShell();
		createComposites();
		filterText = createFilterText(viewMenuButtonComposite);
		// creates the drop down menu and creates the actions
		createViewMenu(viewMenuButtonComposite);
		// createHorizontalSeparator(composite);

		createStatusField(composite);
		// set the tab order
		viewMenuButtonComposite.setTabList(new Control[] { filterText });
		composite.setTabList(new Control[] { viewMenuButtonComposite });

		setInfoSystemColor();
		addListenersToShell();
		initializeBounds();
		// open the window
		dialogShell.open();
	}

	/**
	 * @param workbenchPart The workbenchPart to set.
	 */
	public void setWorkbenchPart(IWorkbenchPart workbenchPart) {
		this.workbenchPart = workbenchPart;
	}

	// --------------------- creating and filling the status field
	// ---------------------------

	private void addListenersToShell() {
		dialogShell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				close();
				if (statusTextFont != null && !statusTextFont.isDisposed()) {
					statusTextFont.dispose();
				}

				// dialogShell = null;
				composite = null;
				filterText = null;
				statusTextFont = null;

			}
		});

		deactivateListener = new Listener() {
			public void handleEvent(Event event) {
				if (isDeactivateListenerActive) {
					dispose();
				}
			}
		};

		dialogShell.addListener(SWT.Deactivate, deactivateListener);
		isDeactivateListenerActive = true;
		dialogShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				if (e.widget == dialogShell && dialogShell.getShells().length == 0) {
					isDeactivateListenerActive = true;
				}
			}
		});

		dialogShell.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				bounds = dialogShell.getBounds();
				if (trim != null) {
					Point location = composite.getLocation();
					bounds.x = bounds.x - trim.x + location.x;
					bounds.y = bounds.y - trim.y + location.y;
				}

			}

			@Override
			public void controlResized(ControlEvent e) {
				bounds = dialogShell.getBounds();
				if (trim != null) {
					Point location = composite.getLocation();
					bounds.x = bounds.x - trim.x + location.x;
					bounds.y = bounds.y - trim.y + location.y;
				}
			}
		});
	}

	// ----------- all to do with setting the bounds of the dialog -------------

	private void createComposites() {
		// Composite for filter text and tree
		composite = new Composite(dialogShell, SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		viewMenuButtonComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewMenuButtonComposite.setLayout(layout);
		viewMenuButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createContents() {
		if (workbenchPart != null) {
			try {
				RooShellView view = (RooShellView) workbenchPart.getSite().getPage().showView(
						"com.springsource.sts.roo.ui.rooShellView", null, IWorkbenchPage.VIEW_ACTIVATE);
				this.tab = view.openShell(this.selectedProject);
			}
			catch (PartInitException e) {
			}
		}
	}

	private Text createFilterText(Composite parent) {
		label = new Label(parent, SWT.NONE);
		label.setText("roo> ");

		filterText = new Text(parent, SWT.NONE);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		GC gc = new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.CENTER;
		filterText.setLayoutData(data);

		this.tab.addTypeFieldAssistToText(this.filterText);
		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == 0x1B) { // ESC
					dispose();
					return;
				}
				isDeactivateListenerActive = false; // otherwise dialog will be disposed when popup opens
				tab.processKeyEvent(tab.getText(), e, filterText.getText());
				isDeactivateListenerActive = true;
				if (e.doit && e.keyCode == 0x0D) { // return
					dispose();
					return;
				}
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		return filterText;
	}

	private void createHorizontalSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createShell() {
		// Create the shell
		dialogShell = new Shell(parentShell, shellStyle);

		// To handle "ESC" case
		dialogShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent event) {
				event.doit = false; // don't close now
				dispose();
			}
		});

		Display display = dialogShell.getDisplay();
		dialogShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : 1;
		dialogShell.setLayout(new BorderFillLayout(border));

	}

	private void createStatusField(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createHorizontalSeparator(comp);

		// Status field label
		statusField = new Label(parent, SWT.RIGHT);
		statusField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statusField.setText("Type Roo command and press 'Enter'");
		Font font = statusField.getFont();
		Display display = parent.getDisplay();
		FontData[] fontDatas = font.getFontData();
		for (FontData element : fontDatas) {
			element.setHeight(element.getHeight() * 9 / 10);
		}
		Font statusTextFont = new Font(display, fontDatas);
		statusField.setFont(statusTextFont);
	}

	// ----------- all to do with filtering text

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		ToolItem viewMenuButton = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		data.verticalAlignment = GridData.BEGINNING;
		toolBar.setLayoutData(data);

		viewMenuButton.setImage(JavaPluginImages.get(JavaPluginImages.IMG_ELCL_VIEW_MENU));
		viewMenuButton.setDisabledImage(JavaPluginImages.get(JavaPluginImages.IMG_DLCL_VIEW_MENU));
		viewMenuButton.setToolTipText("Menu");
		viewMenuButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});
	}

	private void fillViewMenu(IMenuManager viewMenu) {
		viewMenu.add(new GroupMarker("SystemMenuStart")); //$NON-NLS-1$
		viewMenu.add(new MoveAction());
		viewMenu.add(new ResizeAction());
		viewMenu.add(new RememberBoundsAction());
		viewMenu.add(new Separator("SystemMenuEnd")); //$NON-NLS-1$
	}

	// ---------- shuts down the dialog ---------------

	private Point getDefaultLocation(Point initialSize) {
		Monitor monitor = dialogShell.getDisplay().getPrimaryMonitor();
		if (parentShell != null) {
			monitor = parentShell.getMonitor();
		}

		Rectangle monitorBounds = monitor.getClientArea();
		Point centerPoint;
		if (parentShell != null) {
			centerPoint = Geometry.centerPoint(parentShell.getBounds());
		}
		else {
			centerPoint = Geometry.centerPoint(monitorBounds);
		}

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(monitorBounds.y, Math.min(centerPoint.y
				- (initialSize.y * 2 / 3), monitorBounds.y + monitorBounds.height - initialSize.y)));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = RooUiActivator.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null) {
			settings = RooUiActivator.getDefault().getDialogSettings().addNewSection(sectionName);
		}

		return settings;
	}

	// ------------------ moving actions --------------------------

	private MenuManager getViewMenuManager() {
		if (viewMenuManager == null) {
			viewMenuManager = new MenuManager();
			fillViewMenu(viewMenuManager);
		}
		return viewMenuManager;
	}

	/**
	 * Initialize the shell's bounds.
	 */
	private void initializeBounds() {
		// if we don't remember the dialog bounds then reset
		// to be the defaults (behaves like inplace outline view)
		Rectangle oldBounds = restoreBounds();
		if (oldBounds != null) {
			dialogShell.setBounds(oldBounds);
			return;
		}
		dialogShell.setBounds(getDefaultBounds());
	}

	/**
	 * Perform the requested tracker action (resize or move).
	 * 
	 * @param style The track style (resize or move).
	 */
	private void performTrackerAction(int style) {
		Tracker tracker = new Tracker(dialogShell.getDisplay(), style);
		tracker.setStippled(true);
		Rectangle[] r = new Rectangle[] { dialogShell.getBounds() };
		tracker.setRectangles(r);
		isDeactivateListenerActive = false;
		if (tracker.open()) {
			dialogShell.setBounds(tracker.getRectangles()[0]);
			isDeactivateListenerActive = true;
		}
	}

	private Rectangle restoreBounds() {

		IDialogSettings dialogSettings = getDialogSettings();

		boolean controlRestoresSize = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_SIZE);
		boolean controlRestoresLocation = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_LOCATION);

		Rectangle bounds = new Rectangle(-1, -1, -1, -1);

		if (controlRestoresSize) {
			try {
				bounds.width = dialogSettings.getInt(STORE_SIZE_WIDTH);
				bounds.height = dialogSettings.getInt(STORE_SIZE_HEIGHT);
			}
			catch (NumberFormatException ex) {
				bounds.width = -1;
				bounds.height = -1;
			}
		}

		if (controlRestoresLocation) {
			try {
				bounds.x = dialogSettings.getInt(STORE_LOCATION_X);
				bounds.y = dialogSettings.getInt(STORE_LOCATION_Y);
			}
			catch (NumberFormatException ex) {
				bounds.x = -1;
				bounds.y = -1;
			}
		}

		// sanity check
		if (bounds.x == -1 && bounds.y == -1 && bounds.width == -1 && bounds.height == -1) {
			return null;
		}

		Rectangle maxBounds = null;
		if (dialogShell != null && !dialogShell.isDisposed()) {
			maxBounds = dialogShell.getDisplay().getBounds();
		}
		else {
			// fallback
			Display display = Display.getCurrent();
			if (display == null) {
				display = Display.getDefault();
			}
			if (display != null && !display.isDisposed()) {
				maxBounds = display.getBounds();
			}
		}

		if (bounds.width > -1 && bounds.height > -1) {
			if (maxBounds != null) {
				bounds.width = Math.min(bounds.width, maxBounds.width);
				bounds.height = Math.min(bounds.height, maxBounds.height);
			}
			// Enforce an absolute minimal size
			bounds.width = Math.max(bounds.width, 30);
			bounds.height = Math.max(bounds.height, 50);
		}

		if (bounds.x > -1 && bounds.y > -1 && maxBounds != null) {
			bounds.x = Math.max(bounds.x, maxBounds.x);
			bounds.y = Math.max(bounds.y, maxBounds.y);

			if (bounds.width > -1 && bounds.height > -1) {
				bounds.x = Math.min(bounds.x, maxBounds.width - bounds.width);
				bounds.y = Math.min(bounds.y, maxBounds.height - bounds.height);
			}
		}
		return bounds;
	}

	// -------------------- all to do with the contents of the view
	// --------------------

	private void setInfoSystemColor() {
		Display display = dialogShell.getDisplay();

		// set the foreground colour
		filterText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		viewMenuButtonComposite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		toolBar.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		statusField.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

		// set the background colour
		filterText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		viewMenuButtonComposite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolBar.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		statusField.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	private void showViewMenu() {
		isDeactivateListenerActive = false;

		Menu aMenu = getViewMenuManager().createContextMenu(dialogShell);

		Rectangle bounds = toolBar.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = dialogShell.toDisplay(topLeft);
		aMenu.setLocation(topLeft.x, topLeft.y);
		aMenu.addMenuListener(new MenuListener() {

			public void menuHidden(MenuEvent e) {
				isDeactivateListenerActive = true;
			}

			public void menuShown(MenuEvent e) {
			}
		});
		aMenu.setVisible(true);
	}

	private void storeBounds() {
		IDialogSettings dialogSettings = getDialogSettings();

		boolean controlRestoresSize = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_SIZE);
		boolean controlRestoresLocation = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_LOCATION);

		if (bounds == null) {
			return;
		}

		if (controlRestoresSize) {
			dialogSettings.put(STORE_SIZE_WIDTH, bounds.width);
			dialogSettings.put(STORE_SIZE_HEIGHT, bounds.height);
		}
		if (controlRestoresLocation) {
			dialogSettings.put(STORE_LOCATION_X, bounds.x);
			dialogSettings.put(STORE_LOCATION_Y, bounds.y);
		}
	}

	public void setSelectedProject(IProject selected) {
		this.selectedProject = selected;
	}
}
