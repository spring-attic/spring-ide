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
package org.springframework.ide.eclipse.internal.bestpractices.springiderules;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Utility method(s) for use by Spring IDE rules.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class RuleUtil {

	/**
	 * Returns the specified lines of a given file. 
	 * @param filePath the path to the file to be opened
	 * @param startLineNumber The starting line number of the content to be returned
	 * @param endLineNumber The ending line number of the content to be returned
	 * @return a string containing the specified lines in the given file
	 * @throws java.io.IOException on IO errors
	 */
    public static String getFileLines(String filePath, int startLineNumber, int endLineNumber) throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuffer requestedLines = new StringBuffer();
		int currLineNum = 1;
		while(reader.ready()) {
			String currLineStr = reader.readLine();
			if (currLineNum >= startLineNumber) {
				requestedLines.append(currLineStr + "\n");
			}
			if (currLineNum > endLineNumber) {
				break;
			}
			currLineNum++;	
		}
		reader.close();
		return requestedLines.toString();
	}
	
}
