package org.springframework.ide.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

@SuppressWarnings("restriction")
public class FilteredElementTreeSelectionDialog extends SelectionStatusDialog {

	private TreeViewer fViewer;

    private ILabelProvider fLabelProvider;

    private ITreeContentProvider fContentProvider;

    private ISelectionStatusValidator fValidator = null;

    private ViewerSorter fSorter;

    private boolean fAllowMultiple = true;

    private boolean fDoubleClickSelects = true;

    private String fEmptyListMessage = WorkbenchMessages.ElementTreeSelectionDialog_nothing_available;

    private IStatus fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
            IStatus.OK, "", null); //$NON-NLS-1$

    private List<ViewerFilter> fFilters;

    private Object fInput;

    private boolean fIsEmpty;

    private int fWidth = 60;

    private int fHeight = 18;

    /**
     * Constructs an instance of <code>ElementTreeSelectionDialog</code>.
     * @param parent The parent shell for the dialog
     * @param labelProvider   the label provider to render the entries
     * @param contentProvider the content provider to evaluate the tree structure
     */
    public FilteredElementTreeSelectionDialog(Shell parent,
            ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
        super(parent);

        fLabelProvider = labelProvider;
        fContentProvider = contentProvider;

        setResult(new ArrayList(0));
        setStatusLineAboveButtons(true);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);
    }

    /**
     * Sets the initial selection.
     * Convenience method.
     * @param selection the initial selection.
     */
    public void setInitialSelection(Object selection) {
        setInitialSelections(new Object[] { selection });
    }

    /**
     * Sets the message to be displayed if the list is empty.
     * @param message the message to be displayed.
     */
    public void setEmptyListMessage(String message) {
        fEmptyListMessage = message;
    }

    /**
     * Specifies if multiple selection is allowed.
     * @param allowMultiple
     */
    public void setAllowMultiple(boolean allowMultiple) {
        fAllowMultiple = allowMultiple;
    }

    /**
     * Specifies if default selected events (double click) are created.
     * @param doubleClickSelects
     */
    public void setDoubleClickSelects(boolean doubleClickSelects) {
        fDoubleClickSelects = doubleClickSelects;
    }

    /**
     * Sets the sorter used by the tree viewer.
     * @param sorter
     */
    public void setSorter(ViewerSorter sorter) {
        fSorter = sorter;
    }

    /**
     * Adds a filter to the tree viewer.
     * @param filter a filter.
     */
    public void addFilter(ViewerFilter filter) {
        if (fFilters == null) {
			fFilters = new ArrayList<ViewerFilter>(4);
		}

        fFilters.add(filter);
    }

    /**
     * Sets an optional validator to check if the selection is valid.
     * The validator is invoked whenever the selection changes.
     * @param validator the validator to validate the selection.
     */
    public void setValidator(ISelectionStatusValidator validator) {
        fValidator = validator;
    }

    /**
     * Sets the tree input.
     * @param input the tree input.
     */
    public void setInput(Object input) {
        fInput = input;
    }

    /**
     * Sets the size of the tree in unit of characters.
     * @param width  the width of the tree.
     * @param height the height of the tree.
     */
    public void setSize(int width, int height) {
        fWidth = width;
        fHeight = height;
    }

    /**
     * Validate the receiver and update the ok status.
     */
    protected void updateOKStatus() {
        if (!fIsEmpty) {
            if (fValidator != null) {
                fCurrStatus = fValidator.validate(getResult());
                updateStatus(fCurrStatus);
            } else {
                fCurrStatus = new Status(IStatus.OK, PlatformUI.PLUGIN_ID,
                        IStatus.OK, "", //$NON-NLS-1$
                        null);
            }
        } else {
            fCurrStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
                    IStatus.ERROR, fEmptyListMessage, null);
        }
        updateStatus(fCurrStatus);
    }

    @Override
	public int open() {
        fIsEmpty = evaluateIfTreeEmpty(fInput);
        super.open();
        return getReturnCode();
    }

    private void access$superCreate() {
        super.create();
    }

    /**
     * Handles cancel button pressed event.
     */
    @Override
	protected void cancelPressed() {
        setResult(null);
        super.cancelPressed();
    }

    /*
     * @see SelectionStatusDialog#computeResult()
     */
    @Override
	protected void computeResult() {
        setResult(((IStructuredSelection) fViewer.getSelection()).toList());
    }

    @Override
	public void create() {
        BusyIndicator.showWhile(null, new Runnable() {
            public void run() {
                access$superCreate();
                fViewer.setSelection(new StructuredSelection(
                        getInitialElementSelections()), true);
                updateOKStatus();
            }
        });
    }

    @Override
	protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label messageLabel = createMessageArea(composite);
        TreeViewer treeViewer = createTreeViewer(composite);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(fWidth);
        data.heightHint = convertHeightInCharsToPixels(fHeight);

        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());

        if (fIsEmpty) {
            messageLabel.setEnabled(false);
            treeWidget.setEnabled(false);
        }

        return composite;
    }

    /**
     * Creates the tree viewer.
     * 
     * @param parent the parent composite
     * @return the tree viewer
     */
    protected TreeViewer createTreeViewer(Composite parent) {
        int style = SWT.BORDER | (fAllowMultiple ? SWT.MULTI : SWT.SINGLE);
		FilteredTree filteredTree = new FilteredTree(parent, style,
				new PatternFilter());
//		filteredTree.setBackground(parent.getDisplay().getSystemColor(
//				SWT.COLOR_LIST_BACKGROUND));

//		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		gd.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
//		gd.verticalIndent = IDialogConstants.HORIZONTAL_MARGIN;
//		filteredTree.getFilterControl().setLayoutData(gd);

		fViewer = filteredTree.getViewer();
		fViewer.setUseHashlookup(true);
        fViewer.setContentProvider(fContentProvider);
        fViewer.setLabelProvider(fLabelProvider);
        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                access$setResult(((IStructuredSelection) event.getSelection())
                        .toList());
                updateOKStatus();
            }
        });

        fViewer.setSorter(fSorter);
        if (fFilters != null) {
            for (int i = 0; i != fFilters.size(); i++) {
				fViewer.addFilter(fFilters.get(i));
			}
        }

        if (fDoubleClickSelects) {
            Tree tree = fViewer.getTree();
            tree.addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetDefaultSelected(SelectionEvent e) {
                    updateOKStatus();
                    if (fCurrStatus.isOK()) {
						access$superButtonPressed(IDialogConstants.OK_ID);
					}
                }
            });
        }
        fViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                updateOKStatus();

                //If it is not OK or if double click does not
                //select then expand
                if (!(fDoubleClickSelects && fCurrStatus.isOK())) {
                    ISelection selection = event.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        Object item = ((IStructuredSelection) selection)
                                .getFirstElement();
                        if (fViewer.getExpandedState(item)) {
							fViewer.collapseToLevel(item, 1);
						} else {
							fViewer.expandToLevel(item, 1);
						}
                    }
                }
            }
        });

        fViewer.setInput(fInput);

        return fViewer;
    }

    private boolean evaluateIfTreeEmpty(Object input) {
        Object[] elements = fContentProvider.getElements(input);
        if (elements.length > 0) {
            if (fFilters != null) {
                for (int i = 0; i < fFilters.size(); i++) {
                    ViewerFilter curr = fFilters.get(i);
                    elements = curr.filter(fViewer, input, elements);
                }
            }
        }
        return elements.length == 0;
    }

    /**
     * Set the result using the super class implementation of
     * buttonPressed.
     * @param id
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void access$superButtonPressed(int id) {
        super.buttonPressed(id);
    }

    /**
     * Set the result using the super class implementation of
     * setResult.
     * @param result
     * @see SelectionStatusDialog#setResult(int, Object)
     */
    protected void access$setResult(List result) {
        super.setResult(result);
    }

    /**
     * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
     */
    @Override
	protected void handleShellCloseEvent() {
        super.handleShellCloseEvent();

        //Handle the closing of the shell by selecting the close icon
        if (getReturnCode() == CANCEL) {
			setResult(null);
		}
    }
}
