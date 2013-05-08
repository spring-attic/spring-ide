package org.springframework.ide.eclipse.gettingstarted.github.auth;

import org.springframework.web.client.RestTemplate;

/**
 * Accesses github rest apis without any credentials. 
 * Severe rate limits will be in effect, but for some use cases tat may be ok.
 * 
 * @author Kris De Volder
 */
public class NullCredentials extends Credentials {

	@Override
	public RestTemplate apply(RestTemplate rest) {
		return rest;
	}

}
