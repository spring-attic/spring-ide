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
package org.springframework.ide.eclipse.config.tests.util.gef;

import org.eclipse.gef.EditPart;
import org.hamcrest.Matcher;


/**
 * @author Leo Dos Santos
 */
public abstract class EditPartMatcherFactory {

	public static <T extends EditPart> Matcher<T> editPartOfType(Class<? extends EditPart> type) {
		return EditPartOfType.editPartOfType(type);
	}

	public static <T extends EditPart> Matcher<T> withLabel(String label) {
		return WithLabel.withLabel(label);
	}

}
