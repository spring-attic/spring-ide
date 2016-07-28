package org.springframework.ide.eclipse.boot.dash.cli;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.springframework.ide.eclipse.boot.util.Log;

public class CloudCliProcess extends RuntimeProcess {

	private static final String PID_REGEX = ".* INFO \\d+ --- \\[-cloud-launcher\\] .*";

	private int delegateAppPid = -1;

	public CloudCliProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		super(launch, process, name, attributes);
		getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {

			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				for (String line : text.split("\n")) {
					if (Pattern.matches(PID_REGEX, line)) {
						int startIdx = line.indexOf("INFO") + 5;
						int endIdx = startIdx;
						for (; Character.isDigit(line.charAt(endIdx)); endIdx++) {
							// nothing to do
						}
						delegateAppPid = Integer.valueOf(line.substring(startIdx, endIdx));
						monitor.removeListener(this);
						return;
					}
				}
			}

		});
	}

	@Override
	public void terminate() throws DebugException {
		if (delegateAppPid != -1) {
			try {
				if (Platform.getOS() == Platform.OS_WIN32) {
					Runtime.getRuntime().exec("Taskkill /PID " + delegateAppPid + " /F ").waitFor(500, TimeUnit.MILLISECONDS);
				} else {
					Runtime.getRuntime().exec("kill -SIGINT " + delegateAppPid).waitFor(500, TimeUnit.MILLISECONDS);
				}
			} catch (IOException | InterruptedException e) {
				Log.log(e);
			}
		}
		super.terminate();
	}



}
