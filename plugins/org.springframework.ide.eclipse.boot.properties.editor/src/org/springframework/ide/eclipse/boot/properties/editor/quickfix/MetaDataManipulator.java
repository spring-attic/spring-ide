/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.quickfix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.json.JSONObject;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

/**
 * Helper class to manipulate data in a file presumed to contain
 * spring-boot configuration data.
 *
 * @author Kris De Volder
 */
public class MetaDataManipulator {

	public interface ContentStore {
		InputStream getContents() throws Exception;
		void setContents(InputStream inputStream) throws Exception;
	}

	private static final String INITIAL_CONTENT =
			"{\"properties\": [\n" +
			"]}";

	private static String ENCODING = "UTF8";
	private ContentStore file;
	private Document content;
	private String fNewline;
	private int indentFactor = 2;

	public MetaDataManipulator(final IFile file) {
		this(new ContentStore() {
			public InputStream getContents() throws Exception {
				return file.getContents();
			}

			@Override
			public void setContents(InputStream inputStream) throws Exception {
				file.setContents(inputStream, true, true, new NullProgressMonitor());
			}
		});
	}

	public MetaDataManipulator(ContentStore contentStore) {
		this.file = contentStore;
	}

	private Document getContent() throws Exception {
		if (content==null) {
			content = readDocument(file, ENCODING);
		}
		return content;
	}

	private Document readDocument(ContentStore file, String encoding) throws Exception {
		InputStream data = file.getContents();
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			IOUtil.pipe(data, bytes);
			return new Document(new String(bytes.toByteArray(), encoding));
		} finally {
			data.close();
		}
	}

	public void addDefaultInfo(String propertyName) throws Exception {
		ensureNotEmpty();
		String newline = getNewline();
		int insertAt = findLast(']');
		if (insertAt<0) {
			//although we're not looking for much, we didn't find it!
			//Funky file contents. Let's just insert something at end of file in a 'best effort' spirit.
			insertAt = getContent().getLength();
		}
		insert(insertAt, newline);

		insert(insertAt, createDefaultData(propertyName));

		int insertComma = findInsertCommaPos(insertAt);
		if (insertComma>=0) {
			insert(insertComma, ",");
		}
	}

	private String createDefaultData(String propertyName) throws Exception {
		JSONObject obj = new JSONObject(new LinkedHashMap<String, Object>());
		obj.put("name", propertyName);
		obj.put("type", String.class.getName());
		obj.put("description", "A description for '"+propertyName+"'");
		obj.put("defaultValue", (Object)null);
		return obj.toString(indentFactor);
	}

	/**
	 * Maybe we need to add a comma in front of the new entry. This
	 * method finds if/where to stick this comma.
	 * @throws Exception
	 */
	private int findInsertCommaPos(int pos) throws Exception {
		pos--;
		Document d = getContent();
		while (pos>=0 && Character.isWhitespace(d.getChar(pos))) {
			pos--;
		}
		if (pos>=0) {
			char c = d.getChar(pos);
			if (c == '}') {
				//Add a comma after a '}'
				return pos+1;
			}
		}
		return -1;
	}

	private void ensureNotEmpty() throws Exception {
		IDocument d = getContent();
		if (isEmpty()) {
			d.set(initialContent());
		}
	}

	private int insert(int insertAt, String str) throws Exception {
		getContent().replace(insertAt, 0, str);
		return insertAt + str.length();
	}

	private int findLast(char toFind) throws Exception {
		Document d = getContent();
		int pos = d.getLength()-1;
		while (pos>=0 && d.getChar(pos)!=toFind) {
			pos--;
		}
		//We got here either because
		//  - we found char at pos or..
		//  - we reached position *before* start of file (i.e. -1)
		return pos;
	}

	/**
	 * Generate the initial content (must be generated rather than being a constant to respect newline conventions
	 * on user's system.
	 */
	private String initialContent() throws Exception {
		return INITIAL_CONTENT.replace("\n", getNewline());
	}

	private String getNewline() throws Exception {
		if (fNewline==null) {
			fNewline = getContent().getDefaultLineDelimiter().toString();
		}
		return fNewline;
	}

	/**
	 * Checks if file 'looks empty'. Files with only whitespace in them
	 * are considered empty.
	 */
	private boolean isEmpty() throws Exception {
		IDocument d = getContent();
		int len = d.getLength();
		for (int i = 0; i < len; i++) {
			if (!Character.isWhitespace(d.getChar(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * After manipulating the data, use this to persist changes back to the file.
	 */
	public void save() throws Exception {
		file.setContents(toInputStream(getContent()));
	}

	private InputStream toInputStream(IDocument content) throws Exception {
		return new ByteArrayInputStream(content.get().getBytes(ENCODING));
	}

}
