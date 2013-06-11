/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.content;

import java.net.URL;
import java.util.List;

import org.springframework.ide.eclipse.gettingstarted.util.DownloadableItem;
import org.springframework.ide.eclipse.gettingstarted.util.UIThreadDownloadDisallowed;
import org.springframework.ide.eclipse.gettingstarted.wizard.GSImportWizard;

/**
 * Interface that needs to be implemented by any content type that can be imported via
 * the generic {@link GSImportWizard}
 * 
 * @author Kris De Volder
 */
public interface GSContent extends Describable {

	public String getName();
	public String getDisplayName();

	public List<CodeSet> getCodeSets() throws UIThreadDownloadDisallowed;
	public CodeSet getCodeSet(String name) throws UIThreadDownloadDisallowed;
	public boolean isDownloaded();
	public URL getHomePage();
	public DownloadableItem getZip();
	
}
