/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.inplace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Item;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelContentProvider;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorLabelProvider;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorSorter;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Inplace variant of the Beans Quick Outline.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings( "restriction" )
public class BeansInplaceOutlineDialog {

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
	private final String sectionName = BeansInplaceOutlineDialog.class.getName();

	/**
	 * Fields for text matching and filtering
	 */
	private Text filterText;

	private StringMatcher stringMatcher;

	private Font statusTextFont;

	private List<Object> filteredElements = new ArrayList<Object>();

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

	/**
	 * Fields which are updated by the IWorkbenchWindowActionDelegate to record
	 * the selection in the editor
	 */
	private IModelElement lastSelection;

	private IWorkbenchPart workbenchPart;

	private IBeansConfig selectedBeansConfig;

	/**
	 * Fields for view toggling support - to show or hide parent crosscutting
	 */
	private final String invokingCommandId = 
		"org.springframework.ide.eclipse.beans.ui.inplace.show"; //$NON-NLS-1$

	private boolean isShowingParent = false;

	private Command invokingCommand;

	private KeyAdapter keyAdapter;

	private TriggerSequence[] invokingCommandTriggerSequences;

	private Label statusField;

	private Action doubleClickAction;

	private boolean isDeactivateListenerActive = false;

	private Composite composite, viewMenuButtonComposite;

	private int shellStyle;

	private Listener deactivateListener;

	private Shell parentShell;

	private Shell dialogShell;

	private TreeViewer viewer;

	/**
	 * Constructor which takes the parent shell
	 */
	public BeansInplaceOutlineDialog(Shell parent) {
		parentShell = parent;
		shellStyle = SWT.RESIZE;
	}

	/**
	 * Open the dialog
	 */
	public void open() {
		// If the dialog is already open, dispose the shell and recreate it
		if (dialogShell != null) {
			close();
		}
		if (invokingCommandId != null) {
			ICommandService commandService = (ICommandService) PlatformUI
					.getWorkbench().getAdapter(ICommandService.class);
			invokingCommand = commandService.getCommand(invokingCommandId);
			if (invokingCommand != null && !invokingCommand.isDefined())
				invokingCommand = null;
			else
				// Pre-fetch key sequence - do not change because scope will
				// change later.
				getInvokingCommandKeySequences();
		}

		createShell();
		createComposites();
		filterText = createFilterText(viewMenuButtonComposite);
		// creates the drop down menu and creates the actions
		createViewMenu(viewMenuButtonComposite);
		createHorizontalSeparator(composite);
		viewer = createTreeViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL);

		createStatusField(composite);
		addListenersToTree(viewer);
		// set the tab order
		viewMenuButtonComposite.setTabList(new Control[] { filterText });
		composite.setTabList(new Control[] { viewMenuButtonComposite,
				viewer.getTree() });

		setInfoSystemColor();
		installFilter();
		addListenersToShell();
		createContents();
		initializeBounds();
		// open the window
		dialogShell.open();
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

	private void createComposites() {
		// Composite for filter text and tree
		composite = new Composite(dialogShell, SWT.RESIZE);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		viewMenuButtonComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewMenuButtonComposite.setLayout(layout);
		viewMenuButtonComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
	}

	private TreeViewer createTreeViewer(Composite parent, int style) {
		viewer = new TreeViewer(parent, SWT.SINGLE | (style & ~SWT.MULTI));
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.setContentProvider(new BeansModelContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

		});
		viewer.setLabelProvider(new BeansNavigatorLabelProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		viewer.setSorter(new BeansNavigatorSorter());

		// adding these filters which restrict the contents of
		// the view according to what has been typed in the
		// text bar
		viewer.addFilter(new NamePatternFilter());

		// add filter from the common navigator
		INavigatorContentService contentService = NavigatorContentServiceFactory.INSTANCE
				.createContentService("org.springframework.ide.eclipse.ui.navigator.springExplorer");
		ViewerFilter[] viewFilters = contentService.getFilterService()
				.getVisibleFilters(false);
		for (ViewerFilter viewFilter : viewFilters) {
			viewer.addFilter(viewFilter);
		}

		return viewer;
	}

