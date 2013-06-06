/*******************************************************************************
 * Copyright (c) 2012, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Tomasz Zarna
 */
@RunWith(Suite.class)
@SuiteClasses({ KeywordProviderSupportUnitTests.class, //
		QueryMethodCandidateUnitTests.class, //
		QueryMethodPartUnitTests.class, //
		RepositoryInformationTest.class //
})
public class AllDataCoreTests {
	// goofy junit4, no class body needed
}