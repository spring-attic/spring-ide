/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.ide.eclipse.wizard.gettingstarted.boot.URLConnectionFactory;
import org.springframework.ide.eclipse.wizard.gettingstarted.util.DownloadManager.DownloadService;

public class SimpleDownloadService implements DownloadService {

	private final URLConnectionFactory connectionFactory;
	private static final boolean DEBUG = false;

	public SimpleDownloadService() {
		this(new URLConnectionFactory());
	}

	public SimpleDownloadService(URLConnectionFactory urlConnectionFactory) {
		this.connectionFactory = urlConnectionFactory;
	}

	//@Override
	public void fetch(URL url, OutputStream writeTo) throws IOException {
		URLConnection conn = null;
		InputStream input = null;
		try {
			conn = connectionFactory.createConnection(url);
			conn.connect();
			if (DEBUG) {
				System.out.println(">>> "+url);
				Map<String, List<String>> headers = conn.getHeaderFields();
				for (Entry<String, List<String>> header : headers.entrySet()) {
					System.out.println(header.getKey()+":");
					for (String value : header.getValue()) {
						System.out.println("   "+value);
					}
				}
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}

			input = conn.getInputStream();
			IOUtil.pipe(input, writeTo);
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (Throwable e) {
					//ignore.
				}
			}
		}
	}

}
