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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

/**
 * Helper class to manipulate data in a file presumed to contain
 * spring-boot configuration data.
 *
 * @author Kris De Volder
 */
public class MetaDataManipulator {

	private abstract class Content {
		public abstract String toString();
		public abstract void addProperty(JSONObject jsonObject) throws Exception;
	}

	/**
	 * Content was parse as JSONObject.
	 */
	private class ParsedContent extends Content {

		private JSONObject object;

		public ParsedContent(JSONObject o) {
			this.object = o;
		}

		public String toString() {
			return object.toString(indentFactor);
		}

		@Override
		public void addProperty(JSONObject propertyData) throws Exception {
			JSONArray properties = object.getJSONArray("properties");
			properties.put(properties.length(), propertyData);
		}
	}

	/**
	 * Content that is 'unparsed' and just a bunch of text.
	 * Used only as a fallback when data in file can't
	 * be parsed.
	 * <p>
	 * This content is manipulated by string manipulation.
	 * It is less reliable, but can be done even if the
	 * file data is not parseable.
	 */
	private class RawContent extends Content {

		private Document doc;

		public RawContent(Document doc) {
			this.doc = doc;
		}

		@Override
		public String toString() {
			return doc.get();
		}

		@Override
		public void addProperty(JSONObject propertyData) throws Exception {
			String newline = getNewline();
			int insertAt = findLast(']');
			if (insertAt<0) {
				//although we're not looking for much, we didn't find it!
				//Funky file contents. Let's just insert something at end of file in a 'best effort' spirit.
				insertAt = doc.getLength();
			}
			insert(insertAt, newline);

			insert(insertAt, propertyData.toString(indentFactor));

			int insertComma = findInsertCommaPos(insertAt);
			if (insertComma>=0) {
				insert(insertComma, ",");
			}
		}

		/**
		 * Maybe we need to add a comma in front of the new entry. This
		 * method finds if/where to stick this comma.
		 * @throws Exception
		 */
		private int findInsertCommaPos(int pos) throws Exception {
			pos--;
			Document d = doc;
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

		private int insert(int insertAt, String str) throws Exception {
			doc.replace(insertAt, 0, str);
			return insertAt + str.length();
		}

		private int findLast(char toFind) throws Exception {
			Document d = doc;
			int pos = d.getLength()-1;
			while (pos>=0 && d.getChar(pos)!=toFind) {
				pos--;
			}
			//We got here either because
			//  - we found char at pos or..
			//  - we reached position *before* start of file (i.e. -1)
			return pos;
		}

		private String getNewline() throws Exception {
			if (fNewline==null) {
				fNewline = doc.getDefaultLineDelimiter().toString();
			}
			return fNewline;
		}

	}

	public interface ContentStore {
		InputStream getContents() throws Exception;
		void setContents(InputStream inputStream) throws Exception;
	}

	private static final String INITIAL_CONTENT =
			"{\"properties\": [\n" +
			"]}";

	private static String ENCODING = "UTF8";
	private ContentStore file;
	private Content fContent;
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

	private Content getContent() throws Exception {
		if (fContent==null) {
			fContent = readContent(file, ENCODING);
		}
		return fContent;
	}

	private Content readContent(ContentStore file, String encoding) throws Exception {
		Document d = readDocument(file, encoding);
		if (isEmpty(d)) {
			JSONObject o = initialContent();
			return new ParsedContent(o);
		} else {
			try {
				return new ParsedContent(new JSONObject(d.get()));
			} catch (Exception e) {
				//couldn't parse?
				return new RawContent(d);
			}
		}
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
		getContent().addProperty(createDefaultData(propertyName));
	}

	private JSONObject createDefaultData(String propertyName) throws Exception {
		JSONObject obj = new JSONObject(new LinkedHashMap<String, Object>());
		obj.put("name", propertyName);
		obj.put("type", String.class.getName());
		obj.put("description", "A description for '"+propertyName+"'");
		obj.put("defaultValue", (Object)null);
		return obj;
	}

	/**
	 * Generate the initial content (must be generated rather than being a constant to respect newline conventions
	 * on user's system.
	 */
	private JSONObject initialContent() throws Exception {
		return new JSONObject(INITIAL_CONTENT);
	}

	/**
	 * Checks if file 'looks empty'. Files with only whitespace in them
	 * are considered empty.
	 */
	private boolean isEmpty(Document d) throws Exception {
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

	private InputStream toInputStream(Content content) throws Exception {
		return new ByteArrayInputStream(content.toString().getBytes(ENCODING));
	}

	/**
	 * Determines whether the 'reliable' manipulations can be used (which is the case
	 * only if the data in the file is valid json).
	 */
	public boolean isReliable() throws Exception {
		return getContent() instanceof ParsedContent;
	}

}
