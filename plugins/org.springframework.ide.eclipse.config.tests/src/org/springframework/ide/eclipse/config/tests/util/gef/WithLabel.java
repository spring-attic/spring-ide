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
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;


/**
 * @author Leo Dos Santos
 */
public class WithLabel<T extends EditPart> extends BaseMatcher<T> {

	@Factory
	public static <T extends EditPart> Matcher<T> withLabel(String label) {
		return new WithLabel<T>(label);
	}

	private final String label;

	WithLabel(String label) {
		this.label = label;
	}

	public void describeTo(Description description) {
		description.appendText("with label '").appendText(label).appendText("'");
	}

	public boolean matches(Object item) {
		if (label == null || label.trim().length() == 0) {
			return false;
		}

		String toMatch = null;
		// Too specific to current visual editor implementation. What if future
		// editors don't operate on ActivityPart? There is no common way of
		// getting a label from an EditPart.
		if (item instanceof ActivityPart) {
			ActivityPart part = (ActivityPart) item;
			toMatch = part.getModelElement().getName();
		}

		if (toMatch == null) {
			return false;
		}
		else {
			return label.equals(toMatch);
		}
	}

}
