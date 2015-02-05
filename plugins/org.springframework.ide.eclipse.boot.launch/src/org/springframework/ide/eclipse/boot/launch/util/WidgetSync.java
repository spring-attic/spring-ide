///*******************************************************************************
// * Copyright (c) 2015 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.boot.launch.util;
//
//import org.eclipse.swt.events.DisposeEvent;
//import org.eclipse.swt.events.DisposeListener;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Text;
//import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
//import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
//import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
//
///**
// * Utility methods for binding UI widgets to livexp model elements.
// *
// * @author Kris De Volder
// */
//public class WidgetSync {
//
//	public static void bindCheckbox(final Button checkbox, final LiveVariable<Boolean> boolVar, final LiveVariable<Boolean> dirtyState) {
//		if (!checkbox.isDisposed()) {
//			checkbox.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					boolVar.setValue(checkbox.getSelection());
//					if (dirtyState!=null) {
//						dirtyState.setValue(true);
//					}
//				}
//			});
//			final ValueListener<Boolean> listener = new ValueListener<Boolean>() {
//				public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
//					if (!checkbox.isDisposed()) {
//						boolean e = value!=null && value;
//						checkbox.setSelection(e);
//					}
//				}
//			};
//			boolVar.addListener(listener);
//			checkbox.addDisposeListener(new DisposeListener() {
//				public void widgetDisposed(DisposeEvent e) {
//					boolVar.removeListener(listener);
//				}
//			});
//		}
//	}
//
//	public static void bindText(final Text textInput, final LiveVariable<String> textVar, final LiveVariable<Boolean> dirtyState) {
//		if (!textInput.isDisposed()) {
//			textInput.addModifyListener(new ModifyListener() {
//				public void modifyText(ModifyEvent e) {
//					textVar.setValue(textInput.getText());
//					if (dirtyState!=null) {
//						dirtyState.setValue(true);
//					}
//				}
//			});
//			final ValueListener<String> listener = new ValueListener<String>() {
//				public void gotValue(LiveExpression<String> exp, String value) {
//					if (!textInput.isDisposed()) {
//						String newText = value==null?"":value;
//						String oldText = textInput.getText();
//						if (!newText.equals(oldText)) {
//							textInput.setText(newText);
//						}
//					}
//				}
//			};
//			textVar.addListener(listener);
//			textInput.addDisposeListener(new DisposeListener() {
//				public void widgetDisposed(DisposeEvent e) {
//					textVar.removeListener(listener);
//				}
//			});
//		}
//	}
//}
