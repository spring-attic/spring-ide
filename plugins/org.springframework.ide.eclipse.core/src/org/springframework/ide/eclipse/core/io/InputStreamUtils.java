/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility to wrap a {@link ZipEntry} based {@link InputStream} into a stream that makes sure that on calls to
 * {@link InputStream#close()} calls on to {@link ZipFile#close()}.
 * @author Christian Dupuis
 * @since 2.3.1
 */
abstract class InputStreamUtils {

	public static InputStream getWrappedInputStream(ZipFile file, ZipEntry entry) throws IOException {
		return new ZipFileClosingZipEntryInputStream(file, entry);
	}

	static class ZipFileClosingZipEntryInputStream extends InputStream {

		private final InputStream is;

		private final ZipFile file;

		public ZipFileClosingZipEntryInputStream(ZipFile file, ZipEntry entry) throws IOException {
			this.is = file.getInputStream(entry);
			this.file = file;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int read(byte b[], int off, int len) throws IOException {
			return this.is.read(b, off, len);
		}

		/**
		 * {@inheritDoc}
		 */
		public int read() throws IOException {
			return this.is.read();
		}

		/**
		 * {@inheritDoc}
		 */
		public long skip(long n) throws IOException {
			return this.is.skip(n);
		}

		/**
		 * {@inheritDoc}
		 */
		public int available() throws IOException {
			return this.is.available();
		}

		/**
		 * {@inheritDoc}
		 */
		public void close() throws IOException {
			this.is.close();
			this.file.close();
		}

		/**
		 * {@inheritDoc}
		 */
		protected void finalize() throws IOException {
			close();
		}
	}

}
