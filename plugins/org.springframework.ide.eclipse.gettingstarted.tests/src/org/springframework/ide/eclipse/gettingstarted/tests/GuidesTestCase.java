package org.springframework.ide.eclipse.gettingstarted.tests;

import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.springframework.ide.gettingstarted.guides.GettingStartedGuide;

/**
 * Some infrastucture shared among different dynamically generated testcases for
 * Guides.
 * 
 * @author Kris De Volder
 */
public class GuidesTestCase extends TestCase {
	
	
	/**
	 * The guide under test
	 */
	protected GettingStartedGuide guide;

	public GuidesTestCase(GettingStartedGuide guide) {
		super(guide.getName());
		this.guide = guide;
	}
	

}
