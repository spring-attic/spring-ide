/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.aop.ui.inplace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.jdt.internal.ui.JavaPlugin;
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
import org.eclipse.jface.commands.ActionHandler;
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
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.NavigatorContentServiceFactory;
import org.springframework.ide.eclipse.aop.ui.Activator;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigatorContentProvider;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigatorLabelProvider;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorSorter;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;

/**
 * Inplace variant of the Beans Cross References View.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings( { "deprecation", "restriction" })
public class AopReferenceModelInplaceDialog {

	/**
	 * Dialog constants telling whether this control can be resized or move.
	 */
	public static final String STORE_DISABLE_RESTORE_SIZE = "DISABLE_RESTORE_SIZE"; //$NON-NLS-1$

	public static final String STORE_DISABLE_RESTORE_LOCATION = "DISABLE_RESTORE_LOCATION"; //$NON-NLS-1$

	/**
	 * Dialog store constant for the location's x-coordinate, location's y-coordinate and the size's
	 * width and height.
	 */
	private static final String STORE_LOCATION_X = "location.x"; //$NON-NLS-1$

	private static final String STORE_LOCATION_Y = "location.y"; //$NON-NLS-1$

	private static final String STORE_SIZE_WIDTH = "size.width"; //$NON-NLS-1$

	private static final String STORE_SIZE_HEIGHT = "size.height"; //$NON-NLS-1$

	/**
	 * The name of the dialog store's section associated with the inplace XReference view.
	 */
	private final String sectionName = "org.springframework.ide.eclipse.aop.ui.inplace.Inplace"; //$NON-NLS-1$

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
	 * Fields which are updated by the IWorkbenchWindowActionDelegate to record the selection in the
	 * editor
	 */
	private ISelection lastSelection;

	private IWorkbenchPart workbenchPart;

	/**
	 * Fields for view toggling support - to show or hide parent crosscutting
	 */
	private final String invokingCommandId = "org.springframework.ide.eclipse.aop.ui.inplace.show"; //$NON-NLS-1$

	private boolean isShowingParentCrosscutting = false;

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

	private IKeyBindingService fKeyBindingService;

	private String[] fKeyBindingScopes;

	private IAction fShowViewMenuAction;

	private IHandlerActivation handlerActivation;

	/**
	 * For testing purposes need to be able to get hold of the XReferenceInplaceDialog instance
	 */
	public static AopReferenceModelInplaceDialog dialog;

	/**
	 * Constructor which takes the parent shell
	 */
	public AopReferenceModelInplaceDialog(Shell parent) {
		parentShell = parent;
		shellStyle = SWT.RESIZE;
		dialog = this;
	}

	/**
	 * Open the dialog
	 */
	public void open() {
		// If the dialog is already open, dispose the shell and recreate it
		if (dialogShell != null) {
			close();
		}
		// set isInplace to true after calling close
		// XReferenceProviderManager.getManager().setIsInplace(true);

		if (invokingCommandId != null) {
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getAdapter(ICommandService.class);
			invokingCommand = commandService.getCommand(invokingCommandId);
			if (invokingCommand != null && !invokingCommand.isDefined())
				invokingCommand = null;
			else
				// Pre-fetch key sequence - do not change because scope will
				// change later.
				getInvokingCommandKeySequences();
		}

		createShell();
		// bug 102140 - need to pass the action the shell of the inplace
		// view so that focus is returned to the inplace view when the
		// filter dialog is closed.
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
		composite.setTabList(new Control[] { viewMenuButtonComposite, viewer.getTree() });

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
		viewMenuButtonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private TreeViewer createTreeViewer(Composite parent, int style) {
		viewer = new TreeViewer(parent, SWT.SINGLE | (style & ~SWT.MULTI));
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer.setContentProvider(new AopReferenceModelNavigatorContentProvider());
		viewer.setLabelProvider(new AopReferenceModelNavigatorLabelProvider());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		viewer.setSorter(new AopReferenceModelNavigatorSorter());

		// adding these filters which restrict the contents of
		// the view according to what has been typed in the
		// text bar
		viewer.addFilter(new NamePatternFilter());

		// add filter from the common navigator
		INavigatorContentService contentService = NavigatorContentServiceFactory.INSTANCE
				.createContentService(AopReferenceModelNavigator.ID);
		ViewerFilter[] viewFilters = contentService.getFilterService().getVisibleFilters(true);
		for (ViewerFilter viewFilter : viewFilters) {
			viewer.addFilter(viewFilter);
		}

		return viewer;
	}

	private void createHorizontalSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void setInfoSystemColor() {
		Display display = dialogShell.getDisplay();

		// set the foreground colour
		viewer.getTree().setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		filterText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		viewMenuButtonComposite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		toolBar.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		statusField.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

		// set the background colour
		viewer.getTree().setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		filterText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		viewMenuButtonComposite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolBar.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		statusField.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
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
						} else if (e.y < tree.getItemHeight() / 4) {
							// Scroll up
							Point p = tree.toDisplay(e.x, e.y);
							Item item = viewer.scrollUp(p.x, p.y);
							if (item instanceof TreeItem) {
								fLastItem = (TreeItem) item;
								tree.setSelection(new TreeItem[] { fLastItem });
							}
						} else if (e.y > tree.getBounds().height - tree.getItemHeight() / 4) {
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

		doubleClickAction = new OpenRevealableReferenceNodeAction(dialogShell, treeViewer);

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

				dialogShell = null;
				viewer = null;
				composite = null;
				filterText = null;
				statusTextFont = null;

			}
		});

		deactivateListener = new Listener() {
			/*
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				if (isDeactivateListenerActive)
					dispose();
			}
		};

		dialogShell.addListener(SWT.Deactivate, deactivateListener);
		isDeactivateListenerActive = true;
		dialogShell.addShellListener(new ShellAdapter() {
			/*
			 * @see org.eclipse.swt.events.ShellAdapter#shellActivated(org.eclipse.swt.events.ShellEvent)
			 */
			public void shellActivated(ShellEvent e) {
				if (e.widget == dialogShell && dialogShell.getShells().length == 0)
					isDeactivateListenerActive = true;
			}
		});

		dialogShell.addControlListener(new ControlAdapter() {
			/**
			 * {@inheritDoc}
			 */
			public void controlMoved(ControlEvent e) {
				bounds = dialogShell.getBounds();
				if (trim != null) {
					Point location = composite.getLocation();
					bounds.x = bounds.x - trim.x + location.x;
					bounds.y = bounds.y - trim.y + location.y;
				}

			}

			/**
			 * {@inheritDoc}
			 */
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

		viewMenuButton.setImage(JavaPluginImages.get(JavaPluginImages.IMG_ELCL_VIEW_MENU));
		viewMenuButton.setDisabledImage(JavaPluginImages.get(JavaPluginImages.IMG_DLCL_VIEW_MENU));
		viewMenuButton.setToolTipText("Menu");

		// Used to enable the menu to be accessed from the keyboard
		// Key binding service
		IWorkbenchPart part = JavaPlugin.getActivePage().getActivePart();
		IWorkbenchPartSite site = part.getSite();
		fKeyBindingService = site.getKeyBindingService();

		// Remember current scope and then set window context.
		fKeyBindingScopes = fKeyBindingService.getScopes();
		fKeyBindingService.setScopes(new String[] { IContextService.CONTEXT_ID_WINDOW });

		// Create show view menu action
		fShowViewMenuAction = new Action("showViewMenu") { //$NON-NLS-1$
			/*
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				showViewMenu();
			}
		};
		fShowViewMenuAction.setEnabled(true);
		fShowViewMenuAction.setActionDefinitionId("org.eclipse.ui.window.showViewMenu"); //$NON-NLS-1$

		// Register action with handler service
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getAdapter(
				IHandlerService.class);
		handlerActivation = handlerService.activateHandler(fShowViewMenuAction
				.getActionDefinitionId(), new ActionHandler(fShowViewMenuAction));

		viewMenuButton.addSelectionListener(new SelectionAdapter() {
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
		for (int i = 0; i < fontDatas.length; i++)
			fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
		Font statusTextFont = new Font(display, fontDatas);
		statusField.setFont(statusTextFont);
	}

	private String getStatusFieldText() {
		TriggerSequence[] sequences = getInvokingCommandKeySequences();
		if (sequences == null || sequences.length == 0)
			return ""; //$NON-NLS-1$

		String keySequence = sequences[0].format();

		if (isShowingParentCrosscutting)
			return NLS.bind(
					"Press \'\'{0}\'\' to hide cross references for entire file", keySequence); //$NON-NLS-1$
		return NLS.bind("Press \'\'{0}\'\' to show cross references for entire file", keySequence); //$NON-NLS-1$
	}

	private TriggerSequence[] getInvokingCommandKeySequences() {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(
				IBindingService.class);
		TriggerSequence[] bindings = bindingService.getActiveBindingsFor(invokingCommandId);
		if (bindings.length > 0) {
			invokingCommandTriggerSequences = bindings;
		}
		return invokingCommandTriggerSequences;
	}

	private KeyAdapter getKeyAdapter() {
		if (keyAdapter == null) {
			keyAdapter = new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
					KeySequence keySequence = KeySequence.getInstance(SWTKeySupport
							.convertAcceleratorToKeyStroke(accelerator));
					TriggerSequence[] sequences = getInvokingCommandKeySequences();
					if (sequences == null)
						return;
					for (int i = 0; i < sequences.length; i++) {
						if (sequences[i].equals(keySequence)) {
							e.doit = false;
							toggleShowParentCrosscutting();
							return;
						}
					}
				}
			};
		}
		return keyAdapter;
	}

	public void refresh() {
		if (lastSelection != null && workbenchPart != null) {
			Object element = null;
			if (!isShowingParentCrosscutting) {
				element = AopReferenceModelNavigator.calculateRootElement(
						AopReferenceModelNavigatorUtils.getSelectedElement(workbenchPart,
								lastSelection), true);
			} else {
				element = AopReferenceModelNavigator.calculateRootElement(
						AopReferenceModelNavigatorUtils.getSelectedElement(workbenchPart,
								lastSelection), false);
			}
			if (element != null) {
				viewer.getControl().setRedraw(false);
				viewer.setInput(element);
				viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
				AopReferenceModelNavigator.expandTree(viewer.getTree().getItems(), false);
				AopReferenceModelNavigator.revealSelection(viewer, AopReferenceModelNavigatorUtils
						.getSelectedElement(workbenchPart, lastSelection),
						!isShowingParentCrosscutting);
				viewer.getControl().setRedraw(true);
			}
		}
	}

	protected void toggleShowParentCrosscutting() {
		if (lastSelection != null && workbenchPart != null) {
			Object element = null;
			if (!isShowingParentCrosscutting) {
				element = AopReferenceModelNavigator.calculateRootElement(
						AopReferenceModelNavigatorUtils.getSelectedElement(workbenchPart,
								lastSelection), true);
			} else {
				element = AopReferenceModelNavigator.calculateRootElement(
						AopReferenceModelNavigatorUtils.getSelectedElement(workbenchPart,
								lastSelection), false);
			}
			if (element != null) {
				viewer.getControl().setRedraw(false);
				viewer.setInput(element);
				viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);
				AopReferenceModelNavigator.expandTree(viewer.getTree().getItems(), false);
				AopReferenceModelNavigator.revealSelection(viewer, AopReferenceModelNavigatorUtils
						.getSelectedElement(workbenchPart, lastSelection),
						!isShowingParentCrosscutting);
				viewer.getControl().setRedraw(true);
			}
		}

		isShowingParentCrosscutting = !isShowingParentCrosscutting;
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
		} else {
			centerPoint = Geometry.centerPoint(monitorBounds);
		}

		return new Point(centerPoint.x - (initialSize.x / 2), Math.max(monitorBounds.y, Math.min(
				centerPoint.y - (initialSize.y * 2 / 3), monitorBounds.y + monitorBounds.height
						- initialSize.y)));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(
				sectionName);
		if (settings == null)
			settings = Activator.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}

	private void storeBounds() {
		IDialogSettings dialogSettings = getDialogSettings();

		boolean controlRestoresSize = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_SIZE);
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

		boolean controlRestoresSize = !dialogSettings.getBoolean(STORE_DISABLE_RESTORE_SIZE);
		boolean controlRestoresLocation = !dialogSettings
				.getBoolean(STORE_DISABLE_RESTORE_LOCATION);

		Rectangle bounds = new Rectangle(-1, -1, -1, -1);

		if (controlRestoresSize) {
			try {
				bounds.width = dialogSettings.getInt(STORE_SIZE_WIDTH);
				bounds.height = dialogSettings.getInt(STORE_SIZE_HEIGHT);
			} catch (NumberFormatException ex) {
				bounds.width = -1;
				bounds.height = -1;
			}
		}

		if (controlRestoresLocation) {
			try {
				bounds.x = dialogSettings.getInt(STORE_LOCATION_X);
				bounds.y = dialogSettings.getInt(STORE_LOCATION_Y);
			} catch (NumberFormatException ex) {
				bounds.x = -1;
				bounds.y = -1;
			}
		}

		// sanity check
		if (bounds.x == -1 && bounds.y == -1 && bounds.width == -1 && bounds.height == -1) {
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
		if (selectedElement instanceof IRevealableReferenceNode) {
			((IRevealableReferenceNode) selectedElement).openAndReveal();
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
				if (length > 0 && text.charAt(length - 1) != '*') {//$NON-NLS-1$
					text = text + '*';//$NON-NLS-1$
				}
				setMatcherString(text);
			}
		});
	}

	private void setMatcherString(String pattern) {
		if (pattern.length() == 0) {
			stringMatcher = null;
		} else {
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
		AopReferenceModelNavigator.expandTree(viewer.getTree().getItems(), false);
		selectFirstMatch();
		viewer.getControl().setRedraw(true);
	}

	private void selectFirstMatch() {
		Tree tree = viewer.getTree();
		Object element = findElement(tree.getItems());
		if (element != null) {
			viewer.setSelection(new StructuredSelection(element), true);
		} else {
			TreeItem[] items = tree.getItems();
			if (items != null && items.length > 0) {
				Object wr = items[0].getData();
				viewer.setSelection(new StructuredSelection(wr));
			}
		}
	}

	@SuppressWarnings("restriction")
	private Object findElement(TreeItem[] items) {
		ILabelProvider labelProvider = (ILabelProvider) viewer.getLabelProvider();
		for (int i = 0; i < items.length; i++) {
			Object o = items[i].getData();
			Object element = null;
			if (o instanceof IReferenceNode) {
				element = o;
			}
			if (stringMatcher == null)
				return null;

			if (element != null) {
				String label = labelProvider.getText(element);
				if (stringMatcher.match(label))
					return o;
			}
			o = findElement(items[i].getItems());
			if (o != null)
				return o;
		}
		return null;
	}

	protected class NamePatternFilter extends ViewerFilter {

		public NamePatternFilter() {
		}

		/*
		 * (non-Javadoc) Method declared on ViewerFilter.
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			StringMatcher matcher = getMatcher();
			if (matcher == null || !(viewer instanceof TreeViewer))
				return true;
			TreeViewer treeViewer = (TreeViewer) viewer;

			String matchName = ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
			if (matchName != null && matcher.match(matchName)) {
				if (element instanceof IReferenceNode) {
					filteredElements.add(element);
				}
				return true;
			}
			return hasUnfilteredChild(treeViewer, element);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			if (element instanceof IReferenceNode) {
				Object[] children = ((ITreeContentProvider) viewer.getContentProvider())
						.getChildren(element);
				for (int i = 0; i < children.length; i++) {
					if (select(viewer, element, children[i])) {
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc) Method declared on ViewerFilter.
		 */
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
			int size = elements.length;
			List<Object> out = new ArrayList<Object>(size);
			for (int i = 0; i < size; ++i) {
				Object element = elements[i];
				if (filteredElements.contains(parent)) {
					if (element instanceof IReferenceNode) {
						filteredElements.add(element);
					}
					out.add(element);
				} else if (filteredElements.contains(element)) {
					out.add(element);
				} else if (select(viewer, parent, element)) {
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
	 * Static inner class which sets the layout for the inplace view. Without this, the inplace view
	 * will not be populated.
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

		/*
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int,
		 *      int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {

			Control[] children = composite.getChildren();
			Point minSize = new Point(0, 0);

			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					Point size = children[i].computeSize(wHint, hHint, flushCache);
					minSize.x = Math.max(minSize.x, size.x);
					minSize.y = Math.max(minSize.y, size.y);
				}
			}

			minSize.x += fBorderSize * 2 + 3;
			minSize.y += fBorderSize * 2;

			return minSize;
		}

		/*
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {

			Control[] children = composite.getChildren();
			Point minSize = new Point(composite.getClientArea().width,
					composite.getClientArea().height);

			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					child.setSize(minSize.x - fBorderSize * 2, minSize.y - fBorderSize * 2);
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
			dialog = null;
		}
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getAdapter(
				IHandlerService.class);
		handlerService.deactivateHandler(handlerActivation);

		// Restore editor's key binding scope
		if (fKeyBindingScopes != null && fKeyBindingService != null) {
			fKeyBindingService.setScopes(fKeyBindingScopes);
			fKeyBindingScopes = null;
			fKeyBindingService = null;
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IAction#run()
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IAction#run()
		 */
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

		/*
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			performTrackerAction(SWT.RESIZE);
			isDeactivateListenerActive = true;
		}

	}

	/**
	 * Perform the requested tracker action (resize or move).
	 * 
	 * @param style
	 *            The track style (resize or move).
	 */
	private void performTrackerAction(int style) {
		Tracker tracker = new Tracker(dialogShell.getDisplay(), style);
		tracker.setStippled(true);
		Rectangle[] r = new Rectangle[] { dialogShell.getBounds() };
		tracker.setRectangles(r);

		if (tracker.open()) {
			dialogShell.setBounds(tracker.getRectangles()[0]);
		}
	}

	// -------------------- all to do with the contents of the view
	// --------------------

	private void createContents() {
		if (lastSelection != null && workbenchPart != null) {
			Object element = AopReferenceModelNavigator.calculateRootElement(
					AopReferenceModelNavigatorUtils
							.getSelectedElement(workbenchPart, lastSelection),
					isShowingParentCrosscutting);
			viewer.setInput(element);
		}
		filterText.setText("");
	}

	/**
	 * @param lastSelection
	 *            The lastSelection to set.
	 */
	public void setLastSelection(ISelection lastSelection) {
		this.lastSelection = lastSelection;
	}

	/**
	 * @param workbenchPart
	 *            The workbenchPart to set.
	 */
	public void setWorkbenchPart(IWorkbenchPart workbenchPart) {
		this.workbenchPart = workbenchPart;
	}

	public boolean isOpen() {
		return dialogShell != null;
	}
}
