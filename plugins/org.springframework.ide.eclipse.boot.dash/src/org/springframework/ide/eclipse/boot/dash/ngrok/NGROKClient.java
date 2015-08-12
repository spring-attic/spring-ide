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
package org.springframework.ide.eclipse.boot.dash.ngrok;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Martin Lippert
 */
public class NGROKClient {

	private String url;
	private List<Process> processes;

	public NGROKClient() {
		this("http://localhost:4040");
	}

	public NGROKClient(String url) {
		this.url = url;
		this.processes = new ArrayList<Process>();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				for (Iterator<Process> iterator = processes.iterator(); iterator.hasNext();) {
					Process process = iterator.next();
					process.destroy();
				}
			}
		}));
	}

	public boolean isRunning() {
		try {
			String response = Request.Get(url + "/api").execute().returnContent().asString();
			System.out.println(response);
			return true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public NGROKTunnel[] getTunnels() {
		NGROKTunnel[] result = null;
		try {
			String response = Request.Get(url + "/api/tunnels").execute().returnContent().asString();

			JSONObject jsonResponse = new JSONObject(response);

			JSONArray tunnels = jsonResponse.getJSONArray("tunnels");
			if (tunnels != null) {
				result = new NGROKTunnel[tunnels.length()];
				for (int i = 0; i < result.length; i++) {
					JSONObject tunnel = tunnels.getJSONObject(i);
					String name = tunnel.getString("name");
					String proto = tunnel.getString("proto");
					String public_url = tunnel.getString("public_url");
					String addr = tunnel.getJSONObject("config").getString("addr");

					result[i] = new NGROKTunnel(name, proto, public_url, addr);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public NGROKTunnel createTunnel(String name, String proto, String addr) {

		try {
			ProcessBuilder processBuilder = new ProcessBuilder("/Users/mlippert/Desktop/ngrok", proto, addr);

			Process process = null;
			try {
				process = processBuilder.start();
				processes.add(process);
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Tunnel creation initiated...");

			boolean success = false;
			while (!success) {
				System.out.println("try and see if tunnel is there...");
				NGROKTunnel[] tunnels = getTunnels();
				if (tunnels != null && tunnels.length > 0) {
					for (int i = 0; i < tunnels.length; i++) {
						if (tunnels[i].getAddr().endsWith(addr)) {
							System.out.println("yepp, its here: " + tunnels[0].getPublic_url());
							return tunnels[0];
						}
					}
				}
				System.out.println("not yet there, so lets sleep for 1sec and try again...");
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
