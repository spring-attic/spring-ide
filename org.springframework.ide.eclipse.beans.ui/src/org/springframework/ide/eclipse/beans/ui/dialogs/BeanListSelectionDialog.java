package org.springframework.ide.eclipse.beans.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * Bean Selection Dialog
 * 
 * @author Christian Dupuis
 */
public class BeanListSelectionDialog extends ElementListSelectionDialog {
	
	private CLabel fLabel;
	
	protected ILabelProvider labelProvider;
	
	private IDialogSettings fSettings;
	private Point fLocation;
	private Point fSize;

	private static final String DIALOG_SETTINGS= BeanListSelectionDialog.class.getName(); //$NON-NLS-1$
	private static final String WIDTH= "width"; 
	private static final String HEIGHT= "height";
	
	public BeanListSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
		setBlockOnOpen(true);
		setEmptySelectionMessage(BeansUIPlugin.getResourceString("BeanListSelectionDialog.selectionMessage"));
		setIgnoreCase(true);
		setStatusLineAboveButtons(true);
		setTitle(BeansUIPlugin.getResourceString("BeanListSelectionDialog.title"));
		setMessage(BeansUIPlugin.getResourceString("BeanListSelectionDialog.message"));
		setMultipleSelection(false);
		this.labelProvider = renderer;
		
		IDialogSettings settings= BeansUIPlugin.getDefault().getDialogSettings();
		fSettings= settings.getSection(DIALOG_SETTINGS);
		if (fSettings == null) {
			fSettings= new DialogSettings(DIALOG_SETTINGS);
			settings.addSection(fSettings);
			fSettings.put(WIDTH, 480);
			fSettings.put(HEIGHT, 320);
		}
	}

	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		readSettings();
		Composite contents = (Composite) super.createDialogArea(parent);

		ViewForm fForm = new ViewForm(contents, SWT.BORDER | SWT.FLAT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fForm.setLayoutData(gd);
		fLabel = new CLabel(fForm, SWT.FLAT);
		fLabel.setFont(fForm.getFont());
		fForm.setContent(fLabel);

		return contents;
	}

	/*
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		final ArrayList beanList = new ArrayList();

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				IBeansModel beansModel = BeansCorePlugin.getModel();
				List beans = BeansModelUtils.getBeans(beansModel);
				beanList.addAll(beans);
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
		};

		try {
			IRunnableContext context = new BusyIndicatorRunnableContext();
			context.run(true, true, runnable);
		} catch (InvocationTargetException e) {
			return CANCEL;
		} catch (InterruptedException e) {
			// cancelled by user
			return CANCEL;
		}

		setElements(beanList.toArray());

		return super.open();
	}

	/**
	 * Handles a selection changed event. By default, the current selection is
	 * validated.
	 */
	protected void handleSelectionChanged() {
		validateCurrentSelection();
		if (getSelectedElements().length == 1) {
			IBean bean = (IBean) getSelectedElements()[0];
			fLabel.setImage(labelProvider.getImage(bean.getElementResource()));
			fLabel.setText(bean.getElementResource().getFullPath().toString());
		}
		else {
			fLabel.setImage(null);
			fLabel.setText(null);
		}
	}
	
	protected Point getInitialSize() {
		Point result= super.getInitialSize();
		if (fSize != null) {
			result.x= Math.max(result.x, fSize.x);
			result.y= Math.max(result.y, fSize.y);
			Rectangle display= getShell().getDisplay().getClientArea();
			result.x= Math.min(result.x, display.width);
			result.y= Math.min(result.y, display.height);
		}
		return result;
	}
	
	protected Point getInitialLocation(Point initialSize) {
		Point result= super.getInitialLocation(initialSize);
		if (fLocation != null) {
			result.x= fLocation.x;
			result.y= fLocation.y;
			Rectangle display= getShell().getDisplay().getClientArea();
			int xe= result.x + initialSize.x;
			if (xe > display.width) {
				result.x-= xe - display.width; 
			}
			int ye= result.y + initialSize.y;
			if (ye > display.height) {
				result.y-= ye - display.height; 
			}
		}
		return result;
	}
	
	public boolean close() {
		writeSettings();
		return super.close();
	}
	
	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readSettings() {
		try {
			int x= fSettings.getInt("x"); //$NON-NLS-1$
			int y= fSettings.getInt("y"); //$NON-NLS-1$
			fLocation= new Point(x, y);
		} catch (NumberFormatException e) {
			fLocation= null;
		}
		try {
			int width= fSettings.getInt("width"); //$NON-NLS-1$
			int height= fSettings.getInt("height"); //$NON-NLS-1$
			fSize= new Point(width, height);

		} catch (NumberFormatException e) {
			fSize= null;
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeSettings() {
		Point location= getShell().getLocation();
		fSettings.put("x", location.x); //$NON-NLS-1$
		fSettings.put("y", location.y); //$NON-NLS-1$

		Point size= getShell().getSize();
		fSettings.put("width", size.x); //$NON-NLS-1$
		fSettings.put("height", size.y); //$NON-NLS-1$
	}	
}
