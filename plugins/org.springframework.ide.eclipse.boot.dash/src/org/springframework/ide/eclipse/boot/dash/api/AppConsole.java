package org.springframework.ide.eclipse.boot.dash.api;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.ide.eclipse.boot.dash.console.LogType;

public interface AppConsole {

	void write(String string, LogType stdout);

	default OutputStream getOutputStream(LogType type) {
		return new OutputStream() {
			StringBuffer line = new StringBuffer();

			@Override
			public void write(int b) throws IOException {
				if (b=='\n') {
					AppConsole.this.write(line.toString(), type);
					line.delete(0, line.length());
				} else if (b=='\r') {
				} else {
					line.append((char)b);
				}
			}

			@Override
			public void close() throws IOException {
				super.close();
			}
		};

	}

	void show();

}
