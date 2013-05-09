package org.springframework.ide.eclipse.gettingstarted.github.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.springframework.ide.eclipse.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.gettingstarted.util.DownloadManager.DownloadService;
import org.springframework.web.util.HtmlUtils;

/**
 * For now we need to authenticate to be able to download guides from github. 
 * This is because the github repos for guides are private at the moment.
 * 
 * @author Kris De Volder
 */
public class AuthenticatedDownloader implements DownloadService {
	
	GithubClient client = new GithubClient();

	@Override
	public void fetch(URL url, OutputStream writeTo) throws IOException {
		client.fetch(url, writeTo);
	}

}
