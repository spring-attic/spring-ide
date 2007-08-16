/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIActivationHistory;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.util.StringUtils;

/**
 * Spring Bean selection dialog.
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class BeanListSelectionDialog extends SelectionStatusDialog {

	/**
	 * Implements a {@link ViewFilter} based on content typed in the filter
	 * field
	 */
	private static class BeanFilter extends ViewerFilter {

		private StringMatcher matcher;

		private boolean isUpperCasePattern;

		private String filterText;

		private List<IBean> beanActivationHistory;

		protected BeanFilter(List<IBean> historyBeans) {
			this.beanActivationHistory = historyBeans;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (matcher == null) {
				return beanActivationHistory.contains(element);
			}
			if (element instanceof IBean) {
				IBean bean = (IBean) element;
				if (matcher.match(bean.getElementName())) {
					return true;
				}
				if (matcher.match(bean.getClassName())) {
					return true;
				}
				String[] aliases = bean.getAliases();
				if (aliases != null) {
					for (String alias : aliases) {
						if (matcher.match(alias)) {
							return true;
						}
					}
				}
				if (isUpperCasePattern) {
					if (SearchPattern.camelCaseMatch(filterText, StringUtils
							.capitalize(bean.getElementName()))) {
						return true;
					}
					String className = bean.getClassName();
					if (className != null) {
						int i = className.lastIndexOf('.');
						if (i > 0 && i < className.length()) {
							className = className.substring(i+1);
						}
						if (SearchPattern.camelCaseMatch(filterText, className)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public void setFilterText(String filterText) {
			if (filterText.trim().equals("")) {
				this.matcher = null;
				this.isUpperCasePattern = false;
				this.filterText = null;
			}
			else {
				this.matcher = new StringMatcher(filterText + '*', true, false);
				this.isUpperCasePattern = filterText.length() > 0
						&& Character.isUpperCase(filterText.charAt(0));
				this.filterText = filterText;
			}
		}
	}

	private static final String DIALOG_SETTINGS = BeanListSelectionDialog.class
			.getName();

	private static final String HEIGHT = "height";

	private static final String WIDTH = "width";

	private int fHeight = 18;

	private CLabel fLabel;

	private Point fLocation;

	private IDialogSettings fSettings;

	private Point fSize;

	private int fWidth = 60;

	private final LabelProvider labelProvider = new BeansModelLabelProvider(
			true);

	private TableViewer viewer;

	public BeanListSelectionDialog(Shell parent) {
		super(parent);
		setTitle(BeansUIPlugin
				.getResourceString("BeanListSelectionDialog.title"));
		setStatusLineAboveButtons(true);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(true);

		IDialogSettings settings = BeansUIPlugin.getDefault()
				.getDialogSettings();
		fSettings = settings.getSection(DIALOG_SETTINGS);
		if (fSettings == null) {
			fSettings = new DialogSettings(DIALOG_SETTINGS);
			settings.addSection(fSettings);
			fSettings.put(WIDTH, 480);
			fSettings.put(HEIGHT, 320);
		}
	}

	@Override
	public boolean close() {
		writeSettings();
		return super.close();
	}

	@Override
	protected void computeResult() {
		setResult(((IStructuredSelection) viewer.getSelection()).toList());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		readSettings();
		Composite area = (Composite) super.createDialogArea(parent);

		Label message = new Label(area, SWT.NONE);
		message.setText(BeansUIPlugin
				.getResourceString("BeanListSelectionDialog.message"));
		final Text filterText = new Text(area, SWT.SINGLE | SWT.BORDER);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
				false));

		Label matches = new Label(area, SWT.NONE);
		matches.setText("&Matching beans:");
		viewer = new TableViewer(area, SWT.SINGLE | SWT.BORDER);
		Control control = viewer.getControl();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = convertWidthInCharsToPixels(fWidth);
		gd.heightHint = convertHeightInCharsToPixels(fHeight);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		control.setLayoutData(gd);

		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(new ArrayContentProvider());

		final Set<IBean> beanList = new LinkedHashSet<IBean>();
		final List<IBean> historyList = new ArrayList<IBean>();
		final List<IBean> historyBeans = new ArrayList<IBean>();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {

					historyBeans.addAll(BeansUIActivationHistory
							.getBeanActivationHistory());
					Collections.reverse(historyBeans);
					historyList.addAll(historyBeans);

					Set<IBean> beans = new HashSet<IBean>();
					IBeansModel beansModel = BeansCorePlugin.getModel();
					beans.addAll(BeansModelUtils.getBeans(beansModel, monitor));

					beanList.addAll(historyBeans);
					beanList.addAll(beans);
				}
				catch (OperationCanceledException e) {
					throw new InterruptedException();
				}
			}
		};

		try {
			IRunnableContext context = new ProgressMonitorDialog(getShell());
			context.run(true, true, runnable);
		}
		catch (InvocationTargetException e) {
		}
		catch (InterruptedException e) {
		}

		viewer.setInput(beanList);

		final BeanListSelectionDialog.BeanFilter filter = new BeanListSelectionDialog.BeanFilter(
				historyBeans);
		viewer.addFilter(filter);
		viewer.setComparator(new ViewerComparator() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IBean t1 = getCorrespondingTask(e1);
				IBean t2 = getCorrespondingTask(e2);
				boolean isInHistory1 = historyList.contains(t1);
				boolean isInHistory2 = historyList.contains(t2);

				// Being on task history takes precedence...
				if (isInHistory1 && !isInHistory2) {
					return -1;
				}
				if (!isInHistory1 && isInHistory2) {
					return 1;
				}

				// Both are in task history; who is more recent?
				if (isInHistory1 && isInHistory2) {
					return historyList.indexOf(t1) - historyList.indexOf(t2);
				}

				// Both are not in task history; sort by summary...
				return labelProvider.getText(e1).compareTo(
						labelProvider.getText(e2));
			}

			private IBean getCorrespondingTask(Object o) {
				if (o instanceof IBean) {
					return (IBean) o;
				}
				return null;
			}

		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});

		viewer.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				if (getOkButton().getEnabled()) {
					okPressed();
				}
			}

		});

		filterText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					viewer.getControl().setFocus();
				}
			}

		});
		filterText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				filter.setFilterText(filterText.getText());
				viewer.refresh(false);
				Object first = viewer.getElementAt(0);
				if (first != null) {
					viewer.setSelection(new StructuredSelection(first));
				}
			}

		});

		ViewForm fForm = new ViewForm(area, SWT.BORDER | SWT.FLAT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fForm.setLayoutData(gd);
		fLabel = new CLabel(fForm, SWT.FLAT);
		fLabel.setFont(fForm.getFont());
		fForm.setContent(fLabel);

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof ITextSelection) {
			String text = ((ITextSelection) selection).getText();
			int n = text.indexOf('\n');
			if (n > -1) {
				text.substring(0, n);
			}
			filterText.setText(text);
			filterText.setSelection(0, text.length());
		}

		return area;
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point result = super.getInitialLocation(initialSize);
		if (fLocation != null) {
			result.x = fLocation.x;
			result.y = fLocation.y;
			Rectangle display = getShell().getDisplay().getClientArea();
			int xe = result.x + initialSize.x;
			if (xe > display.width) {
				result.x -= xe - display.width;
			}
			int ye = result.y + initialSize.y;
			if (ye > display.height) {
				result.y -= ye - display.height;
			}
		}
		return result;
	}

	@Override
	protected Point getInitialSize() {
		Point result = super.getInitialSize();
		if (fSize != null) {
			result.x = Math.max(result.x, fSize.x);
			result.y = Math.max(result.y, fSize.y);
			Rectangle display = getShell().getDisplay().getClientArea();
			result.x = Math.min(result.x, display.width);
			result.y = Math.min(result.y, display.height);
		}
		return result;
	}

	/**
	 * Handles a selection changed event:
	 * <ol>
	 * <li>validate the current selection
	 * <li>if an element is selected then show the element's resource (full
	 * path and icon)
	 * </ol>
	 */
	protected void handleSelectionChanged() {
		validateCurrentSelection();
		if (!viewer.getSelection().isEmpty()) {
			ISelection sel = viewer.getSelection();
			if (sel instanceof IStructuredSelection) {
				IBean bean = (IBean) ((IStructuredSelection) sel)
						.getFirstElement();
				fLabel
						.setImage(labelProvider.getImage(bean
								.getElementParent()));
				fLabel.setText(bean.getElementResource().getFullPath()
						.toString());
			}
		}
		else {
			fLabel.setImage(null);
			fLabel.setText(null);
		}
	}

	/**
	 * Initializes itself from the dialog settings with the same state as at the
	 * previous invocation.
	 */
	private void readSettings() {
		try {
			int x = fSettings.getInt("x");
			int y = fSettings.getInt("y");
			fLocation = new Point(x, y);
		}
		catch (NumberFormatException e) {
			fLocation = null;
		}
		try {
			int width = fSettings.getInt("width");
			int height = fSettings.getInt("height");
			fSize = new Point(width, height);

		}
		catch (NumberFormatException e) {
			fSize = null;
		}
	}

	/**
	 * Validates the current selection and updates the status line accordingly.
	 * @return boolean <code>true</code> if the current selection is valid.
	 */
	protected boolean validateCurrentSelection() {
		IStatus status;
		if (viewer.getSelection().isEmpty()) {
			status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
					IStatus.ERROR, "", null);
		}
		else {
			status = new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.OK,
					"", //$NON-NLS-1$
					null);
		}

		updateStatus(status);
		return status.isOK();
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeSettings() {
		Point location = getShell().getLocation();
		fSettings.put("x", location.x);
		fSettings.put("y", location.y);

		Point size = getShell().getSize();
		fSettings.put("width", size.x);
		fSettings.put("height", size.y);
	}
}
