package org.springframework.ide.eclipse.gettingstarted.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

	public static void pipe(InputStream input, OutputStream output) throws IOException {
	    byte[] buf = new byte[1024*4];
	    int n = input.read(buf);
	    while (n >= 0) {
	      output.write(buf, 0, n);
	      n = input.read(buf);
	    }
	    output.flush();        
	}

	public static void pipe(InputStream data, File target) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(target));
		try {
			pipe(data, out);
		} finally {
			out.close();
		}
	}

}