	private void createHorizontalSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL
				| SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void setInfoSystemColor() {
		Display display = dialogShell.getDisplay();

		// set the foreground colour
		viewer.getTree().setForeground(
				display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		filterText.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		composite.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		viewMenuButtonComposite.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		toolBar
				.setForeground(display
						.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		statusField.setForeground(display
				.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

		// set the background colour
		viewer.getTree().setBackground(
				display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		filterText.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		composite.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		viewMenuButtonComposite.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolBar
				.setBackground(display
						.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		statusField.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	// --------------------- adding listeners ---------------------------

	private void addListenersToTree(TreeViewer treeViewer) {
		final Tree tree = treeViewer.getTree();
		tree.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == 0x1B) // ESC
					dispose();
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				gotoSelectedElement();
			}
		});

		tree.addMouseMoveListener(new MouseMoveListener() {
			TreeItem fLastItem = null;

			public void mouseMove(MouseEvent e) {
				if (tree.equals(e.getSource())) {
					Object o = tree.getItem(new Point(e.x, e.y));
					if (o instanceof TreeItem) {
						if (!o.equals(fLastItem)) {
							fLastItem = (TreeItem) o;
							tree.setSelection(new TreeItem[] { fLastItem });
						}
						else if (e.y < tree.getItemHeight() / 4) {
							// Scroll up
							Point p = tree.toDisplay(e.x, e.y);
							Item item = viewer.scrollUp(p.x, p.y);
							if (item instanceof TreeItem) {
								fLastItem = (TreeItem) item;
								tree.setSelection(new TreeItem[] { fLastItem });
							}
						}
						else if (e.y > tree.getBounds().height
								- tree.getItemHeight() / 4) {
							// Scroll down
							Point p = tree.toDisplay(e.x, e.y);
							Item item = viewer.scrollDown(p.x, p.y);
							if (item instanceof TreeItem) {
								fLastItem = (TreeItem) item;
								tree.setSelection(new TreeItem[] { fLastItem });
							}
						}
					}
				}
			}
		});

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				if (tree.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (tree.equals(e.getSource())) {
					Object o = tree.getItem(new Point(e.x, e.y));
					TreeItem selection = tree.getSelection()[0];
					if (selection.equals(o)) {
						gotoSelectedElement();
					}
				}
			}
		});

		doubleClickAction = new Action() {
			
			@Override
			public void run() {
				gotoSelectedElement();
			}
		};

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
				if (dialogShell != null && dialogShell.isDisposed()) {
					dispose();
				}
			}
		});

		treeViewer.getTree().addKeyListener(getKeyAdapter());
	}

	private void addListenersToShell() {
		dialogShell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				close();
				if (statusTextFont != null && !statusTextFont.isDisposed())
					statusTextFont.dispose();

				// dialogShell = null;
				viewer = null;
				composite = null;
				filterText = null;
				statusTextFont = null;

			}
		});

		deactivateListener = new Listener() {
			public void handleEvent(Event event) {
				if (isDeactivateListenerActive)
					dispose();
			}
		};

		dialogShell.addListener(SWT.Deactivate, deactivateListener);
		isDeactivateListenerActive = true;
		dialogShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				if (e.widget == dialogShell
						&& dialogShell.getShells().length == 0)
					isDeactivateListenerActive = true;
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

	// --------------------- creating and filling the menu
	// ---------------------------

	private void createViewMenu(Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		ToolItem viewMenuButton = new ToolItem(toolBar, SWT.PUSH, 0);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		data.verticalAlignment = GridData.BEGINNING;
		toolBar.setLayoutData(data);

		viewMenuButton.setImage(JavaPluginImages
				.get(JavaPluginImages.IMG_ELCL_VIEW_MENU));
		viewMenuButton.setDisabledImage(JavaPluginImages
				.get(JavaPluginImages.IMG_DLCL_VIEW_MENU));
		viewMenuButton.setToolTipText("Menu");
		viewMenuButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});
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
			}});
		aMenu.setVisible(true);
	}

	private MenuManager getViewMenuManager() {
		if (viewMenuManager == null) {
			viewMenuManager = new MenuManager();
			fillViewMenu(viewMenuManager);
		}
		return viewMenuManager;
	}

	private void fillViewMenu(IMenuManager viewMenu) {
		viewMenu.add(new GroupMarker("SystemMenuStart")); //$NON-NLS-1$
		viewMenu.add(new MoveAction());
		viewMenu.add(new ResizeAction());
		viewMenu.add(new RememberBoundsAction());
		viewMenu.add(new Separator("SystemMenuEnd")); //$NON-NLS-1$
	}

	// --------------------- creating and filling the status field
	// ---------------------------

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
		statusField.setText(getStatusFieldText());
		Font font = statusField.getFont();
		Display display = parent.getDisplay();
		FontData[] fontDatas = font.getFontData();
		for (FontData element : fontDatas)
			element.setHeight(element.getHeight() * 9 / 10);
		Font statusTextFont = new Font(display, fontDatas);
		statusField.setFont(statusTextFont);
	}

	private String getStatusFieldText() {
		TriggerSequence[] sequences = getInvokingCommandKeySequences();
		if (sequences == null || sequences.length == 0)
			return ""; //$NON-NLS-1$

		String keySequence = sequences[0].format();

		if (isShowingParent)
			return NLS
					.bind(
							"Press \'\'{0}\'\' to hide beans of other files", keySequence); //$NON-NLS-1$
		return NLS.bind(
				"Press \'\'{0}\'\' to show beans of other files", keySequence); //$NON-NLS-1$
	}

	private TriggerSequence[] getInvokingCommandKeySequences() {
		IBindingService bindingService = (IBindingService) PlatformUI
				.getWorkbench().getAdapter(IBindingService.class);
		TriggerSequence[] bindings = bindingService
				.getActiveBindingsFor(invokingCommandId);
		if (bindings.length > 0) {
			invokingCommandTriggerSequences = bindings;
		}
		return invokingCommandTriggerSequences;
	}

	private KeyAdapter getKeyAdapter() {
		if (keyAdapter == null) {
			keyAdapter = new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int accelerator = SWTKeySupport
							.convertEventToUnmodifiedAccelerator(e);
					KeySequence keySequence = KeySequence
							.getInstance(SWTKeySupport
									.convertAcceleratorToKeyStroke(accelerator));
					TriggerSequence[] sequences = getInvokingCommandKeySequences();
					if (sequences == null)
						return;
					for (TriggerSequence element : sequences) {
						if (element.equals(keySequence)) {
							e.doit = false;
							toggleShowParent();
							return;
						}
					}
				}
			};
		}
		return keyAdapter;
	}

	public void refresh() {
		if (lastSelection != null) {
			Object element = null;
			if (!isShowingParent) {
				element = this.selectedBeansConfig;
			}
			else {
				element = this.selectedBeansConfig.getElementParent();
			}
			if (element != null) {
				viewer.getControl().setRedraw(false);
				viewer.setInput(element);
				viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
				viewer.setSelection(
						new StructuredSelection(this.lastSelection), true);
				viewer.reveal(this.lastSelection);
				viewer.getControl().setRedraw(true);
			}
		}
	}

	protected void toggleShowParent() {
		if (lastSelection != null && workbenchPart != null) {
			Object element = null;
			if (isShowingParent) {
				element = this.selectedBeansConfig;
			}
			else {
				element = BeansModelUtils.getParentOfClass(this.selectedBeansConfig, IBeansProject.class);
			}
			if (element != null) {
				viewer.getControl().setRedraw(false);
				viewer.setInput(element);
				viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
				viewer.setSelection(
						new StructuredSelection(this.lastSelection), true);
				viewer.reveal(this.lastSelection);
				viewer.getControl().setRedraw(true);
			}
		}

		isShowingParent = !isShowingParent;
		updateStatusFieldText();
	}

	protected void updateStatusFieldText() {
		if (statusField != null)
			statusField.setText(getStatusFieldText());
	}

	// ----------- all to do with setting the bounds of the dialog -------------

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

	public Rectangle getDefaultBounds() {
		GC gc = new GC(composite);
		gc.setFont(composite.getFont());
		int width = gc.getFontMetrics().getAverageCharWidth();
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();

		Point size = new Point(60 * width, 10 * height);
		Point location = getDefaultLocation(size);
		return new Rectangle(location.x, location.y, size.x, size.y);
	}

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

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(
				monitorBounds.y, Math.min(centerPoint.y
						- (initialSize.y * 2 / 3), monitorBounds.y
						+ monitorBounds.height - initialSize.y)));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = BeansUIPlugin.getDefault()
				.getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings = BeansUIPlugin.getDefault().getDialogSettings()
					.addNewSection(sectionName);

		return settings;
	}

	private void storeBounds() {
		IDialogSettings dialogSettings = getDialogSettings();

		boolean controlRestoresSize = !dialogSettings
				.getBoolean(STORE_DISABLE_RESTORE_SIZE);
		boolean controlRestoresLocation = !dialogSettings
				.getBoolean(STORE_DISABLE_RESTORE_LOCATION);

		if (bounds == null)
			return;

		if (controlRestoresSize) {
			dialogSettings.put(STORE_SIZE_WIDTH, bounds.width);
			dialogSettings.put(STORE_SIZE_HEIGHT, bounds.height);
		}
		if (controlRestoresLocation) {
			dialogSettings.put(STORE_LOCATION_X, bounds.x);
			dialogSettings.put(STORE_LOCATION_Y, bounds.y);
		}
	}

	private Rectangle restoreBounds() {

		IDialogSettings dialogSettings = getDialogSettings();

		boolean controlRestoresSize = !dialogSettings
				.getBoolean(STORE_DISABLE_RESTORE_SIZE);
		boolean controlRestoresLocation = !dialogSettings
				.getBoolean(STORE_DISABLE_RESTORE_LOCATION);

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
		if (bounds.x == -1 && bounds.y == -1 && bounds.width == -1
				&& bounds.height == -1) {
			return null;
		}

		Rectangle maxBounds = null;
		if (dialogShell != null && !dialogShell.isDisposed())
			maxBounds = dialogShell.getDisplay().getBounds();
		else {
			// fallback
			Display display = Display.getCurrent();
			if (display == null)
				display = Display.getDefault();
			if (display != null && !display.isDisposed())
				maxBounds = display.getBounds();
		}

		if (bounds.width > -1 && bounds.height > -1) {
			if (maxBounds != null) {
				bounds.width = Math.min(bounds.width, maxBounds.width);
				bounds.height = Math.min(bounds.height, maxBounds.height);
			}
			// Enforce an absolute minimal size
			bounds.width = Math.max(bounds.width, 30);
			bounds.height = Math.max(bounds.height, 30);
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

	// ----------- all to do with filtering text

	private Text createFilterText(Composite parent) {
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

		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) // return
					gotoSelectedElement();
				if (e.keyCode == SWT.ARROW_DOWN)
					viewer.getTree().setFocus();
				if (e.keyCode == SWT.ARROW_UP)
					viewer.getTree().setFocus();
				if (e.character == 0x1B) // ESC
					dispose();
			}

			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		filterText.addKeyListener(getKeyAdapter());
		return filterText;
	}

	private void gotoSelectedElement() {
		Object selectedElement = getSelectedElement();
		if (selectedElement instanceof IResourceModelElement) {
			BeansUIUtils.openInEditor((IResourceModelElement) selectedElement, 
					true);
		}
		else if (selectedElement instanceof IFile) {
			SpringUIUtils.openInEditor((IFile) selectedElement, -1, true);
		}
		dispose();
	}

	private Object getSelectedElement() {
		if (viewer == null)
			return null;
		return ((IStructuredSelection) viewer.getSelection()).getFirstElement();
	}

	private void installFilter() {
		filterText.setText(""); //$NON-NLS-1$

		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = ((Text) e.widget).getText();
				int length = text.length();
				if (length > 0 && text.charAt(length - 1) != '*') {
					text = text + '*';
				}
				setMatcherString(text);
			}
		});
	}

	private void setMatcherString(String pattern) {
		if (pattern.length() == 0) {
			stringMatcher = null;
		}
		else {
			boolean ignoreCase = pattern.toLowerCase().equals(pattern);
			stringMatcher = new StringMatcher(pattern, ignoreCase, false);
		}
		stringMatcherUpdated();
	}

	private void stringMatcherUpdated() {
		filteredElements.clear();
		// refresh viewer to refilter
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
		selectFirstMatch();
		viewer.getControl().setRedraw(true);
	}

	private void selectFirstMatch() {
		Tree tree = viewer.getTree();
		Object element = findElement(tree.getItems());
		if (element != null) {
			viewer.setSelection(new StructuredSelection(element), true);
		}
		else {
			TreeItem[] items = tree.getItems();
			if (items != null && items.length > 0) {
				Object wr = items[0].getData();
				viewer.setSelection(new StructuredSelection(wr));
			}
		}
	}

	private Object findElement(TreeItem[] items) {
		ILabelProvider labelProvider = (ILabelProvider) viewer
				.getLabelProvider();
		for (TreeItem element0 : items) {
			Object o = element0.getData();
			Object element = null;
			if (o instanceof IBeansModelElement) {
				element = o;
			}
			if (stringMatcher == null)
				return null;

			if (element != null) {
				String label = labelProvider.getText(element);
				if (stringMatcher.match(label))
					return o;
			}
			o = findElement(element0.getItems());
			if (o != null)
				return o;
		}
		return null;
	}

	protected class NamePatternFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			StringMatcher matcher = getMatcher();
			if (matcher == null || !(viewer instanceof TreeViewer))
				return true;
			TreeViewer treeViewer = (TreeViewer) viewer;

			String matchName = ((ILabelProvider) treeViewer.getLabelProvider())
					.getText(element);
			if (matchName != null && matcher.match(matchName)) {
				if (element instanceof IBeansModelElement || element instanceof IFile) {
					filteredElements.add(element);
				}
				return true;
			}
			return hasUnfilteredChild(treeViewer, element);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if (element instanceof IBeansModelElement || element instanceof IFile) {
				Object[] children = ((ITreeContentProvider) viewer
						.getContentProvider()).getChildren(element);
				for (Object element0 : children) {
					if (select(viewer, element, element0)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
			int size = elements.length;
			List<Object> out = new ArrayList<Object>(size);
			for (int i = 0; i < size; ++i) {
				Object element = elements[i];
				if (filteredElements.contains(parent)) {
					if (element instanceof IBeansModelElement) {
						filteredElements.add(element);
					}
					out.add(element);
				}
				else if (filteredElements.contains(element)) {
					out.add(element);
				}
				else if (select(viewer, parent, element)) {
					out.add(element);
				}
			}
			return out.toArray();
		}
	}

	private StringMatcher getMatcher() {
		return stringMatcher;
	}

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
			if (borderSize < 0)
				throw new IllegalArgumentException();
			fBorderSize = borderSize;
		}

		/**
		 * Returns the border size.
		 */
		public int getBorderSize() {
			return fBorderSize;
		}

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {

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
			Point minSize = new Point(composite.getClientArea().width,
					composite.getClientArea().height);

			if (children != null) {
				for (Control child : children) {
					child.setSize(minSize.x - fBorderSize * 2, minSize.y
							- fBorderSize * 2);
					child.setLocation(fBorderSize, fBorderSize);
				}
			}
		}
	}

	// ---------- shuts down the dialog ---------------

	/**
	 * Close the dialog
	 */
	public void close() {
		storeBounds();
		toolBar = null;
		viewMenuManager = null;
	}

	public void dispose() {
		filterText = null;
		if (dialogShell != null) {
			if (!dialogShell.isDisposed())
				dialogShell.dispose();
			dialogShell = null;
			parentShell = null;
			viewer = null;
			composite = null;
		}
	}

	// ------------------ moving actions --------------------------

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
			setChecked(!getDialogSettings().getBoolean(
					STORE_DISABLE_RESTORE_LOCATION));
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

	// -------------------- all to do with the contents of the view
	// --------------------

	private void createContents() {
		if (lastSelection != null && workbenchPart != null) {
			IEditorInput input = SpringUIPlugin.getActiveWorkbenchPage()
					.getActiveEditor().getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				if (BeansCoreUtils.isBeansConfig(file, true)) {
					this.selectedBeansConfig = BeansCorePlugin.getModel()
							.getConfig(BeansConfigId.create(file), true);
					viewer.setInput(this.selectedBeansConfig);
				}
			}
		}
		filterText.setText("");
		refresh();
	}

	/**
	 * @param lastSelection The lastSelection to set.
	 */
	public void setLastSelection(ISelection lastSelection) {
		if (lastSelection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) lastSelection)
					.getFirstElement();
			IEditorInput input = SpringUIPlugin.getActiveWorkbenchPage()
					.getActiveEditor().getEditorInput();
			IFile file = null;
			if (input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			}
			if (obj instanceof IDOMNode && file != null) {
				IDOMNode node = (IDOMNode) obj;
				int startLine = ((IDOMDocument) node.getOwnerDocument())
						.getStructuredDocument().getLineOfOffset(
								node.getStartOffset()) + 1;
				int endLine = ((IDOMDocument) node.getOwnerDocument())
						.getStructuredDocument().getLineOfOffset(
								node.getEndOffset()) + 1;

				IModelElement mostspecificElement = BeansModelUtils
						.getMostSpecificModelElement(startLine, endLine, file,
								null);
				if (mostspecificElement != null) {
					this.lastSelection = mostspecificElement;
				}
				else {
					this.lastSelection = BeansCorePlugin.getModel().getConfig(BeansConfigId.create(file));
				}
			}
		}
	}

	/**
	 * @param workbenchPart The workbenchPart to set.
	 */
	public void setWorkbenchPart(IWorkbenchPart workbenchPart) {
		this.workbenchPart = workbenchPart;
	}

	public boolean isOpen() {
		return dialogShell != null;
	}
}
