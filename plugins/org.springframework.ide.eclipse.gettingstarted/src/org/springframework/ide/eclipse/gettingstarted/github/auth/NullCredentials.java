package org.springframework.ide.eclipse.gettingstarted.github.auth;

import java.net.URLConnection;

import org.eclipse.swt.browser.Browser;
import org.springframework.web.client.RestTemplate;

/**
 * Accesses github rest apis without any credentials. 
 * Severe rate limits will be in effect, but for some use cases that may be ok.
 * 
 * @author Kris De Volder
 */
public class NullCredentials extends Credentials {

	@Override
	public RestTemplate apply(RestTemplate rest) {
		return rest;
	}

	@Override
	public void apply(URLConnection conn) {
		//No credentials so nothing to apply
	}

}
