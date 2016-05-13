/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.util.DocumentRegion;

import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem.problem;
import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType.*;

/**
 * Instance of this class is fed the regions of names in a properties file, checks them for duplicates and
 * reports the duplicates to {@link IProblemCollector}.
 *
 * @author Kris De Volder
 */
public class DuplicateNameChecker {

	/**
	 * Keep track of seen names. The value in the map entries is either null
	 * or the Region for the first time the name was seen.
	 * <p>
	 * This is used so that the first occurrence can still be reported retroactively
	 * when the second occurrence is encountered.
	 */
	private Map<String, DocumentRegion> seen = new HashMap<>();

	IProblemCollector problems;

	public DuplicateNameChecker(IProblemCollector problems) {
		this.problems = problems;
	}

	public void check(DocumentRegion nameRegion) {
		if (!nameRegion.isEmpty()) {
			String name = nameRegion.toString();
			if (seen.containsKey(name)) {
				DocumentRegion pending = seen.get(name);
				if (pending!=null) {
					reportDuplicate(pending);
					seen.put(name, null);
				}
				reportDuplicate(nameRegion);
			} else {
				seen.put(name, nameRegion);
			}
		}
	}

	private void reportDuplicate(DocumentRegion nameRegion) {
		problems.accept(problem(PROP_DUPLICATE_KEY,
				"Duplicate property '"+nameRegion+"'", nameRegion));
	}

}
